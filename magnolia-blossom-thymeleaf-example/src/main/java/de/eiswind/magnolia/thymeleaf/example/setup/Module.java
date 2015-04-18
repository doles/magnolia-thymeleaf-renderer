package de.eiswind.magnolia.thymeleaf.example.setup;

import de.eiswind.magnolia.springloaded.BlossomReloadPlugin;
import de.eiswind.magnolia.thymeleaf.example.configuration.BlossomServletConfiguration;
import de.eiswind.magnolia.thymeleaf.example.configuration.SampleApplicationConfiguration;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.blossom.module.BlossomModuleSupport;
import info.magnolia.module.blossom.render.BlossomDispatcherServlet;

/**
 * this module handles blossom.
 */
public final class Module extends BlossomModuleSupport implements ModuleLifecycle {

    /**
     * {@inheritDoc}
     */
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        if (moduleLifecycleContext.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_STARTUP) {

            // Using Spring java config
            super.initRootWebApplicationContext(SampleApplicationConfiguration.class);
            super.initBlossomDispatcherServlet("blossom", BlossomServletConfiguration.class);

            new BlossomReloadPlugin((BlossomDispatcherServlet)getDispatcherServlets().get(0));
/*
            // Using Spring xml config
            super.initRootWebApplicationContext("classpath:/applicationContext.xml");
            super.initBlossomDispatcherServlet("blossom", "classpath:/blossom-servlet.xml");
*/
        }
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        if (moduleLifecycleContext.getPhase() == ModuleLifecycleContext.PHASE_SYSTEM_SHUTDOWN) {
            super.destroyDispatcherServlets();
            super.closeRootWebApplicationContext();
        }
    }

}
