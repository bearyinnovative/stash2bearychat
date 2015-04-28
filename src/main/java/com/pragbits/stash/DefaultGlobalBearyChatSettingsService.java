package com.pragbits.stash;


import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

public class DefaultGlobalBearyChatSettingsService implements BearyChatGlobalSettingsService {

    private final PluginSettings pluginSettings;

    public DefaultGlobalBearyChatSettingsService(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
    }

    @Override
    public String getWebHookUrl(String key) {
        Object retval = pluginSettings.get(key);
        if (null == retval) {
            return "";
        }

        return retval.toString();
    }

    @Override
    public void setWebHookUrl(String key, String value) {
        pluginSettings.put(key, value);
    }
}
