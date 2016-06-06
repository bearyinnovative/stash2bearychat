package com.pragbits.bitbucketserver;

public interface BearyChatGlobalSettingsService {
    String getWebHookUrl(String key);
    void setWebHookUrl(String key, String value);
}
