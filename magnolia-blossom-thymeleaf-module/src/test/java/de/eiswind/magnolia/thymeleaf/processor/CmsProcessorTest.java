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

import de.eiswind.magnolia.thymeleaf.base.AbstractMockMagnoliaTest;
import de.eiswind.magnolia.thymeleaf.renderer.TestConfiguration;
import edu.emory.mathcs.backport.java.util.Collections;
import info.magnolia.module.blossom.template.BlossomTemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.templating.elements.ComponentElement;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.thymeleaf.Arguments;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.Context;
import org.thymeleaf.dom.Comment;
import org.thymeleaf.dom.Document;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Macro;
import org.thymeleaf.dom.Node;
import org.thymeleaf.resourceresolver.ClassLoaderResourceResolver;
import org.thymeleaf.templateresolver.AlwaysValidTemplateResolutionValidity;
import org.thymeleaf.templateresolver.TemplateResolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Thomas on 24.12.2014.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfiguration.class)
public class CmsProcessorTest extends AbstractMockMagnoliaTest {


    private Arguments arguments;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final TemplateProcessingParameters templateProcessingParameters =
                new TemplateProcessingParameters(thymeEngine.getConfiguration(), "main.html", new Context());

        final TemplateResolution templateResolution =
                new TemplateResolution("main.html", "resource:" + "main.html",
                        new ClassLoaderResourceResolver(), "UTF-8", "HTML5", new AlwaysValidTemplateResolutionValidity());
        thymeEngine.initialize();
        arguments = new Arguments(thymeEngine, templateProcessingParameters, templateResolution, thymeEngine.getTemplateRepository(), new Document());


    }

    @After
    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Test
    public void testInitProcessor() throws Exception {

        CmsInitElementProcessor cmsInitElementProcessor = new CmsInitElementProcessor();
        Element element = new Element("head", "main.html");
        element.setAttribute("cms:init", false, "");
        List<Node> result = cmsInitElementProcessor.getModifiedChildren(arguments, element, "init");
        List<Node> comments = result.stream().filter((node) -> node instanceof Comment).collect(Collectors.toList());
        assertEquals("Expected two comments", 2, comments.size());
        comments.forEach((comment) -> Assert.assertTrue("should contain cms:page", ((Comment) comment).getContent().contains("cms:page")));
    }

    @Test
    public void testComponentProcessor() throws Exception {

        BlossomTemplateDefinition blossomTemplateDefinition = new BlossomTemplateDefinition();
        blossomTemplateDefinition.setName("test");
        blossomTemplateDefinition.setI18nBasename("en");
        TemplateDefinitionAssignment templateDefinitionAssignment = mock(TemplateDefinitionAssignment.class);
        when(templateDefinitionAssignment.getAssignedTemplateDefinition(any())).thenReturn(blossomTemplateDefinition);
        when(componentProvider.newInstance(eq(ComponentElement.class), any())).thenReturn(new ComponentElement(config, renderingContext, engine, templateDefinitionAssignment));


        Element element = new Element("div", "main.html");
        element.setAttribute("cms:component", false, "${content}");
        Map<String, Object> vars = new HashMap<>();
        vars.put("content", node);
        Arguments args2 = arguments.addLocalVariables(vars);


        CmsComponentElementProcessor cmsComponentElementProcessor = new CmsComponentElementProcessor();
        cmsComponentElementProcessor.processAttribute(args2, element, "cms:component");

        List<Node> macros = element.getChildren().stream().filter((node) -> node instanceof Macro).collect(Collectors.toList());
        assertEquals("Expected one macro", 1, macros.size());
        Macro macro = (Macro) macros.get(0);
        assertTrue("should contain cms:compnent", macro.getContent().contains("<!-- cms:component"));

    }

    @Test
    public void testAreaProcessor() throws Exception {


        Element element = new Element("div", "main.html");
        element.setAttribute("cms:area", false, "Area");
        List<javax.jcr.Node> nodes = Collections.emptyList();
        Map<String, Object> vars = new HashMap<>();
        vars.put("components", nodes);
        Arguments args2 = arguments.addLocalVariables(vars);


        CmsAreaElementProcessor cmsAreaElementProcessor = new CmsAreaElementProcessor();
        cmsAreaElementProcessor.processAttribute(args2, element, "cms:area");

        List<Node> macros = element.getChildren().stream().filter((node) -> node instanceof Macro).collect(Collectors.toList());
        assertEquals("Expected one macro", 1, macros.size());
        Macro macro = (Macro) macros.get(0);
        assertTrue("should contain cms:area", macro.getContent().contains("<!-- cms:area"));

    }
}
