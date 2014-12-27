package de.eiswind.magnolia.thymeleaf.controller.components;

import info.magnolia.module.blossom.annotation.TabFactory;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.ui.form.config.TabBuilder;
import info.magnolia.ui.framework.config.UiConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10.11.12
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
@Template(id = "thymeleaf_proto:components/thymeleafComponent2", title = "Thymeleaf Component2")
@Controller

public class ThymeleafComponent2 {


    @RequestMapping("/thymeleafComponent2")
    public String handleRequest() {
        return "templates/main.html :: component2";
    }
    @TabFactory("Properties")
    public void createTab(UiConfig cfg,TabBuilder tab) {
        tab.fields(
                cfg.fields.text("head").label("Head"),
                cfg.fields.text("text").label("Text"),
                cfg.fields.text("button").label("Button")
        );
    }

}
