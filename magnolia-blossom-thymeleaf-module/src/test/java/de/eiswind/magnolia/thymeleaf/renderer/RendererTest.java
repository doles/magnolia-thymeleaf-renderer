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

package de.eiswind.magnolia.thymeleaf.renderer;

import de.eiswind.magnolia.thymeleaf.base.AbstractMockMagnoliaTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by thomas on 23.12.14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfiguration.class)
public class RendererTest extends AbstractMockMagnoliaTest {


    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }


    @Override
    @After
    public void cleanup() {
        super.cleanup();
    }

    @Test
    public void smokePageTest() throws Exception {
        Map<String, Object> vars = new HashMap<>();
        renderer.onRender(node, renderableDefinition, renderingContext, vars, "main.html");
        String result = stringWriter.toString();
        assertTrue("cms:init was not rendered", result.contains("<!-- cms:page"));
    }

    @Test
    public void smokeComponentTest() throws Exception {
        Map<String, Object> vars = new HashMap<>();
        renderer.onRender(node, renderableDefinition, renderingContext, vars, "main.html :: component");
        String result = stringWriter.toString();
        assertTrue("fragment is wrong", result.startsWith("<div"));
    }
}
