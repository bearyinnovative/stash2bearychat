package com.pragbits.bitbucketserver.components;

import com.atlassian.bitbucket.commit.*;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.event.api.EventListener;
import com.atlassian.bitbucket.event.repository.RepositoryPushEvent;
import com.atlassian.bitbucket.nav.NavBuilder;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.RefChangeType;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequest;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.pragbits.bitbucketserver.BearyChatGlobalSettingsService;
import com.pragbits.bitbucketserver.BearyChatSettings;
import com.pragbits.bitbucketserver.BearyChatSettingsService;
import com.pragbits.bitbucketserver.tools.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class RepositoryPushActivityListener {
    static final String KEY_GLOBAL_SETTING_HOOK_URL = "stash2bearychat.globalsettings.hookurl";
    private static final Logger log = LoggerFactory.getLogger(RepositoryPushActivityListener.class);

    private final BearyChatGlobalSettingsService bearychatGlobalSettingsService;
    private final BearyChatSettingsService bearychatSettingsService;
    private final CommitService commitService;
    private final NavBuilder navBuilder;
    private final BearyChatNotifier bearychatNotifier;
    private final Gson gson = new Gson();

    public RepositoryPushActivityListener(BearyChatGlobalSettingsService bearychatGlobalSettingsService,
                                          BearyChatSettingsService bearychatSettingsService,
                                          CommitService commitService,
                                          NavBuilder navBuilder,
                                          BearyChatNotifier bearychatNotifier) {
        this.bearychatGlobalSettingsService = bearychatGlobalSettingsService;
        this.bearychatSettingsService = bearychatSettingsService;
        this.commitService = commitService;
        this.navBuilder = navBuilder;
        this.bearychatNotifier = bearychatNotifier;
    }

    @EventListener
    public void NotifyBearyChatChannel(RepositoryPushEvent event) {
        // find out if notification is enabled for this repo
        Repository repository = event.getRepository();
        BearyChatSettings bearychatSettings = bearychatSettingsService.getBearyChatSettings(repository);
        String globalHookUrl = bearychatGlobalSettingsService.getWebHookUrl(KEY_GLOBAL_SETTING_HOOK_URL);

        if (!bearychatSettings.isBearyChatNotificationsEnabledForPush()) {
            return;
        }

        String localHookUrl = bearychatSettings.getBearyChatWebHookUrl();
        WebHookSelector hookSelector = new WebHookSelector(globalHookUrl, localHookUrl);

        if (!hookSelector.isHookValid()) {
            log.error("There is no valid configured Web hook url! Reason: " + hookSelector.getProblem());
            return;
        }

        String repoName = repository.getSlug();
        String projectName = repository.getProject().getKey();
        String repoPath = projectName + "/" + event.getRepository().getName();

        for (RefChange refChange : event.getRefChanges()) {
            String ref = refChange.getRef().getId();
            NavBuilder.Repo repoUrlBuilder = navBuilder
                .project(projectName)
                .repo(repoName);
            String url = repoUrlBuilder
                .commits()
                .until(refChange.getRef().getId())
                .buildAbsolute();
            
            String text = String.format("Push on `%s` by `%s <%s>`. See [commit list](%s).",
                    event.getRepository().getName(),
                    event.getUser() != null ? event.getUser().getDisplayName() : "unknown user",
                    event.getUser() != null ? event.getUser().getEmailAddress() : "unknown email",
                    url);
            List<Commit> myCommits = new LinkedList<Commit>();

            boolean isMagicChange = refChange.getFromHash().equalsIgnoreCase("0000000000000000000000000000000000000000");
            boolean isNewRef = isMagicChange;
            boolean isDeleted = isMagicChange && refChange.getType() == RefChangeType.DELETE;
            
            if (isDeleted) {
                // issue#4: if type is "DELETE" and toHash is all zero then this is a branch delete
                if (ref.indexOf("refs/tags") >= 0) {
                    text = String.format("Tag `%s` deleted from repository [`%s`](%s).",
                            ref.replace("refs/tags/", ""),
                            repoPath,
                            repoUrlBuilder.buildAbsolute());
                } else {
                    text = String.format("Branch `%s` deleted from repository [`%s`](%s).",
                            ref.replace("refs/heads/", ""),
                            repoPath,
                            repoUrlBuilder.buildAbsolute());
                }
            } else if (isNewRef) {
                // issue#3 if fromHash is all zero (meaning the beginning of everything, probably), then this push is probably
                // a new branch or tag, and we want only to display the latest commit, not the entire history

                if (ref.indexOf("refs/tags") >= 0) {
                    text = String.format("Tag [`%s`](%s) pushed on [`%s`](%s). See [commit list](%s).",
                            ref.replace("refs/tags/", ""),
                            url,
                            repoPath,
                            repoUrlBuilder.buildAbsolute(),
                            url
                            );
                } else {
                    text = String.format("Branch [`%s`](%s) pushed on [`%s`](%s). See [commit list](%s).",
                            ref.replace("refs/heads/", ""),
                            url,
                            repoPath,
                            repoUrlBuilder.buildAbsolute(),
                            url);
                }
            } else {
                PageRequest pRequest = new PageRequestImpl(0, PageRequestImpl.MAX_PAGE_LIMIT);
                CommitsBetweenRequest commitsBetween = new CommitsBetweenRequest.Builder(repository)
                    .exclude(refChange.getFromHash())
                    .include(refChange.getToHash())
                    .build();
                Page<Commit> commitList = commitService.getCommitsBetween(commitsBetween, pRequest);
                myCommits.addAll(Lists.newArrayList(commitList.getValues()));

                int commitCount = myCommits.size();
                String commitStr = commitCount == 1 ? "commit" : "commits";

                String branch = ref.replace("refs/heads/", "");
                text = String.format("Push on [`%s`](%s) branch [`%s`](%s) by `%s <%s>` (%d %s). See [commit list](%s).",
                        repoPath,
                        repoUrlBuilder.buildAbsolute(),
                        branch,
                        url,
                        event.getUser() != null ? event.getUser().getDisplayName() : "unknown user",
                        event.getUser() != null ? event.getUser().getEmailAddress() : "unknown email",
                        commitCount, commitStr,
                        url);
            }

            BearyChatPayload payload = new BearyChatPayload();
            payload.setText(text);
            payload.setMrkdwn(true);
            
            if (!bearychatSettings.getBearyChatChannelName().isEmpty()) {
                payload.setChannel(bearychatSettings.getBearyChatChannelName());
            }

            for (Commit c : myCommits) {
                BearyChatAttachment attachment = new BearyChatAttachment();
                attachment.setColor("#aabbcc");
                BearyChatAttachmentField field = new BearyChatAttachmentField();

                attachment.setTitle(String.format("[%s:%s] - %s", event.getRepository().getName(), refChange.getRefId().replace("refs/heads", ""), c.getId()));
                attachment.setTitle_link(repoUrlBuilder.commit(c.getId()).buildAbsolute());

                field.setTitle(String.format("comment: %s", c.getMessage()));
                field.setShort(false);
                attachment.addField(field);
                payload.addAttachment(attachment);
            }
            
            bearychatNotifier.SendBearyChatNotification(hookSelector.getSelectedHook(), gson.toJson(payload));
        }
    }

}
