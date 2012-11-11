package thymeleaf.blossom;

import info.magnolia.module.blossom.annotation.Area;
import info.magnolia.module.blossom.annotation.Template;
import info.magnolia.module.blossom.dialog.BlossomDialogDescription;
import info.magnolia.module.blossom.dialog.BlossomDialogRegistry;
import info.magnolia.module.blossom.dialog.DialogDescriptionBuilder;
import info.magnolia.module.blossom.dispatcher.BlossomDispatcher;
import info.magnolia.module.blossom.dispatcher.BlossomDispatcherAware;
import info.magnolia.module.blossom.dispatcher.BlossomDispatcherInitializedEvent;
import info.magnolia.module.blossom.support.AbstractUrlMappedHandlerPostProcessor;
import info.magnolia.module.blossom.template.*;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.RepositoryException;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tkratz
 * Date: 11.11.12
 * Time: 10:59
 * To change this template use File | Settings | File Templates.
 */
public class ThymeleafTemplateExporter extends AbstractUrlMappedHandlerPostProcessor implements InitializingBean, ApplicationListener, BlossomDispatcherAware {

    private static final String TEMPLATE_DIALOG_PREFIX = "blossom-template-dialog:";
    private static final String AREA_DIALOG_PREFIX = "blossom-area-dialog:";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BlossomDispatcher dispatcher;
    private TemplateDefinitionBuilder templateDefinitionBuilder;
    private DialogDescriptionBuilder dialogDescriptionBuilder;

    private DetectedHandlersMetaData detectedHandlers = new DetectedHandlersMetaData();


    public void setTemplateDefinitionBuilder(TemplateDefinitionBuilder templateDefinitionBuilder) {
        this.templateDefinitionBuilder = templateDefinitionBuilder;
    }

    public void setDialogDescriptionBuilder(DialogDescriptionBuilder dialogDescriptionBuilder) {
        this.dialogDescriptionBuilder = dialogDescriptionBuilder;
    }


    public void setBlossomDispatcher(BlossomDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void postProcessHandler(Object handler, String handlerPath) {
        Class<?> handlerClass = AopUtils.getTargetClass(handler);
        if (handlerClass.isAnnotationPresent(Area.class)) {
            detectedHandlers.addArea(new HandlerMetaData(handler, handlerPath, handlerClass));
        } else if (handlerClass.isAnnotationPresent(Template.class)) {
            detectedHandlers.addTemplate(new HandlerMetaData(handler, handlerPath, handlerClass));
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof BlossomDispatcherInitializedEvent && event.getSource() == dispatcher) {
            exportTemplates();
        }
    }

    protected void exportTemplates() {
        for (HandlerMetaData template : detectedHandlers.getTemplates()) {

            BlossomTemplateDefinition definition = templateDefinitionBuilder.buildTemplateDefinition(dispatcher, detectedHandlers, template);

            Components.getComponent(TemplateDefinitionRegistry.class).register(new BlossomTemplateDefinitionProvider(definition));

            if (StringUtils.isEmpty(definition.getDialog())) {
                registerTemplateDialog(definition);
            }

            registerDialogFactories(definition);

            registerAreaDialogs(definition.getAreas().values());
        }


    }

    protected void registerDialogFactories(BlossomTemplateDefinition templateDefinition) {

        List<BlossomDialogDescription> dialogDescriptions = dialogDescriptionBuilder.buildDescriptions(templateDefinition.getHandler());
        for (BlossomDialogDescription dialogDescription : dialogDescriptions) {
            try {
                Components.getComponent(BlossomDialogRegistry.class).addDialogDescription(dialogDescription);
            } catch (RepositoryException e) {
                logger.error("Unable to register dialog factory within template [" + dialogDescription.getFactoryMetaData().getFactoryMethod() + "] with handlerPath [" + templateDefinition.getHandlerPath() + "]", e);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Registered dialog factory within template [" + templateDefinition.getId() + "] with id [" + dialogDescription.getId() + "]");
            }
        }
    }

    protected void registerTemplateDialog(BlossomTemplateDefinition templateDefinition) {

        String templateId = templateDefinition.getId();

        String dialogId = TEMPLATE_DIALOG_PREFIX + AopUtils.getTargetClass(templateDefinition.getHandler()).getName();

        BlossomDialogDescription dialogDescription = dialogDescriptionBuilder.buildDescription(dialogId, templateDefinition.getTitle(), templateDefinition.getHandler());

        if (dialogDescription.getFactoryMetaData().isEmpty()) {
            return;
        }

        templateDefinition.setDialog(dialogId);

        try {
            Components.getComponent(BlossomDialogRegistry.class).addDialogDescription(dialogDescription);
        } catch (RepositoryException e) {
            logger.error("Failed to register dialog for template [" + templateId + "] with id [" + dialogId + "]", e);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Registered dialog for template [" + templateId + "] with id [" + dialogId + "]");
        }
    }

    protected void registerAreaDialogs(Collection<AreaDefinition> areas) {
        for (AreaDefinition areaDefinition : areas) {
            if (StringUtils.isEmpty(areaDefinition.getDialog())) {
                registerAreaDialog((BlossomAreaDefinition) areaDefinition);
            }
            registerAreaDialogs(areaDefinition.getAreas().values());
        }
    }

    protected void registerAreaDialog(BlossomAreaDefinition areaDefinition) {

        String areaName = areaDefinition.getName();

        String dialogId = AREA_DIALOG_PREFIX + AopUtils.getTargetClass(areaDefinition.getHandler()).getName();

        BlossomDialogDescription dialogDescription = dialogDescriptionBuilder.buildDescription(dialogId, areaDefinition.getTitle(), areaDefinition.getHandler());

        if (dialogDescription.getFactoryMetaData().isEmpty()) {
            return;
        }

        areaDefinition.setDialog(dialogId);

        try {
            Components.getComponent(BlossomDialogRegistry.class).addDialogDescription(dialogDescription);
        } catch (RepositoryException e) {
            logger.error("Failed to register dialog for area [" + areaName + "] with id [" + dialogId + "]", e);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Registered dialog for area [" + areaName + "] with id [" + dialogId + "]");
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (templateDefinitionBuilder == null) {
            templateDefinitionBuilder = new TemplateDefinitionBuilder();
        }
        if (dialogDescriptionBuilder == null) {
            dialogDescriptionBuilder = new DialogDescriptionBuilder();
        }
    }

    public DetectedHandlersMetaData getDetectedHandlers() {
        return detectedHandlers;
    }




}
