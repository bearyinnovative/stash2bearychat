package com.pragbits.bitbucketserver;

public interface BearyChatSettings {

    boolean isBearyChatNotificationsEnabled();
    boolean isBearyChatNotificationsEnabledForPush();
    String getBearyChatChannelName();
    String getBearyChatWebHookUrl();

}
