package thymeleaf.renderer;

import com.google.inject.Provider;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.blossom.render.RenderContext;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.templating.functions.TemplatingFunctions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.thymeleaf.spring4.SpringTemplateEngine;

import javax.annotation.Resource;
import javax.jcr.Node;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by thomas on 23.12.14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfiguration.class)
public class RendererTest {


    @Resource
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    }
    @Test
    public void testRenderer() throws Exception{

        Node node = mock(Node.class);
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);
        HttpServletResponse response = new MockHttpServletResponse();
        WebContext webCtx = mock(WebContext.class);
        when(webCtx.getRequest()).thenReturn(request);
        when(webCtx.getResponse()).thenReturn(response);

        MgnlContext.setInstance(webCtx);
        mockStatic(Components.class);
        RenderingEngine engine = mock(RenderingEngine.class);

        ComponentProvider componentProvider = mock(ComponentProvider.class);

        PowerMockito.when(componentProvider.getComponent(RenderingEngine.class)).thenReturn(engine);
        Provider<AggregationState> provider = mock(Provider.class);
        TemplatingFunctions templatingFunctions = new TemplatingFunctions(provider);
        PowerMockito.when(componentProvider.getComponent(TemplatingFunctions.class)).thenReturn(templatingFunctions);

        Components.pushProvider(componentProvider);

        RenderContext.push();
        RenderContext.get().setModel(new HashMap<>());

        ServletContext servletContext = mock(ServletContext.class);

        SpringTemplateEngine thymeEngine = new SpringTemplateEngine();

        ThymeleafRenderer renderer = new ThymeleafRenderer();
        renderer.setApplicationContext(webApplicationContext);
        renderer.setServletContext(servletContext);
        renderer.setEngine(thymeEngine);
        RenderableDefinition renderableDefinition = mock(RenderableDefinition.class);
        RenderingContext renderingContext = mock(RenderingContext.class);

        Map<String,Object> vars = new HashMap<String,Object>();

        renderer.onRender(node,renderableDefinition, renderingContext, vars, "");


    }

}
