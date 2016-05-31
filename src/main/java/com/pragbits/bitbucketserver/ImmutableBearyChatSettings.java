package com.pragbits.bitbucketserver;

public class ImmutableBearyChatSettings implements BearyChatSettings {

    private final boolean bearychatNotificationsEnabled;
    private final boolean bearychatNotificationsEnabledForPush;
    private final String bearychatChannelName;
    private final String bearychatWebHookUrl;

    public ImmutableBearyChatSettings(boolean bearychatNotificationsEnabled,
                                  boolean bearychatNotificationsEnabledForPush,
                                  String bearychatChannelName,
                                  String bearychatWebHookUrl) {
        this.bearychatNotificationsEnabled = bearychatNotificationsEnabled;
        this.bearychatNotificationsEnabledForPush = bearychatNotificationsEnabledForPush;
        this.bearychatChannelName = bearychatChannelName;
        this.bearychatWebHookUrl = bearychatWebHookUrl;
    }

    public boolean isBearyChatNotificationsEnabled() {
        return bearychatNotificationsEnabled;
    }

    public boolean isBearyChatNotificationsEnabledForPush() {
        return bearychatNotificationsEnabledForPush;
    }

    public String getBearyChatChannelName() {
        return bearychatChannelName;
    }

    public String getBearyChatWebHookUrl() {
        return bearychatWebHookUrl;
    }

    @Override
    public String toString() {
        return "ImmutableBearyChatSettings {" + "bearychatNotificationsEnabled=" + bearychatNotificationsEnabled +
                ", bearychatNotificationsEnabledForPush=" + bearychatNotificationsEnabledForPush +
                ", bearychatChannelName=" + bearychatChannelName +
                ", bearychatWebHookUrl=" + bearychatWebHookUrl + "}";
    }

}
