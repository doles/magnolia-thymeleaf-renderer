package de.eiswind.magnolia.thymeleaf.example.setup;

import info.magnolia.module.DefaultModuleVersionHandler;

/**
 * This class is optional and lets you manager the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
public class ModuleVersionHandler extends DefaultModuleVersionHandler {

    public ModuleVersionHandler(){
//        Delta delta = DeltaBuilder.update("1.0.4", "")
//        .addTask(new TemplatesInstallTask("/thymeleaf_proto/.*\\.html", true));
//        register(delta);
    }


    
}
