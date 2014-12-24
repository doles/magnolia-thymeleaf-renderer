package thymeleaf.base;

import com.google.inject.Provider;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.blossom.render.RenderContext;
import info.magnolia.module.blossom.template.BlossomTemplateDefinition;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.variation.RenderableVariationResolver;
import info.magnolia.rendering.util.AppendableWriter;
import info.magnolia.templating.elements.AreaElement;
import info.magnolia.templating.functions.TemplatingFunctions;
import org.junit.After;
import org.junit.Before;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import thymeleaf.dialect.MagnoliaDialect;
import thymeleaf.renderer.ThymeleafRenderer;

import javax.annotation.Resource;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Thomas on 24.12.2014.
 */
public class AbstractMockMagnoliaTest {

    @Resource
    private WebApplicationContext webApplicationContext;

    protected Node node;
    protected ThymeleafRenderer renderer;
    protected RenderableDefinition renderableDefinition;
    protected RenderingContext renderingContext;
    protected StringWriter stringWriter;
    protected SpringTemplateEngine thymeEngine;
    protected ComponentProvider componentProvider;
    protected ServerConfiguration config;
    protected RenderingEngine engine;




    @Before
    public void setUp() throws Exception{

        /** mock up magnolia */
        node = mock(Node.class);
        Session session = mock(Session.class);
        Workspace workspace = mock(Workspace.class);
        when(workspace.getName()).thenReturn("pages");
        when(session.getWorkspace()).thenReturn(workspace);
        when(session.hasPermission(any(),any())).thenReturn(true);
        when(node.getSession()).thenReturn(session);
        when(node.getPath()).thenReturn("/home");
        NodeIterator nodeIterator = mock(NodeIterator.class);
        when(nodeIterator.hasNext()).thenReturn(false);
        when(node.getNodes()).thenReturn(nodeIterator);

        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);
        HttpServletResponse response = new MockHttpServletResponse();
        WebContext webCtx = mock(WebContext.class);
        when(webCtx.getRequest()).thenReturn(request);
        when(webCtx.getResponse()).thenReturn(response);
        AggregationState state = mock(AggregationState.class);
        when(state.getMainContentNode()).thenReturn(node);

        when(webCtx.getAggregationState()).thenReturn(state);

        AccessManager accessManager = mock(AccessManager.class);
        when(accessManager.isGranted(anyString(),anyLong())).thenReturn(true);
        when(webCtx.getAccessManager(anyString())).thenReturn(accessManager);
        when(webCtx.getLocale()).thenReturn(Locale.ENGLISH);
        MgnlContext.setInstance(webCtx);

        config = mock(ServerConfiguration.class);
        when(config.isAdmin()).thenReturn(true);

        componentProvider = mock(ComponentProvider.class);
        when(componentProvider.getComponent(ServerConfiguration.class)).thenReturn(config);
        engine = mock(RenderingEngine.class);
        when(componentProvider.getComponent(RenderingEngine.class)).thenReturn(engine);
        Provider<AggregationState> provider = mock(Provider.class);
        TemplatingFunctions templatingFunctions = new TemplatingFunctions(provider);
        when(componentProvider.getComponent(TemplatingFunctions.class)).thenReturn(templatingFunctions);
        I18nContentSupport i18nContentSupport = mock(I18nContentSupport.class);
        when(i18nContentSupport.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        when(componentProvider.getComponent(I18nContentSupport.class)).thenReturn(i18nContentSupport);



        Components.pushProvider(componentProvider);

        DefaultMessagesManager mgr = new DefaultMessagesManager();
        when(componentProvider.getComponent(MessagesManager.class)).thenReturn(mgr);

        RenderContext.push();
        RenderContext.get().setModel(new HashMap<>());

        ServletContext servletContext = mock(ServletContext.class);

        thymeEngine = new SpringTemplateEngine();
        thymeEngine.addTemplateResolver(new ClassLoaderTemplateResolver());
        thymeEngine.addDialect(new MagnoliaDialect());
        renderer = new ThymeleafRenderer();
        renderer.setApplicationContext(webApplicationContext);
        renderer.setServletContext(servletContext);
        renderer.setEngine(thymeEngine);

        renderableDefinition = mock(RenderableDefinition.class);
        renderingContext = mock(RenderingContext.class);
        when(engine.getRenderingContext()).thenReturn(renderingContext);
        RenderableVariationResolver variationResolver = mock(RenderableVariationResolver.class);
        AreaElement areaElement = new AreaElement(config, renderingContext, engine, variationResolver);
        areaElement.setContent(node);
        when(componentProvider.newInstance(eq(AreaElement.class), any())).thenReturn(areaElement);

        stringWriter = new StringWriter();
        AppendableWriter out = new AppendableWriter(stringWriter);
        when(renderingContext.getAppendable()).thenReturn(out);

        BlossomTemplateDefinition templateDefinition = mock(BlossomTemplateDefinition.class);
        when(templateDefinition.getDialog()).thenReturn(null);
        AreaDefinition areaDef = mock(AreaDefinition.class);
        when(areaDef.getName()).thenReturn("Area");
        when(areaDef.getEnabled()).thenReturn(true);
        Map<String, AreaDefinition> areaMap = new HashMap<>();
        areaMap.put("Area",areaDef);
        when(templateDefinition.getAreas()).thenReturn(areaMap);
        when(renderingContext.getRenderableDefinition()).thenReturn(templateDefinition);
    }

    @After
    public void cleanup(){
        Components.popProvider();
        RenderContext.pop();
    }
}
