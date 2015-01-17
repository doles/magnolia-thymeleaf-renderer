/*
 * Copyright (c) 2014 Thomas Kratz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.eiswind.magnolia.thymeleaf.processor;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.security.Permission;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.objectfactory.Components;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.templating.elements.MarkupHelper;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Comment;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;
import org.thymeleaf.dom.Text;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.processor.attr.AbstractChildrenModifierAttrProcessor;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * initlializes mgnl stuff on the page.
 */
public final class CmsInitElementProcessor extends AbstractChildrenModifierAttrProcessor {

    private I18nContentSupport i18nContentSupport = Components.getComponent(I18nContentSupport.class);

    private static final String CMS_PAGE_TAG = "cms:page";
    /**
     * name of the content attribute.
     */
    public static final String CONTENT_ATTRIBUTE = "content";

    private static final String[] js = new String[]{"/.magnolia/pages/javascript.js", "/.magnolia/pages/messages.en.js", "/.resources/admin-js/dialogs/dialogs.js", "/.resources/calendar/calendar.js", "/.resources/calendar/calendar-setup.js", "/.resources/editor/info.magnolia.templating.editor.PageEditor/info.magnolia.templating.editor.PageEditor.nocache.js"};
    private static final String[] css = new String[]{"/.resources/admin-css/admin-all.css", "/.resources/magnolia-templating-editor/css/editor.css", "/.resources/calendar/skins/aqua/theme.css"};

    /**
     * create an instance.
     */
    public CmsInitElementProcessor() {
        super("init");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Node> getModifiedChildren(Arguments arguments, Element element, String attributeName)

    {
        AggregationState aggregationState = MgnlContext.getAggregationState();

        javax.jcr.Node activePage = aggregationState.getMainContentNode();

        ServerConfiguration config = Components.getComponent(ServerConfiguration.class);
        boolean isAdmin = config.isAdmin()
                && !aggregationState.isPreviewMode()
                && activePage != null
                && NodeUtil.isGranted(activePage, Permission.SET);

        if (!isAdmin) {
            return element.getChildren();
        }

        String name = element.getNormalizedName();
        if (!"head".equals(name)) {
            throw new TemplateProcessingException("cms:init is only allowed on head element");
        }
        List<Node> result = new ArrayList<>(element.getChildren());
        Element el = new Element("meta");
        el.setAttribute("gwt:property", "locale=" + i18nContentSupport.getLocale());
        result.add(el);
        String ctx = MgnlContext.getContextPath();
        for (String sheet : css) {
            el = new Element("link");
            el.setAttribute("rel", "stylesheet");
            el.setAttribute("type", "text/css");
            el.setAttribute("href", ctx + sheet);
            result.add(el);
            Text t = new Text("\n");
            result.add(t);
        }
        for (String script : js) {
            el = new Element("script");
            el.setAttribute("type", "text/javascript");
            el.setAttribute("src", ctx + script);
            result.add(el);
            Text t = new Text("\n");
            result.add(t);
        }
        el = new Element("script");
        el.setAttribute("type", "text/javascript");
        el.setAttribute("src", ctx + "/.resources/calendar/lang/calendar-" + MgnlContext.getLocale().getLanguage() + ".js");
        result.add(el);
        Text t = new Text("\n");
        result.add(t);
        StringWriter writer = new StringWriter();
        MarkupHelper helper = new MarkupHelper(writer);
        try {
            helper.append(" " + CMS_PAGE_TAG);
            helper.attribute(CONTENT_ATTRIBUTE, getNodePath(activePage));

            final RenderingEngine renderingEngine = Components.getComponent(RenderingEngine.class);
            final RenderingContext renderingContext = renderingEngine.getRenderingContext();
            TemplateDefinition templateDefinition = (TemplateDefinition) renderingContext.getRenderableDefinition();
            String dlg = templateDefinition.getDialog();
            if (dlg != null) {
                helper.attribute("dialog", dlg);
            }
            helper.attribute("preview", String.valueOf(MgnlContext.getAggregationState().isPreviewMode()));

            //here we provide the page editor with the available locales and their respective URI for the current page
//            if (i18nAuthoringSupport.isEnabled() && i18nContentSupport.isEnabled() && i18nContentSupport.getLocales().size()>1){
//
//                Content currentPage = MgnlContext.getAggregationState().getMainContent();
//                String currentUri = createURI(currentPage, i18nContentSupport.getLocale());
//                helper.attribute("currentURI", currentUri);
//
//                List<String> availableLocales =  new ArrayList<String>();
//
//                for (Locale locale : i18nContentSupport.getLocales()) {
//                    String uri = createURI(currentPage, locale);
//                    String label = StringUtils.capitalize(locale.getDisplayLanguage(locale));
//                    if(StringUtils.isNotEmpty(locale.getCountry())){
//                        label += " (" + StringUtils.capitalize(locale.getDisplayCountry()) + ")";
//                    }
//                    availableLocales.add(label + ":" + uri);
//                }
//
//                helper.attribute("availableLocales", StringUtils.join(availableLocales, ","));
//            }


        } catch (IOException e) {
            throw new TemplateProcessingException("comment", e);
        }

        Comment comment = new Comment(writer.toString());

        result.add(comment);
        t = new Text("\n");
        result.add(t);
        comment = new Comment(" /" + CMS_PAGE_TAG + " ");
        result.add(comment);
        t = new Text("\n");
        result.add(t);

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPrecedence() {
        return 1000;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * the path to a node.
     * @param node the node
     * @return its path
     * @throws TemplateProcessingException wraps repo exceptions
     */
    protected String getNodePath(javax.jcr.Node node) throws TemplateProcessingException {
        try {
            return node.getSession().getWorkspace().getName() + ":" + node.getPath();
        } catch (RepositoryException e) {
            throw new TemplateProcessingException("Can't construct node path for node " + node, e);
        }
    }
}
