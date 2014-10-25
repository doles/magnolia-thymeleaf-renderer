package thymeleaf.controller.components;

import info.magnolia.module.blossom.annotation.TabFactory;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.ui.form.config.TabBuilder;
import info.magnolia.ui.framework.config.UiConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.jcr.Node;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10.11.12
 * Time: 14:39
 * To change this template use File | Settings | File Templates.
 */
@Template(id = "thymeleaf_proto:components/thymeleafComponent", title = "Thymeleaf Component")
@Controller

public class ThymeleafComponent {


    @RequestMapping("/thymeleafComponent")
    public String handleRequest(Node content) {
        System.out.println("Node:"+ content);
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
