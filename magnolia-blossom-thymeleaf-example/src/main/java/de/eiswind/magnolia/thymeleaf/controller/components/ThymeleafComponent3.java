package de.eiswind.magnolia.thymeleaf.controller.components;

import info.magnolia.module.blossom.annotation.TabFactory;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.ui.form.config.TabBuilder;
import info.magnolia.ui.framework.config.UiConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.jcr.Node;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10.11.12
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
@Template(id = "thymeleaf_proto:components/thymeleafComponent3", title = "Thymeleaf Component3")
@Controller

public class ThymeleafComponent3 {


    @RequestMapping(value="/thymeleafComponent3", method = RequestMethod.GET)
    public String handleRequest3(Node content) {
//        System.out.println("Node:"+ content);
        return "templates/main.html :: component";
    }

    @TabFactory("Properties")
    public void createTab(UiConfig cfg,TabBuilder tab) {
        tab.fields(
                cfg.fields.text("head").label("Head"),
                cfg.fields.text("text").label("Text")
        );
    }
}
