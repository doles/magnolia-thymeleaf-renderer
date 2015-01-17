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

import info.magnolia.context.MgnlContext;
import info.magnolia.module.blossom.render.RenderContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.renderer.AbstractRenderer;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.util.AppendableWriter;
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.templating.jsp.cmsfn.JspTemplatingFunction;
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

/**
 * mgnl renderer for thymeleaf.
 */
public final class ThymeleafRenderer extends AbstractRenderer implements ServletContextAware, ApplicationContextAware {


    //private final Logger log = LoggerFactory.getLogger(getClass());

    private SpringTemplateEngine engine;

    private ApplicationContext applicationContext;


    private ServletContext servletContext;


    /**
     * Constructs a Renderer that uses Thymeleaf.
     */
    public ThymeleafRenderer() {
        super(Components.getComponent(RenderingEngine.class));
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void onRender(final Node content, final RenderableDefinition definition, final RenderingContext renderingCtx,
                            final Map<String, Object> ctx, final String templateScript) throws RenderException {

        Map<String, Object> vars = new HashMap<>(ctx);
        vars.put("content", JspTemplatingFunction.asContentMap(content));
        vars.put("cmsfn", Components.getComponent(TemplatingFunctions.class));

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
        } catch (IOException x) {
            throw new RenderException(x);
        }


    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, Object> newContext() {
        return new HashMap<String, Object>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String resolveTemplateScript(final Node content, final RenderableDefinition definition,
                                           final RenderingModel<?> model, final String actionResult) {
        return RenderContext.get().getTemplateScript();
    }


    public SpringTemplateEngine getEngine() {
        return engine;
    }

    public void setEngine(final SpringTemplateEngine engine1) {
        this.engine = engine1;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(final ServletContext servletContext1) {
        this.servletContext = servletContext1;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(final ApplicationContext applicationContext1) {
        this.applicationContext = applicationContext1;
    }
}
