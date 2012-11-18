package thymeleaf.example.setup;

import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.module.blossom.module.BlossomModuleSupport;

/**
 * Created with IntelliJ IDEA.
 * User: tkratz
 * Date: 09.07.12
 * Time: 16:39
 * To change this template use File | Settings | File Templates.
 */
public class Module extends BlossomModuleSupport implements ModuleLifecycle {
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        initRootWebApplicationContext("classpath:/applicationContext.xml");
        initBlossomDispatcherServlet("blossom", "classpath:/blossom-servlet.xml");
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
        destroyDispatcherServlets();
        closeRootWebApplicationContext();
    }

}
