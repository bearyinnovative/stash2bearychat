package com.pragbits.stash;

public interface BearyChatSettings {

    boolean isBearyChatNotificationsEnabled();
    boolean isBearyChatNotificationsEnabledForPush();
    String getBearyChatChannelName();
    String getBearyChatWebHookUrl();

}
