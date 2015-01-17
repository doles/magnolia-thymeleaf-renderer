package de.eiswind.magnolia.thymeleaf.controller.templates;

import de.eiswind.magnolia.thymeleaf.controller.components.ThymeleafComponent;
import info.magnolia.module.blossom.annotation.Area;
import info.magnolia.module.blossom.annotation.AvailableComponentClasses;
import info.magnolia.module.blossom.annotation.Inherits;
import info.magnolia.module.blossom.annotation.TabFactory;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.ui.form.config.TabBuilder;
import info.magnolia.ui.framework.config.UiConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Sample template controller.
 */
@Template(id = "thymeleaf_proto:pages/mainTemplate", title = "Main Template")
@Controller
public final class MainTemplate {

    /**
     * handles the template.
     *
     * @return the template.
     */
    @RequestMapping("/mainTemplate")
    public String handleRequest() {
        return "templates/main.html";
    }

    /**
     * create the tab.
     *
     * @param cfg the cfg
     * @param tab the tab
     */
    @TabFactory("Properties")
    public void createTab(final UiConfig cfg, final TabBuilder tab) {
        tab.fields(
                cfg.fields.text("test").label("Test")
        );
    }


    /**
     * sample area controller.
     */
    @Area("Area")
    @Inherits
    @AvailableComponentClasses({ThymeleafComponent.class})
    @Controller
    public static final class PromosArea {

        /**
         * handles the area.
         *
         * @return the area fragment
         */
        @RequestMapping("/mainTemplate/promos")
        public String render() {

            return "areas/area.html :: mainArea";
        }
    }
}
