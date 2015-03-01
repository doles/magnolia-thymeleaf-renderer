package de.eiswind.magnolia.thymeleaf.controller.components;

import info.magnolia.module.blossom.annotation.TabFactory;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.ui.form.config.TabBuilder;
import info.magnolia.ui.framework.config.UiConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Sample Teaser Controller.
 */
@Template(id = "thymeleaf_proto:components/teaserComponent", title = "Teaser Component")
@Controller
public final class TeaserComponent {

    /**
     * get the template fragment.
     */
    @RequestMapping("/teaserComponent")
    public String handleRequest() {
        return "templates/main.html :: component";
    }

    /**
     * create the tab.
     */
    @TabFactory("Properties")
    public void createTab(final UiConfig cfg, final TabBuilder tab) {
        tab.fields(
                cfg.fields.text("heading").label("Heading"),
                cfg.fields.text("text").label("Text")
        );
    }
}
