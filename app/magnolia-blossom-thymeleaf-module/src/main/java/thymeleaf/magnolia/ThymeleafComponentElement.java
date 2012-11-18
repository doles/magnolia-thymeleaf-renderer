package thymeleaf.magnolia;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.jcr.inheritance.InheritanceNodeWrapper;
import info.magnolia.module.blossom.template.BlossomTemplateDefinition;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.assignment.TemplateDefinitionAssignment;
import info.magnolia.templating.elements.ComponentElement;
import info.magnolia.templating.elements.MarkupHelper;
import info.magnolia.templating.freemarker.AreaDirective;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.exceptions.TemplateProcessingException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: Thomas
 * Date: 12.11.12
 * Time: 20:12
 * To change this template use File | Settings | File Templates.
 */
public class ThymeleafComponentElement extends ComponentElement{

    private static final Logger log = LoggerFactory.getLogger(ThymeleafComponentElement.class);
    private Node content;
    private TemplateDefinitionAssignment templateDefinitionAssignment2;
    private TemplateDefinition componentDefinition2;
    private Boolean editable2;
    private String dialog2;

    public ThymeleafComponentElement(ServerConfiguration server, RenderingContext renderingContext, RenderingEngine renderingEngine, TemplateDefinitionAssignment templateDefinitionAssignment) {
        super(server, renderingContext, renderingEngine, templateDefinitionAssignment);
        this.templateDefinitionAssignment2 = templateDefinitionAssignment;
    }

    public String createComment(){

        StringWriter out = new StringWriter();
        try {
            content = getPassedContent();

            if(content == null) {
                throw new RenderException("The 'content' or 'workspace' and 'path' attribute have to be set to render a component.");
            }

            if(isAdmin() && hasPermission(content)){

                try {
                    this.componentDefinition2 = templateDefinitionAssignment2.getAssignedTemplateDefinition(content);
                } catch (RegistrationException e) {
                    throw new RenderException("No template definition found for the current content", e);
                }

                final Messages messages = MessagesManager.getMessages(componentDefinition2.getI18nBasename());

                if (isRenderEditbar()) {
                    MarkupHelper helper = new MarkupHelper(out);

                    out.write("cms:component");

                    helper.attribute(AreaDirective.CONTENT_ATTRIBUTE, getNodePath(content));

                    if(content instanceof InheritanceNodeWrapper) {
                        if (((InheritanceNodeWrapper) content).isInherited()) {
                            helper.attribute("inherited", "true");
                        }
                    }

                    this.editable2 = resolveEditable();
                    if (this.editable2 != null) {
                        helper.attribute("editable", String.valueOf(this.editable2));
                    }

                    if(StringUtils.isEmpty(dialog2)) {
                        dialog2 = resolveDialog();
                    }
                    helper.attribute("dialog", dialog2);

                    String label = StringUtils.defaultIfEmpty(componentDefinition2.getTitle(),componentDefinition2.getName());
                    helper.attribute("label", messages.getWithDefault(label, label));

                    if(StringUtils.isNotEmpty(componentDefinition2.getDescription())){
                        helper.attribute("description", componentDefinition2.getDescription());
                    }

                }
            }
        } catch (Exception e) {
            throw new TemplateProcessingException("Cant create comment",e);
        }

        return out.toString();
    }
    private boolean hasPermission(Node node) {
        try {
            return node.getSession().hasPermission(node.getPath(), Session.ACTION_SET_PROPERTY);
        } catch (RepositoryException e) {
            log.error("Could not determine permission for node {}", node);
        }
        return false;
    }

    private Boolean resolveEditable() {
        return editable2 != null ? editable2 : componentDefinition2 != null && componentDefinition2.getEditable() != null ? componentDefinition2.getEditable() : null;
    }

    private String resolveDialog() {
        if (StringUtils.isNotEmpty(this.dialog2)) {
            return this.dialog2;
        }
        String dialog = componentDefinition2.getDialog();
        if (StringUtils.isNotEmpty(dialog)) {
            return dialog;
        }
        return null;
    }

    public BlossomTemplateDefinition getTemplate(Node content) throws RegistrationException {
        return (BlossomTemplateDefinition)templateDefinitionAssignment2.getAssignedTemplateDefinition(content);

    }
}
