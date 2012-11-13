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
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.OrderComparator;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.Arguments;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.dom.Comment;
import org.thymeleaf.dom.Document;
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
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
public abstract class AbstractRecursiveInclusionProcessor extends AbstractAttrProcessor {

    private static final String FRAGMENT_ATTR_NAME = StandardFragmentAttrProcessor.ATTR_NAME;

    protected ServletContext servletContext;
    protected ApplicationContext context;

    public AbstractRecursiveInclusionProcessor(ApplicationContext ctx, ServletContext sctx, String attrName){
        super(attrName);
        this.context =ctx;
        this.servletContext =sctx;
    }

    protected void doRecursiveProcessing(Arguments arguments, Element element, String attributeName, String fragmentSpec, String template, boolean substituteInclusionNode, Comment commentNode, Map<String, Object> vars, String documentName, String closeTag) {
        final IWebContext webcontext =
                new SpringWebContext(MgnlContext.getWebContext().getRequest(), MgnlContext.getWebContext().getResponse(), servletContext , MgnlContext.getWebContext().getRequest().getLocale(), vars, this.context);
        final FragmentAndTarget fragmentAndTarget =
                getFragmentAndTarget(arguments, element, attributeName, template, substituteInclusionNode);


        final List<Node> fragmentNodes =
                fragmentAndTarget.extractFragment(
                        arguments.getConfiguration(), webcontext, arguments.getTemplateRepository());

        if (fragmentNodes == null) {
            throw new TemplateProcessingException(
                    "Cannot correctly process \"" + attributeName + "\" attribute. " +
                            "Fragment specification \"" + fragmentSpec + "\" matched null.");
        }
        // process fragment nodes


        Document document = new Document(documentName);
        for (final Node processingRootNode : fragmentNodes) {
            if (processingRootNode != null) {
                final Node clonedProcessingRootNode =
                        processingRootNode.cloneNode(document, false);
                document.addChild(clonedProcessingRootNode);
            }
        }
        TemplateProcessingParameters params = new TemplateProcessingParameters(arguments.getConfiguration(), documentName,webcontext );

        final Arguments areaArgs =
                new Arguments(arguments.getTemplateEngine(),params,arguments.getTemplateResolution(),arguments.getTemplateRepository(),document);

        document.precompute(areaArgs.getConfiguration());
        document.process(areaArgs);

        element.clearChildren();
        element.removeAttribute(attributeName);


        List<Node> newNodes = new ArrayList<Node>();
        newNodes.add(commentNode);
        for(Node newNode : document.getChildren()){
            final Node clonedNode =
                    newNode.cloneNode(arguments.getDocument(), false);
            newNodes.add(clonedNode);

        }


        commentNode = new Comment(closeTag);
        newNodes.add(commentNode);
        if (substituteInclusionNode) {

            element.setChildren(newNodes);
            element.getParent().extractChild(element);

        } else {

            for (final Node fragmentNode : newNodes) {
                element.addChild(fragmentNode);
            }

        }
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

}
