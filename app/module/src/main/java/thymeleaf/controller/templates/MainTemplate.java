package thymeleaf.controller.templates;

import info.magnolia.module.blossom.annotation.Template;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10.11.12
 * Time: 08:05
 * To change this template use File | Settings | File Templates.
 */
@Template(id = "thymeleaf_proto:pages/mainTemplate", title = "Main Template")
@Controller
public class MainTemplate {

    @RequestMapping("/mainTemplate")
    public String handleRequest() {
        return "templates/main.html";
    }
}
