package thymeleaf.processor;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.blossom.render.BlossomDispatcherServlet;
import info.magnolia.module.blossom.support.ForwardRequestWrapper;
import info.magnolia.module.blossom.support.IncludeRequestWrapper;
import info.magnolia.module.blossom.template.BlossomAreaDefinition;
import info.magnolia.module.blossom.template.BlossomTemplateDefinition;
import info.magnolia.module.blossom.template.HandlerMetaData;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.templating.elements.AreaElement;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.*;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.fragment.FragmentAndTarget;
import org.thymeleaf.processor.IAttributeNameProcessorMatcher;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;
import org.thymeleaf.standard.fragment.StandardFragmentProcessor;
import org.thymeleaf.standard.processor.attr.AbstractStandardFragmentHandlingAttrProcessor;
import org.thymeleaf.standard.processor.attr.StandardFragmentAttrProcessor;
import org.thymeleaf.util.PrefixUtils;
import thymeleaf.blossom.ThymeleafTemplateExporter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tkratz
 * Date: 11.11.12
 * Time: 09:39
 * To change this template use File | Settings | File Templates.
 */
public class CmsAreaElementProcessor extends AbstractAttrProcessor {

    private static final String FRAGMENT_ATTR_NAME = StandardFragmentAttrProcessor.ATTR_NAME;
    public static final String ATTR_NAME = "area";

    private ThymeleafTemplateExporter templateExporter;
    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;
    private ApplicationContext context;

    public CmsAreaElementProcessor(ApplicationContext ctx) {

        super(ATTR_NAME);
        this.templateExporter = ctx.getBean(ThymeleafTemplateExporter.class);
        this.context = ctx;
        initGHandlerAdapters();
        initHandlerMappings();
    }

