package thymeleaf.renderer;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.blossom.render.RenderContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.renderer.AbstractRenderer;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.util.AppendableWriter;
import info.magnolia.templating.jsp.cmsfn.JspTemplatingFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.support.RequestContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.ProcessingContext;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.naming.SpringContextVariableNames;
import org.thymeleaf.standard.fragment.StandardFragment;
import org.thymeleaf.standard.fragment.StandardFragmentProcessor;
import org.thymeleaf.standard.processor.attr.StandardFragmentAttrProcessor;

import javax.jcr.Node;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ThymeleafRenderer extends AbstractRenderer implements ServletContextAware, ApplicationContextAware {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private SpringTemplateEngine engine;

    private ApplicationContext applicationContext;


    private ServletContext servletContext;


    /**
     * Constructs a FreemarkerTemplateRenderer that uses the default (singleton)
     * instance of FreemarkerHelper.
     */
    public ThymeleafRenderer() {

    }


    @Override
    protected void onRender(Node content, RenderableDefinition definition,RenderingContext renderingCtx,  Map<String, Object> ctx, String templateScript) throws RenderException {

        Map<String, Object> vars = new HashMap<String, Object>(ctx);
        vars.put("content", JspTemplatingFunction.asContentMap(content));
        vars.put("renderingContext",renderingCtx);
        vars.put("cmsfn", new JspTemplatingFunction());

        final HttpServletRequest request = MgnlContext.getWebContext().getRequest();
        final HttpServletResponse response = MgnlContext.getWebContext().getResponse();

        // setup spring request context in spring web context
        final RequestContext requestContext = new RequestContext(request, response, servletContext, vars);
        vars.put(SpringContextVariableNames.SPRING_REQUEST_CONTEXT, requestContext);

        // copy all spring model attributes into the spring web context as variables
        vars.putAll(RenderContext.get().getModel());

        final IWebContext context = new SpringWebContext(request, response, servletContext, MgnlContext.getWebContext()
                .getRequest().getLocale(), vars, getApplicationContext());

        try (AppendableWriter out = renderingCtx.getAppendable()) {
            // need to ensure engine initialised before getting configuration
            if (!engine.isInitialized()) {
                engine.initialize();
            }
            // allow template fragment syntax to be used e.g. template.html :: area
            final StandardFragment fragment = StandardFragmentProcessor.computeStandardFragmentSpec(
                    engine.getConfiguration(), new ProcessingContext(context), templateScript, null, "th:"
                            + StandardFragmentAttrProcessor.ATTR_NAME);

            // and pass the fragment name and spec then onto the engine
            engine.process(fragment.getTemplateName(), context, fragment.getFragmentSpec(), out);
        }catch(IOException x) {
            throw new RenderException(x);
        }


    }

    @Override
    protected Map<String, Object> newContext() {
        return new HashMap<String, Object>();
    }

    @Override
    protected String resolveTemplateScript(Node content, RenderableDefinition definition, RenderingModel<?> model, String actionResult) {
        return RenderContext.get().getTemplateScript();
    }


    public SpringTemplateEngine getEngine() {
        return engine;
    }

    public void setEngine(SpringTemplateEngine engine) {
        this.engine = engine;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
