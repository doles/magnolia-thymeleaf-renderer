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
 * the area processor.
 */
public class CmsAreaElementProcessor extends AbstractCmsElementProcessor<AreaElement> {


    /**
     * the area attribute name.
     */
    public static final String ATTR_NAME = "area";

    /**
     * create an instance.
     */
    public CmsAreaElementProcessor() {

        super(ATTR_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessorResult processAttribute( Arguments arguments,  Element element,  String attributeName) {

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
