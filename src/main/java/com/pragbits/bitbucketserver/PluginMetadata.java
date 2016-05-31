package com.pragbits.bitbucketserver;

public class PluginMetadata {

    public static String getPluginKey() {
        return "com.pragbits.bitbucketserver.stash2bearychat";
    }

    public static String getCompleteModuleKey(String moduleKey) {
        return getPluginKey() + ":" + moduleKey;
    }
}
