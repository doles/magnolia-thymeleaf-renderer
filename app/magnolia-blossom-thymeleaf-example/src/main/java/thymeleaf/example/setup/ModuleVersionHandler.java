package thymeleaf.example.setup;

import info.magnolia.module.DefaultModuleVersionHandler;


import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.inplacetemplating.setup.TemplatesInstallTask;

import java.util.List;

/**
 * This class is optional and lets you manager the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
public class ModuleVersionHandler extends DefaultModuleVersionHandler {

    public ModuleVersionHandler(){
        Delta delta = DeltaBuilder.update("1.0.4", "")
        .addTask(new TemplatesInstallTask("/thymeleaf_proto/.*\\.html", true));
        register(delta);
    }

    @Override
    protected List<Task> getBasicInstallTasks(InstallContext installContext) {
        List<Task> install = super.getBasicInstallTasks(installContext);
        install.add(new TemplatesInstallTask("/thymeleaf_proto/.*\\.html", true));
        return install;
    }
    
}