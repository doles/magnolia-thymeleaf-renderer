package thymeleaf.renderer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import thymeleaf.base.AbstractMockMagnoliaTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Created by thomas on 23.12.14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfiguration.class)
public class RendererTest extends AbstractMockMagnoliaTest{



    @Before
    @Override
    public void setUp() throws Exception{
        super.setUp();
    }


    @Override
    @After
    public void cleanup() {
        super.cleanup();
    }

    @Test
    public void smokePageTest() throws Exception{
        Map<String,Object> vars = new HashMap<>();
        renderer.onRender(node, renderableDefinition, renderingContext, vars, "main.html");
        String result = stringWriter.toString();
        assertTrue("cms:init was not rendered",result.contains("<!-- cms:page"));
    }

    @Test
    public void smokeComponentTest() throws Exception{
        Map<String,Object> vars = new HashMap<>();
        renderer.onRender(node, renderableDefinition, renderingContext, vars, "main.html :: component");
        String result = stringWriter.toString();
        assertTrue("fragment is wrong",result.startsWith("<div"));
    }
}
