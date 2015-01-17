package de.eiswind.magnolia.thymeleaf.controller.components;

import info.magnolia.module.blossom.annotation.TabFactory;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.ui.form.config.TabBuilder;
import info.magnolia.ui.framework.config.UiConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Sample Component Controller.
 */
@Template(id = "thymeleaf_proto:components/thymeleafComponent2", title = "Thymeleaf Component2")
@Controller
public final class ThymeleafComponent2 {

    /**
     * get the template fragment.
     *
     * @return the fragment
     */
    @RequestMapping("/thymeleafComponent2")
    public String handleRequest() {
        return "templates/main.html :: component2";
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
                cfg.fields.text("head").label("Head"),
                cfg.fields.text("text").label("Text"),
                cfg.fields.text("button").label("Button")
        );
    }

}
