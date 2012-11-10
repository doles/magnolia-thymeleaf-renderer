package thymeleaf.controller.templates;

import info.magnolia.module.blossom.annotation.Area;
import info.magnolia.module.blossom.annotation.AvailableComponentClasses;
import info.magnolia.module.blossom.annotation.Inherits;
import info.magnolia.module.blossom.annotation.Template;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import thymeleaf.controller.components.JspComponent;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10.11.12
 * Time: 08:05
 * To change this template use File | Settings | File Templates.
 */
@Template(id = "thymeleaf_proto:pages/jspTemplate", title = "JSP Template")
@Controller
public class JspTemplate {

    @RequestMapping("/jspTemplate")
    public String handleRequest() {
        return "templates/test.jsp";
    }

    @Area("Area")
    @Inherits
    @AvailableComponentClasses({JspComponent.class})
    @Controller
    public static class PromosArea {

        @RequestMapping("/mainTemplate/area")
        public String render() {
            return "areas/area.jsp";
        }
    }
}
