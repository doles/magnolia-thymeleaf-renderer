package thymeleaf.dialect;

import org.thymeleaf.dialect.AbstractDialect;
import org.thymeleaf.processor.IProcessor;
import thymeleaf.processor.CmsAreaElementProcessor;
import thymeleaf.processor.CmsComponentElementProcessor;
import thymeleaf.processor.CmsInitElementProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 10.11.12
 * Time: 12:18
 * To change this template use File | Settings | File Templates.
 */
public class MagnoliaDialect extends AbstractDialect {


    @Override
    public String getPrefix() {
        return "cms";
    }



    @Override
    public Set<IProcessor> getProcessors() {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new CmsInitElementProcessor());
        processors.add(new CmsAreaElementProcessor());
        processors.add(new CmsComponentElementProcessor());
        return processors;
    }


}
