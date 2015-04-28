package com.pragbits.stash;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.pragbits.stash.BearyChatSettings;
import com.pragbits.stash.BearyChatSettingsService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionValidationService;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultBearyChatSettingsService implements BearyChatSettingsService {

    static final ImmutableBearyChatSettings DEFAULT_CONFIG = new ImmutableBearyChatSettings(false, false, "", "");

    static final String KEY_bearychat_NOTIFICATION = "bearychatNotificationsEnabled";
    static final String KEY_bearychat_NOTIFICATION_PUSH = "bearychatNotificationsEnabledForPush";
    static final String KEY_bearychat_CHANNEL_NAME = "bearychatChannelName";
    static final String KEY_bearychat_WEBHOOK_URL = "bearychatWebHookUrl";

    private final PluginSettings pluginSettings;
    private final PermissionValidationService validationService;

    private final Cache<Integer, BearyChatSettings> cache = CacheBuilder.newBuilder().build(
            new CacheLoader<Integer, BearyChatSettings>() {
                @Override
                public BearyChatSettings load(@Nonnull Integer repositoryId) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> data = (Map) pluginSettings.get(repositoryId.toString());
                    return data == null ? DEFAULT_CONFIG : deserialize(data);
                }
            }
    );

    public DefaultBearyChatSettingsService(PluginSettingsFactory pluginSettingsFactory, PermissionValidationService validationService) {
        this.validationService = validationService;
        this.pluginSettings = pluginSettingsFactory.createSettingsForKey(PluginMetadata.getPluginKey());
    }

    @Nonnull
    @Override
    public BearyChatSettings getBearyChatSettings(@Nonnull Repository repository) {
        validationService.validateForRepository(checkNotNull(repository, "repository"), Permission.REPO_READ);

        try {
            //noinspection ConstantConditions
            return cache.get(repository.getId());
        } catch (ExecutionException e) {
            throw Throwables.propagate(e.getCause());
        }
    }

    @Nonnull
    @Override
    public BearyChatSettings setBearyChatSettings(@Nonnull Repository repository, @Nonnull BearyChatSettings settings) {
        validationService.validateForRepository(checkNotNull(repository, "repository"), Permission.REPO_ADMIN);
        Map<String, String> data = serialize(checkNotNull(settings, "settings"));
        pluginSettings.put(Integer.toString(repository.getId()), data);
        cache.invalidate(repository.getId());

        return deserialize(data);
    }

    // note: for unknown reason, pluginSettngs.get() is not getting back the key for an empty string value
    // probably I don't know someyhing here. Applying a hack
    private Map<String, String> serialize(BearyChatSettings settings) {
        return ImmutableMap.of(
                KEY_bearychat_NOTIFICATION, Boolean.toString(settings.isBearyChatNotificationsEnabled()),
                KEY_bearychat_NOTIFICATION_PUSH, Boolean.toString(settings.isBearyChatNotificationsEnabledForPush()),
                KEY_bearychat_CHANNEL_NAME, settings.getBearyChatChannelName().isEmpty() ? " " : settings.getBearyChatChannelName(),
                KEY_bearychat_WEBHOOK_URL, settings.getBearyChatWebHookUrl().isEmpty() ? " " : settings.getBearyChatWebHookUrl()
        );
    }

    // note: for unknown reason, pluginSettngs.get() is not getting back the key for an empty string value
    // probably I don't know someyhing here. Applying a hack
    private BearyChatSettings deserialize(Map<String, String> settings) {
        return new ImmutableBearyChatSettings(
                Boolean.parseBoolean(settings.get(KEY_bearychat_NOTIFICATION)),
                Boolean.parseBoolean(settings.get(KEY_bearychat_NOTIFICATION_PUSH)),
                settings.get(KEY_bearychat_CHANNEL_NAME).toString().equals(" ") ? "" : settings.get(KEY_bearychat_CHANNEL_NAME).toString(),
                settings.get(KEY_bearychat_WEBHOOK_URL).toString().equals(" ") ? "" : settings.get(KEY_bearychat_WEBHOOK_URL).toString()
        );
    }

}
