package com.pragbits.stash;

public interface BearyChatGlobalSettingsService {
    String getWebHookUrl(String key);
    void setWebHookUrl(String key, String value);
}
