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
 * the component processor.
 */
public  class CmsComponentElementProcessor extends AbstractCmsElementProcessor<ComponentElement> {

    /**
     * the content attribute name.
     */
    public static final String ATTR_NAME = "component";

    /**
     * instance.
     */
    public CmsComponentElementProcessor() {

        super(ATTR_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessorResult processAttribute(Arguments arguments, Element element,  String attributeName) {

        final Expression expression = new StandardExpressionParser().parseExpression(arguments.getConfiguration(),
                arguments, element.getAttributeValue(attributeName));
        final Object contentObject = expression.execute(arguments.getConfiguration(), arguments);

        final javax.jcr.Node content;
        if (contentObject instanceof ContentMap) {
            content = ((ContentMap) contentObject).getJCRNode();
        } else if (contentObject instanceof javax.jcr.Node) {
            content = (javax.jcr.Node) contentObject;
        } else {
            throw new TemplateProcessingException("Cannot cast " + contentObject.getClass() + " to javax.jcr.Node");
        }

        final RenderingEngine renderingEngine = Components.getComponent(RenderingEngine.class);
        final RenderingContext renderingContext = renderingEngine.getRenderingContext();

        ComponentElement componentElement = createElement(renderingContext);
        componentElement.setContent(content);
        processElement(element, attributeName, componentElement);

        return ProcessorResult.OK;
    }
}
