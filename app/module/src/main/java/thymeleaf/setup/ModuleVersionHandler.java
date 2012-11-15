package thymeleaf.setup;

import info.magnolia.module.DefaultModuleVersionHandler;


import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.inplacetemplating.setup.TemplatesInstallTask;

/**
 * This class is optional and lets you manager the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
public class ModuleVersionHandler extends DefaultModuleVersionHandler {

    public ModuleVersionHandler(){
        Delta delta = DeltaBuilder.update("1.0.3", "")

        .addTask(new TemplatesInstallTask("/thymeleaf_proto/.*\\.html", true));
        register(delta);
    }

}