    private void initHandlerMappings() {
        Map<String, HandlerMapping> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.handlerMappings = new ArrayList<HandlerMapping>(matchingBeans.values());
            // We keep HandlerMappings in sorted order.
            OrderComparator.sort(this.handlerMappings);
        }
    }

    private void initGHandlerAdapters() {
        Map<String, HandlerAdapter> matchingBeans =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.handlerAdapters = new ArrayList<HandlerAdapter>(matchingBeans.values());
            // We keep HandlerAdapters in sorted order.
            OrderComparator.sort(this.handlerAdapters);
        }
    }

    private AreaElement createAreaElement() {
        final RenderingEngine renderingEngine = Components.getComponent(RenderingEngine.class);
        final RenderingContext renderingContext = renderingEngine.getRenderingContext();

        return Components.getComponentProvider().newInstance(AreaElement.class, renderingContext);
    }

    @Override
    public int getPrecedence() {
        return 1000;
    }


    protected String getTargetAttributeName(
            final Arguments arguments, final Element element,
            final String attributeName, final String attributeValue) {

        if (attributeName != null) {
            final String prefix = "th";
            if (prefix != null) {
                return prefix + ":" + FRAGMENT_ATTR_NAME;
            }
        }
        return FRAGMENT_ATTR_NAME;

    }


    protected boolean getSubstituteInclusionNode(
            final Arguments arguments,
            final Element element, final String attributeName, final String attributeValue) {
        // th:include does not substitute the inclusion node
        return false;
    }


    @Override
    public final ProcessorResult processAttribute(final Arguments arguments, final Element element, final String attributeName) {


        HttpServletRequest request = MgnlContext.getWebContext().getRequest();
        final HttpServletResponse response = MgnlContext.getWebContext().getResponse();

        final String attributeValue = element.getAttributeValue(attributeName);

        Object ctxObj = StandardExpressionProcessor.processExpression(
                arguments, "${renderingContext}");
        if (!(ctxObj instanceof RenderingContext)) {
            throw new TemplateProcessingException("Musst pass a RenderingContext here");
        }
        AreaDefinition areaDef = null;
        try {
            RenderingContext renderingContext = (RenderingContext) ctxObj;
            BlossomTemplateDefinition templateDefinition = (BlossomTemplateDefinition) renderingContext.getRenderableDefinition();
            if (templateDefinition.getAreas().containsKey(attributeValue)) {
                areaDef = templateDefinition.getAreas().get(attributeValue);
            }

        } catch (ClassCastException x) {

            throw new TemplateProcessingException("Only Blossom, templates supported", x);
        }

        if (areaDef == null) {
            throw new TemplateProcessingException("Area not found:" + attributeValue);
        }

        Object handlerBean = null;
        String path = ((BlossomAreaDefinition) areaDef).getHandlerPath();
        for (HandlerMetaData meta : templateExporter.getDetectedHandlers().getTemplates()) {
            final List<HandlerMetaData> areasByEnclosingClass = templateExporter.getDetectedHandlers().getAreasByEnclosingClass(meta.getHandlerClass());
            if (areasByEnclosingClass != null) {
                for (HandlerMetaData areaMeta : areasByEnclosingClass) {


                    if (areaMeta.getHandlerPath().equals(path)) {
                        handlerBean = areaMeta.getHandler();
                        break;
                    }
                }
            }
        }
        if (handlerBean == null) {
            throw new TemplateProcessingException("Handler not found");
        }

        HandlerExecutionChain chain = null;
        try {
            for (HandlerMapping hm : this.handlerMappings) {
                HandlerExecutionChain handler = hm.getHandler(request);
                if (handler != null) {
                    chain = handler;
                    break;
                }
            }
        } catch (Exception e) {
            throw new TemplateProcessingException("Cannot find handler", e);
        }
        if (chain == null) {
            throw new TemplateProcessingException("Handler not found " + handlerBean.getClass().getName());
        }
        HandlerAdapter adapter = null;
        for (HandlerAdapter ha : this.handlerAdapters) {

            if (ha.supports(handlerBean)) {
                adapter = ha;
                break;
            }
        }
        if (adapter == null) {
            throw new TemplateProcessingException("Na HandlerAdepter found");

        }
        ModelAndView mv = null;
        MgnlContext.getWebContext().push(request,response);
        request = new IncludeRequestWrapper(request, MgnlContext.getContextPath() + path, MgnlContext.getContextPath(),path, null, request.getQueryString());

        try {
            mv = adapter.handle(request, response, handlerBean);
        } catch (Exception e) {
            throw new TemplateProcessingException("Spring handler error", e);
        }  finally {
            MgnlContext.getWebContext().pop();
        }
        String template = mv.getViewName();


        //Object result = handler.invoke(handlerBean)

        final boolean substituteInclusionNode =
                getSubstituteInclusionNode(arguments, element, attributeName, template);

        final FragmentAndTarget fragmentAndTarget =
                getFragmentAndTarget(arguments, element, attributeName, template, substituteInclusionNode);

        final List<Node> fragmentNodes =
                fragmentAndTarget.extractFragment(
                        arguments.getConfiguration(), arguments, arguments.getTemplateRepository());

        if (fragmentNodes == null) {
            throw new TemplateProcessingException(
                    "Cannot correctly process \"" + attributeName + "\" attribute. " +
                            "Fragment specification \"" + attributeValue + "\" matched null.");
        }


        element.clearChildren();
        element.removeAttribute(attributeName);

        if (substituteInclusionNode) {

            element.setChildren(fragmentNodes);
            element.getParent().extractChild(element);

        } else {

            for (final Node fragmentNode : fragmentNodes) {
                element.addChild(fragmentNode);
            }

        }

        return ProcessorResult.OK;

    }


    protected final FragmentAndTarget getFragmentAndTarget(final Arguments arguments,
                                                           final Element element, final String attributeName, final String attributeValue,
                                                           final boolean substituteInclusionNode) {

        final String targetAttributeName =
                getTargetAttributeName(arguments, element, attributeName, attributeValue);

        return StandardFragmentProcessor.computeStandardFragmentSpec(
                arguments.getConfiguration(), arguments, attributeValue, null, targetAttributeName,
                !substituteInclusionNode);

    }

}
