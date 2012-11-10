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
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.spring3.SpringTemplateEngine;
import org.thymeleaf.spring3.context.SpringWebContext;

import javax.jcr.Node;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
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
        final Locale locale = MgnlContext.getAggregationState().getLocale();

            Map<String, Object> vars = new HashMap<String, Object>();
            vars.put("content", content);
            vars.put("renderingContext",renderingCtx);
            vars.put("cmsfn", new JspTemplatingFunction());
            final IWebContext context =
                    new SpringWebContext(MgnlContext.getWebContext().getRequest(), MgnlContext.getWebContext().getResponse(), servletContext , MgnlContext.getWebContext().getRequest().getLocale(), vars, getApplicationContext());

        try {
            AppendableWriter out = renderingCtx.getAppendable();
            engine.process(templateScript, context, out);
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
