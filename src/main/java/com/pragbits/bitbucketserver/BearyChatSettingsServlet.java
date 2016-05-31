package com.pragbits.bitbucketserver;

import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.bitbucket.AuthorisationException;
import com.pragbits.bitbucketserver.BearyChatSettings;
import com.pragbits.bitbucketserver.BearyChatSettingsService;
import com.pragbits.bitbucketserver.PluginMetadata;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionValidationService;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.atlassian.bitbucket.i18n.I18nService;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BearyChatSettingsServlet extends HttpServlet {
    private final PageBuilderService pageBuilderService;
    private final BearyChatSettingsService bearychatSettingsService;
    private final RepositoryService repositoryService;
    private final SoyTemplateRenderer soyTemplateRenderer;
    private final PermissionValidationService validationService;
    private final I18nService i18nService;
    private static final Logger log = LoggerFactory.getLogger(BearyChatSettingsServlet.class);
    
    private Repository repository = null;

    public BearyChatSettingsServlet(PageBuilderService pageBuilderService,
                                    BearyChatSettingsService bearychatSettingsService,
                                    RepositoryService repositoryService,
                                    SoyTemplateRenderer soyTemplateRenderer,
                                    PermissionValidationService validationService,
                                    I18nService i18nService) {
        this.pageBuilderService = pageBuilderService;
        this.bearychatSettingsService = bearychatSettingsService;
        this.repositoryService = repositoryService;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.validationService = validationService;
        this.i18nService = i18nService;
    }

    @Override
    protected  void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        try {
            validationService.validateForGlobal(Permission.SYS_ADMIN);
        } catch (AuthorisationException e) {
            // Skip form processing
            doGet(req, res);
            return;
        }

        boolean enabled = false;
        if (null != req.getParameter("bearychatNotificationsEnabled") && req.getParameter("bearychatNotificationsEnabled").equals("on")) {
          enabled = true;
        }

        boolean enabledPush = false;
        if (null != req.getParameter("bearychatNotificationsEnabledForPush") && req.getParameter("bearychatNotificationsEnabledForPush").equals("on")) {
            enabledPush = true;
        }

        String channel = req.getParameter("bearychatChannelName");
        String webHookUrl = req.getParameter("bearychatWebHookUrl");
        bearychatSettingsService.setBearyChatSettings(repository, new ImmutableBearyChatSettings(enabled, enabledPush, channel, webHookUrl));
        
        doGet(req, res);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        if (Strings.isNullOrEmpty(pathInfo) || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String[] pathParts = pathInfo.substring(1).split("/");
        if (pathParts.length != 4) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String projectKey = pathParts[1];
        String repoSlug = pathParts[3];
        
        this.repository = repositoryService.getBySlug(projectKey, repoSlug);
        if (repository == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        doView(repository, response);

    }

    private void doView(Repository repository, HttpServletResponse response)
            throws ServletException, IOException {
        validationService.validateForRepository(repository, Permission.REPO_ADMIN);
        BearyChatSettings bearychatSettings = bearychatSettingsService.getBearyChatSettings(repository);
        render(response,
                "bitbucketserver.page.bearychat.settings.viewBearyChatSettings",
                ImmutableMap.<String, Object>builder()
                        .put("repository", repository)
                        .put("bearychatSettings", bearychatSettings)
                        .put("bearychatSettingsEnabled", bearychatSettings.isBearyChatNotificationsEnabled())
                        .put("bearychatSettingsPushEnabled", bearychatSettings.isBearyChatNotificationsEnabledForPush())
                        .put("bearychatSettingsChannelName", bearychatSettings.getBearyChatChannelName())
                        .put("bearychatSettingsWebhookUrl", bearychatSettings.getBearyChatWebHookUrl())
                        .build()
        );
    }

    private void render(HttpServletResponse response, String templateName, Map<String, Object> data)
            throws IOException, ServletException {
        pageBuilderService.assembler().resources().requireContext("plugin.page.bearychat");
        response.setContentType("text/html;charset=UTF-8");
        try {
            soyTemplateRenderer.render(response.getWriter(), PluginMetadata.getCompleteModuleKey("soy-templates"), templateName, data);
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(e);
        }
    }
}
