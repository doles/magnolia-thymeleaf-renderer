/**
 * This file Copyright (c) 2015 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package de.eiswind.magnolia.thymeleaf.example.configuration;

import de.eiswind.magnolia.thymeleaf.dialect.MagnoliaDialect;
import de.eiswind.magnolia.thymeleaf.renderer.ThymeleafRenderer;
import info.magnolia.cms.beans.config.VirtualURIMapping;
import info.magnolia.module.blossom.annotation.Area;
import info.magnolia.module.blossom.annotation.DialogFactory;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.module.blossom.annotation.VirtualURIMapper;
import info.magnolia.module.blossom.preexecution.BlossomHandlerMapping;
import info.magnolia.module.blossom.view.TemplateViewResolver;
import info.magnolia.module.blossom.view.UuidRedirectViewResolver;
import info.magnolia.module.blossom.web.BlossomHandlerMethodArgumentResolver;
import info.magnolia.module.blossom.web.BlossomRequestMappingHandlerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.thymeleaf.dialect.IDialect;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import java.util.Collections;
import java.util.Set;

/**
 * Configuration class for the blossom servlet housing templates and beans used to render them.
 */
@Configuration
@ComponentScan(
        basePackages = {
                "de.eiswind.magnolia.thymeleaf.controller"},
        includeFilters = {
                @ComponentScan.Filter(Template.class),
                @ComponentScan.Filter(Area.class),
                @ComponentScan.Filter(DialogFactory.class),
                @ComponentScan.Filter(VirtualURIMapper.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = VirtualURIMapping.class)
        })
public class BlossomServletConfiguration {

    private SpringTemplateEngine templateEngine;
    private ITemplateResolver templateResolver;

    /**
     * Handler adapter for &#64;RequestMapping style handler methods. Uses BlossomRequestMappingHandlerAdapter for
     * support of flash attributes in uuid redirect views.
     */
    @Bean
    public HandlerAdapter handlerAdapter() {

        BlossomRequestMappingHandlerAdapter handlerAdapter = new BlossomRequestMappingHandlerAdapter();
        handlerAdapter.setRedirectPatterns("website:*");

        handlerAdapter.setCustomArgumentResolvers(Collections.<HandlerMethodArgumentResolver>singletonList(new BlossomHandlerMethodArgumentResolver()));

        // For @Valid - JSR-303 Bean Validation API -->
        ConfigurableWebBindingInitializer bindingInitializer = new ConfigurableWebBindingInitializer();
        bindingInitializer.setValidator(validatorFactory());
        handlerAdapter.setWebBindingInitializer(bindingInitializer);

        return handlerAdapter;
    }

    @Bean
    public LocalValidatorFactoryBean validatorFactory() {
        return new LocalValidatorFactoryBean();
    }

    /**
     * Handler adapter for Controller interface style controllers. Required because these are used internally by the
     * pre-execution mechanism.
     */
    @Bean
    public HandlerAdapter controllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    /**
     * Handler mapping that delegates to other handler mappings. Required by the pre-execution mechanism and must be
     * ordered first.
     */
    @Bean
    public HandlerMapping blossomHandlerMapping() {
        BlossomHandlerMapping handlerMapping = new BlossomHandlerMapping();
        handlerMapping.setOrder(1);
        handlerMapping.setTargetHandlerMappings(new HandlerMapping[]{mappingHandlerMapping()});
        return handlerMapping;
    }

    /**
     * Handler mapping for &#64;RequestMapping style handler methods.
     */
    @Bean
    public RequestMappingHandlerMapping mappingHandlerMapping() {
        RequestMappingHandlerMapping handlerMapping = new RequestMappingHandlerMapping();
        handlerMapping.setOrder(2);
        handlerMapping.setUseSuffixPatternMatch(false);
        return handlerMapping;
    }

    /**
     * View resolver for uuid redirect views.
     */
    @Bean
    public UuidRedirectViewResolver uuidRedirectViewResolver() {
        UuidRedirectViewResolver resolver = new UuidRedirectViewResolver();
        resolver.setOrder(1);
        return resolver;
    }

//    /**
//     * View resolver for JSP views.
//     */
//    @Bean
//    public TemplateViewResolver jspTemplateViewResolver() {
//        TemplateViewResolver resolver = new TemplateViewResolver();
//        resolver.setOrder(2);
//        resolver.setPrefix("/templates/blossomSampleModule/");
//        resolver.setViewNames("*.jsp");
//        JspTemplateViewRenderer viewRenderer = new JspTemplateViewRenderer();
//        viewRenderer.addContextAttribute("damfn", DamTemplatingFunctions.class);
//        resolver.setViewRenderer(viewRenderer);
//        return resolver;
//    }

    /**
     * View resolver for Thymeleaf views.
     */
    @Bean
    public TemplateViewResolver thymeleafTemplateViewResolver() {
        TemplateViewResolver resolver = new TemplateViewResolver();
        resolver.setOrder(3);
        resolver.setPrefix("");
        resolver.setViewNames("*.html*");

        resolver.setViewRenderer(getThymeleafViewRenderer());
        return resolver;
    }

    @Bean
    public ThymeleafRenderer getThymeleafViewRenderer(){
        ThymeleafRenderer viewRenderer = new ThymeleafRenderer();
        viewRenderer.setEngine(getTemplateEngine());
        return viewRenderer;
    }

    @Bean
    public SpringTemplateEngine getTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(getTemplateResolver());
        Set<IDialect> dialects = Collections.singleton(new MagnoliaDialect());
        engine.setAdditionalDialects(dialects);
        return engine;
    }

    @Bean
    public ITemplateResolver getTemplateResolver() {
        ServletContextTemplateResolver resolver = new ServletContextTemplateResolver();
        resolver.setPrefix("/WEB-INF/classes/thymeleaf_proto/");
        resolver.setSuffix("");
        resolver.setTemplateMode("HTML5");
        resolver.setCacheable(false); // TODO make this configurable for prod deployments
        return resolver;
    }
}
