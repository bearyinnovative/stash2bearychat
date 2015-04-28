package com.pragbits.stash.tools;

import java.util.LinkedList;
import java.util.List;

public class BearyChatPayload {

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channelName) {
        this.channel = channelName;
    }

    private String channel;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;

    public boolean isMrkdwn() {
        return mrkdwn;
    }

    public void setMrkdwn(boolean mrkdwn) {
        this.mrkdwn = mrkdwn;
    }

    private boolean mrkdwn;

    private List<BearyChatAttachment> attachments = new LinkedList<BearyChatAttachment>();

    public void addAttachment(BearyChatAttachment bearychatAttachment) {
        this.attachments.add(bearychatAttachment);
    }

    public void removeAttachment(int index) {
        this.attachments.remove(index);
    }

}
