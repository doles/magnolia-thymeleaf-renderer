package thymeleaf.processor;

import info.magnolia.module.blossom.template.BlossomTemplateDefinition;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.templating.elements.AreaElement;

import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.ProcessorResult;

/**
 * Created with IntelliJ IDEA.
 * User: tkratz
 * Date: 11.11.12
 * Time: 09:39
 * To change this template use File | Settings | File Templates.
 */
public class CmsAreaElementProcessor extends AbstractCmsElementProcessor<AreaElement> {


    public static final String ATTR_NAME = "area";

    public CmsAreaElementProcessor() {

        super(ATTR_NAME);
    }

    @Override
    public final ProcessorResult processAttribute(final Arguments arguments, final Element element, final String attributeName) {

        final String attributeValue = element.getAttributeValue(attributeName);

        final RenderingEngine renderingEngine = Components.getComponent(RenderingEngine.class);
        final RenderingContext renderingContext = renderingEngine.getRenderingContext();

        AreaDefinition areaDef = null;
        BlossomTemplateDefinition templateDefinition;
        try {

            templateDefinition = (BlossomTemplateDefinition) renderingContext.getRenderableDefinition();
            if (templateDefinition.getAreas().containsKey(attributeValue)) {
                areaDef = templateDefinition.getAreas().get(attributeValue);
            }

        } catch (ClassCastException x) {

            throw new TemplateProcessingException("Only Blossom, templates supported", x);
        }

        if (areaDef == null) {
            throw new TemplateProcessingException("Area not found:" + attributeValue);
        }

        AreaElement areaElement = createElement(renderingContext);
        areaElement.setName(areaDef.getName());
        processElement(element, attributeName, areaElement);

        return ProcessorResult.OK;
    }
}
