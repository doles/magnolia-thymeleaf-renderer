/*
 * Copyright (c) 2014 Thomas Kratz
 *
 This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Dieses Programm ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder neueren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Dieses Programm wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */

package de.eiswind.magnolia.thymeleaf.processor;

import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.templating.elements.TemplatingElement;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Macro;
import org.thymeleaf.dom.Node;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.attr.AbstractAttrProcessor;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;

/**
 * abstract base class for the magnolia element processors.
 *
 * @param <T> the element type.
 */
public abstract class AbstractCmsElementProcessor<T extends TemplatingElement> extends AbstractAttrProcessor {


    private static final int PRECEDENCE = 1000;

    /**
     * initializes the attribute name.
     *
     * @param attrName the attribute name
     */
    public AbstractCmsElementProcessor(String attrName) {
        super(attrName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getPrecedence() {
        return PRECEDENCE;
    }

    /**
     * create mgnl templating element.
     *
     * @param renderingContext the context
     * @return the teplating element
     */
    protected final T createElement(RenderingContext renderingContext) {
        return Components.getComponentProvider().newInstance(getTemplatingElementClass(), renderingContext);
    }

    /**
     * the type of this element.
     *
     * @return the type
     */
    @SuppressWarnings("unchecked")
    protected final Class<T> getTemplatingElementClass() {
        return (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * mimics mgnl rendering behaviour.
     *
     * @param element           the thyme element
     * @param attributeName     the att name
     * @param templatingElement the mgnl templating element
     */
    protected final void processElement(Element element, String attributeName, T templatingElement) {
        final StringBuilder out = new StringBuilder();
        try {
            templatingElement.begin(out);
            templatingElement.end(out);
        } catch (RenderException | IOException e) {
            throw new TemplateProcessingException("render area element", e);
        }

        // now convert the cms:area into a macro node and return done
        final Macro macro = new Macro(out.toString());
        macro.setProcessable(false);

        // remove all children so they are ignored
        element.clearChildren();
        // and set children now to be our html
        element.setChildren(Collections.<Node>singletonList(macro));

        // remove cms:area attribute so isn't processed again
        element.removeAttribute(attributeName);
    }
}
