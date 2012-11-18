package thymeleaf.resolver;

import info.magnolia.cms.core.Content;
import info.magnolia.module.inplacetemplating.JcrRepoTemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateProcessingParameters;
import org.thymeleaf.context.IContext;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.resourceresolver.IResourceResolver;
import org.thymeleaf.util.Validate;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: tkratz
 * Date: 15.11.12
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public class InplaceTemplateResourceResolver implements IResourceResolver {

    private static final Logger log = LoggerFactory.getLogger(InplaceTemplateResourceResolver.class);
    public static final String NAME = "MAGNOLIASERVLETCONTEXT";
    public static final String WEB_INF_CLASSES = "/WEB-INF/classes";

    private JcrRepoTemplateLoader templateLoader = new JcrRepoTemplateLoader();

    public InplaceTemplateResourceResolver() {
        super();
        templateLoader.setLocaleAware(false);
        templateLoader.setExtension(".html");
    }


    public String getName() {
        return NAME;
    }




    public InputStream getResourceAsStream(final TemplateProcessingParameters templateProcessingParameters, final String resourceName) {

        Validate.notNull(templateProcessingParameters, "Template Processing Parameters cannot be null");
        Validate.notNull(resourceName, "Resource name cannot be null");

        final IContext context = templateProcessingParameters.getContext();
        if (!(context instanceof IWebContext)) {
            throw new TemplateProcessingException(
                    "Resource resolution by ServletContext with " +
                            this.getClass().getName() + " can only be performed " +
                            "when context implements " + IWebContext.class.getName() +
                            " [current context: " + context.getClass().getName() + "]");
        }

        try {
            String repoPath;
            if(resourceName.startsWith(WEB_INF_CLASSES)){
                    repoPath = resourceName.substring(WEB_INF_CLASSES.length());
            } else {
                repoPath = resourceName;
            }

            Content template = (Content)templateLoader.findTemplateSource(repoPath);
            if(template != null){
                Value value = template.getNodeData("text").getValue();
                return value.getStream();
            }
        } catch (Exception e) {
            log.error("Error reading template from repository",e);
        }

        final ServletContext servletContext =
                ((IWebContext)context).getServletContext();
        if (servletContext == null) {
            throw new TemplateProcessingException("Thymeleaf context returned a null ServletContext");
        }

        return servletContext.getResourceAsStream(resourceName);

    }
}
