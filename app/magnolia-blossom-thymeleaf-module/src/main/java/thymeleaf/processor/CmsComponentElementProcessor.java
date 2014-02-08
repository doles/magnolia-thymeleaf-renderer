package thymeleaf.processor;

import info.magnolia.jcr.util.ContentMap;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.templating.elements.ComponentElement;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.ProcessorResult;
import org.thymeleaf.standard.expression.Expression;
import org.thymeleaf.standard.expression.StandardExpressionParser;

/**
 * Created with IntelliJ IDEA.
 * User: tkratz
 * Date: 11.11.12
 * Time: 09:39
 * To change this template use File | Settings | File Templates.
 */
public class CmsComponentElementProcessor extends AbstractCmsElementProcessor<ComponentElement> {

    public static final String ATTR_NAME = "component";

    public CmsComponentElementProcessor() {

        super(ATTR_NAME);
    }

    @Override
    public final ProcessorResult processAttribute(final Arguments arguments, final Element element, final String attributeName) {

        final Expression expression = new StandardExpressionParser().parseExpression(arguments.getConfiguration(),
                arguments, element.getAttributeValue(attributeName));
        final Object contentObject = expression.execute(arguments.getConfiguration(), arguments);

        final javax.jcr.Node content;
        if(contentObject instanceof ContentMap){
            content = ((ContentMap)contentObject).getJCRNode();
        } else if(contentObject instanceof javax.jcr.Node){
            content =(javax.jcr.Node)contentObject;
        }else {
            throw new TemplateProcessingException("Cannot cast "+contentObject.getClass()+" to javax.jcr.Node");
        }

        final RenderingEngine renderingEngine = Components.getComponent(RenderingEngine.class);
        final RenderingContext renderingContext = renderingEngine.getRenderingContext();

        ComponentElement componentElement = createElement(renderingContext);
        componentElement.setContent(content);
        processElement(element, attributeName, componentElement);

        return ProcessorResult.OK;
    }
}
