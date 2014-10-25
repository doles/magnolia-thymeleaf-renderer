package thymeleaf.processor;

import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.templating.elements.TemplatingElement;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;

import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Macro;
import org.thymeleaf.dom.Node;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;

/**
 * Created with IntelliJ IDEA.
 * User: tkratz
 * Date: 11.11.12
 * Time: 09:39
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCmsElementProcessor<T extends TemplatingElement> extends AbstractAttrProcessor {

    public AbstractCmsElementProcessor(String attrName){
        super(attrName);
    }

    @Override
    public int getPrecedence() {
        return 1000;
    }

    protected T createElement(final RenderingContext renderingContext) {

        return Components.getComponentProvider().newInstance(getTemplatingElementClass(), renderingContext);
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getTemplatingElementClass() {
        return (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected void processElement(final Element element, final String attributeName, final T templatingElement) {
        final StringBuilder out = new StringBuilder();
        try {
            templatingElement.begin(out);
            templatingElement.end(out);
        } catch (final RenderException | IOException e) {
            throw new TemplateProcessingException("render area element", e);
        }

        // now convert the cms:area into a macro node and return done
        final Macro macro = new Macro(out.toString());
        macro.setProcessable(false);

        // remove all children so they are ignored
        element.clearChildren();
        // and set children now to be our html
        element.setChildren(Collections.<Node> singletonList(macro));

        // remove cms:area attribute so isn't processed again
        element.removeAttribute(attributeName);
    }
}
