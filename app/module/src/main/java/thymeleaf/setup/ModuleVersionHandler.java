package magnoliagroovyproto.setup;

import info.magnolia.module.DefaultModuleVersionHandler;



import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.delta.BootstrapSingleModuleResource;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.groovy.setup.InstallGroovyFile;
import info.magnolia.module.inplacetemplating.setup.TemplatesInstallTask;

/**
 * This class is optional and lets you manager the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
public class ModuleVersionHandler extends DefaultModuleVersionHandler {

    public ModuleVersionHandler(){
        InstallGroovyFile groovyTask = new InstallGroovyFile("GroovyScripts","Installs groovy scripts",".*\\.groovy");
        Delta delta = DeltaBuilder.update("1.0.12", "")
         .addTask(groovyTask)
        .addTask(new BootstrapSingleModuleResource("template definition added.", "", "config.modules.standard-templating-kit.templates.components.footer.t8yFooterAbout.xml"))
        .addTask(new TemplatesInstallTask("/groovy-proto-templates/.*\\.ftl", true));
        register(delta);
    }

}