
/*
 * Copyright (c) 2014 Thomas Kratz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
