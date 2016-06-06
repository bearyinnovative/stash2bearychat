package com.pragbits.bitbucketserver.components;

import com.atlassian.event.api.EventListener;
import com.atlassian.bitbucket.event.pull.PullRequestActivityEvent;
import com.atlassian.bitbucket.event.pull.PullRequestCommentActivityEvent;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.repository.Repository;
import com.google.gson.Gson;
import com.pragbits.bitbucketserver.BearyChatGlobalSettingsService;
import com.pragbits.bitbucketserver.BearyChatSettings;
import com.pragbits.bitbucketserver.BearyChatSettingsService;
import com.pragbits.bitbucketserver.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PullRequestActivityListener {
    static final String KEY_GLOBAL_SETTING_HOOK_URL = "stash2bearychat.globalsettings.hookurl";
    private static final Logger log = LoggerFactory.getLogger(PullRequestActivityListener.class);

    private final BearyChatGlobalSettingsService bearychatGlobalSettingsService;
    private final BearyChatSettingsService bearychatSettingsService;
    private final NavBuilder navBuilder;
    private final BearyChatNotifier bearychatNotifier;
    private final Gson gson = new Gson();


    public PullRequestActivityListener(BearyChatGlobalSettingsService bearychatGlobalSettingsService,
                                             BearyChatSettingsService bearychatSettingsService,
                                             NavBuilder navBuilder,
                                             BearyChatNotifier bearychatNotifier) {
        this.bearychatGlobalSettingsService = bearychatGlobalSettingsService;
        this.bearychatSettingsService = bearychatSettingsService;
        this.navBuilder = navBuilder;
        this.bearychatNotifier = bearychatNotifier;
    }

    @EventListener
    public void NotifyBearyChatChannel(PullRequestActivityEvent event) {
        // find out if notification is enabled for this repo
        Repository repository = event.getPullRequest().getToRef().getRepository();
        BearyChatSettings bearychatSettings = bearychatSettingsService.getBearyChatSettings(repository);
        String globalHookUrl = bearychatGlobalSettingsService.getWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL);

        if (bearychatSettings.isBearyChatNotificationsEnabled()) {

            String localHookUrl = bearychatSettings.getBearyChatWebHookUrl();
            WebHookSelector hookSelector = new WebHookSelector(globalHookUrl, localHookUrl);

            if (!hookSelector.isHookValid()) {
                log.error("There is no valid configured Web hook url! Reason: " + hookSelector.getProblem());
                return;
            }

            String repoName = repository.getSlug();
            String projectName = repository.getProject().getKey();
            long pullRequestId = event.getPullRequest().getId();
            String userName = event.getUser() != null ? event.getUser().getDisplayName() : "unknown user";
            String activity = event.getActivity().getAction().name();

            // Ignore RESCOPED PR events
            if (activity.equalsIgnoreCase("RESCOPED")) {
                return;
            }

            String url = navBuilder
                    .project(projectName)
                    .repo(repoName)
                    .pullRequest(pullRequestId)
                    .overview()
                    .buildAbsolute();

            String text = String.format("Pull request event: `%s`, activity: `%s` by `%s`. [See details](%s)",
                    event.getPullRequest().getTitle(),
                    activity,
                    userName,
                    url);
            String fallback = String.format("Pull request event: `%s`, activity: `%s` by `%s`.",
                    event.getPullRequest().getTitle(),
                    activity,
                    userName);

            BearyChatPayload payload = new BearyChatPayload();
            if (!bearychatSettings.getBearyChatChannelName().isEmpty()) {
                payload.setChannel(bearychatSettings.getBearyChatChannelName());
            }
            payload.setText(text);
            //payload.setMrkdwn(true);

            BearyChatAttachment attachment = new BearyChatAttachment();
            attachment.setFallback(fallback);
            //attachment.setPretext(String.format(""));
            //attachment.setColor("#aabbcc");

            BearyChatAttachmentField field = new BearyChatAttachmentField();
            field.setTitle("Event details");

            switch (event.getActivity().getAction()) {
                case OPENED:
                    field.setValue(String.format("*%s* OPENED the pull request: `%s`", userName,  event.getPullRequest().getTitle()));
                    //attachment.setColor("#2267c4"); // blue
                    break;
                case DECLINED:
                    field.setValue(String.format("*%s* DECLINED the pull request", userName));
                    //attachment.setColor("#ff0024"); // red
                    break;
                case APPROVED:
                    field.setValue(String.format("*%s* APPROVED the pull request", userName));
                    //attachment.setColor("#2dc422"); // green
                    break;
                case MERGED:
                    field.setValue(String.format("*%s* MERGED the pull request", userName));
                    //attachment.setColor("#2dc422"); // green
                    break;
                case REOPENED:
                    field.setValue(String.format("*%s* REOPENED the pull request", userName));
                    //attachment.setColor("#2267c4"); // blue
                    break;
                case RESCOPED:
                    field.setValue(String.format("*%s* RESCOPED the pull request", userName));
                    //attachment.setColor("#9055fc"); // purple
                    break;
                case UNAPPROVED:
                    field.setValue(String.format("*%s* UNAPPROVED the pull request", userName));
                    //attachment.setColor("#ff0024"); // red
                    break;
            }

            if (event instanceof PullRequestCommentActivityEvent) {
                field.setValue(String.format("*%s* commented the pull request: `%s`",
                        userName,
                        ((PullRequestCommentActivityEvent)event).getActivity().getComment().getText()));
            }

            field.setShort(false);
            attachment.addField(field);
            //payload.addAttachment(attachment);
            String jsonPayload = gson.toJson(payload);

            bearychatNotifier.SendBearyChatNotification(hookSelector.getSelectedHook(), jsonPayload);
        }

    }
}
