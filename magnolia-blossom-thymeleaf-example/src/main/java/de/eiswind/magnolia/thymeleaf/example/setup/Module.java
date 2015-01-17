package de.eiswind.magnolia.thymeleaf.example.setup;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.blossom.module.BlossomModuleSupport;

/**
 * this module handles blossom.
 */
public final class Module extends BlossomModuleSupport implements ModuleLifecycle {

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final ModuleLifecycleContext moduleLifecycleContext) {
        initRootWebApplicationContext("classpath:/applicationContext.xml");
        initBlossomDispatcherServlet("blossom", "classpath:/blossom-servlet.xml");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(final ModuleLifecycleContext moduleLifecycleContext) {
        destroyDispatcherServlets();
        closeRootWebApplicationContext();
    }

}
