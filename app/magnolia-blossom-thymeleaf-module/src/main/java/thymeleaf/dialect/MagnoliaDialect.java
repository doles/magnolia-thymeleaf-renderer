package thymeleaf.dialect;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;
import thymeleaf.processor.CmsAreaElementProcessor;
import thymeleaf.processor.CmsComponentElementProcessor;
import thymeleaf.processor.CmsInitElementProcessor;

import javax.servlet.ServletContext;
import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10.11.12
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class MagnoliaDialect extends AbstractDialect implements ApplicationContextAware,ServletContextAware {

    private ApplicationContext ctx;
    private ServletContext servletContext;

    @Override
    public String getPrefix() {
        return "cms";
    }

    @Override
    public boolean isLenient() {
        return false;
    }

    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new CmsInitElementProcessor());
        processors.add(new CmsAreaElementProcessor());
        processors.add(new CmsComponentElementProcessor());
        return processors;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
       this.ctx = applicationContext;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
