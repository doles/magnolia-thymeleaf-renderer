package thymeleaf.processor;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.blossom.support.IncludeRequestWrapper;
import info.magnolia.module.blossom.template.BlossomAreaDefinition;
import info.magnolia.module.blossom.template.BlossomTemplateDefinition;
import info.magnolia.module.blossom.template.HandlerMetaData;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.Arguments;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Comment;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.fragment.FragmentAndTarget;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;
import org.thymeleaf.spring3.context.SpringWebContext;
import org.thymeleaf.standard.expression.StandardExpressionProcessor;
import org.thymeleaf.standard.fragment.StandardFragmentProcessor;
import org.thymeleaf.standard.processor.attr.StandardFragmentAttrProcessor;
import thymeleaf.blossom.ThymeleafTemplateExporter;
import thymeleaf.magnolia.ThymeleafAreaElement;
import thymeleaf.magnolia.ThymeleafComponentElement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: tkratz
 * Date: 11.11.12
 * Time: 09:39
 * To change this template use File | Settings | File Templates.
 */
public class CmsComponentElementProcessor extends AbstractRecursiveInclusionProcessor {

    private static final String FRAGMENT_ATTR_NAME = StandardFragmentAttrProcessor.ATTR_NAME;
    public static final String ATTR_NAME = "component";

    private ThymeleafTemplateExporter templateExporter;
    private List<HandlerMapping> handlerMappings;
    private List<HandlerAdapter> handlerAdapters;



    public CmsComponentElementProcessor(ApplicationContext ctx, ServletContext sctx) {

        super(ctx,sctx,ATTR_NAME);
        this.templateExporter = ctx.getBean(ThymeleafTemplateExporter.class);

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

    private ThymeleafComponentElement createComponentElement() {
        final RenderingEngine renderingEngine = Components.getComponent(RenderingEngine.class);
        final RenderingContext renderingContext = renderingEngine.getRenderingContext();

        return Components.getComponentProvider().newInstance(ThymeleafComponentElement.class, renderingContext);
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

        javax.jcr.Node content =(javax.jcr.Node) StandardExpressionProcessor.processExpression(arguments,  element.getAttributeValue(attributeName));

        Object ctxObj = StandardExpressionProcessor.processExpression(
                arguments, "${renderingContext}");
        if (!(ctxObj instanceof RenderingContext)) {
            throw new TemplateProcessingException("Musst pass a RenderingContext here");
        }
        BlossomTemplateDefinition templateDefinition = null;
        try {
            RenderingContext renderingContext = (RenderingContext) ctxObj;
            templateDefinition = (BlossomTemplateDefinition) renderingContext.getRenderableDefinition();


        } catch (ClassCastException x) {

            throw new TemplateProcessingException("Only Blossom, templates supported", x);
        }

        if (templateDefinition == null) {
            throw new TemplateProcessingException("Template not found" );
        }

        Object handlerBean = null;
        String path = templateDefinition.getHandlerPath();
        for (HandlerMetaData meta : templateExporter.getDetectedHandlers().getTemplates()) {


            if (meta.getHandlerPath().equals(path)) {
                handlerBean = meta.getHandler();
                break;
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
            throw new TemplateProcessingException("Na HandlerAdapter found");

        }
        ModelAndView mv = null;
        MgnlContext.getWebContext().push(request, response);
        request = new IncludeRequestWrapper(request, MgnlContext.getContextPath() + path, MgnlContext.getContextPath(), path, null, request.getQueryString());

        try {
            mv = adapter.handle(request, response, handlerBean);
        } catch (Exception e) {
            throw new TemplateProcessingException("Spring handler error", e);
        } finally {
            MgnlContext.getWebContext().pop();
        }
        String template = mv.getViewName();


        //Object result = handler.invoke(handlerBean)

        final boolean substituteInclusionNode =
                getSubstituteInclusionNode(arguments, element, attributeName, template);

        final FragmentAndTarget fragmentAndTarget =
                getFragmentAndTarget(arguments, element, attributeName, template, substituteInclusionNode);

        ThymeleafComponentElement componentElement = createComponentElement();
        componentElement.setContent(content);
       // componentElement.setName(templateDefinition.getName());
        StringWriter out = new StringWriter();

        String comment = componentElement.createComment();

        Comment commentNode = new Comment(comment);

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("content",componentElement.getContent());

        doRecursiveProcessing(arguments,element,attributeName,template,mv.getViewName(),substituteInclusionNode,commentNode,vars,templateDefinition.getTitle()," /cms:component");
        return ProcessorResult.OK;
    }



}
