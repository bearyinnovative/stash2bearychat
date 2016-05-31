package com.pragbits.bitbucketserver.tools;
import java.util.LinkedList;
import java.util.List;

public class BearyChatAttachment {

    private String[] mrkdwn_in = new String[]{
            "pretext",
            "text",
            "title",
            "fields",
            "fallback"
    };

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public String getPretext() {
        return pretext;
    }

    public void setPretext(String pretext) {
        this.pretext = pretext;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void addField(BearyChatAttachmentField bearychatAttachmentField) {
        this.fields.add(bearychatAttachmentField);
    }

    public void removeField(int index) {
        this.fields.remove(index);
    }


    private List<BearyChatAttachmentField> fields = new LinkedList<BearyChatAttachmentField>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle_link() {
        return title_link;
    }

    public void setTitle_link(String title_link) {
        this.title_link = title_link;
    }

    private String title;
    private String title_link;
    private String fallback;
    private String pretext;
    private String color;
}
