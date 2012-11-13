package thymeleaf.controller.components;

import info.magnolia.module.blossom.annotation.TabFactory;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.module.blossom.dialog.TabBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String handleRequest() {
        return "components/component.html :: component";
    }

    @TabFactory("Properties")
    public void createTab(TabBuilder builder) {
        builder.addEdit("head", "head", "");
        builder.addEdit("text", "Testtext", "");
    }
}
