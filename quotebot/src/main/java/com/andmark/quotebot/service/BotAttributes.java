package com.andmark.quotebot.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BotAttributes {

    private int lastMessageId;
    private String lastCallbackMessage;
    private String confirmedContent;
    private List<String> imageUrls;

    public BotAttributes(List<String> imageUrls) {
        this.imageUrls = new ArrayList<>();
    }

    public int getLastMessageId() {
        return lastMessageId;
    }

    public void setLastMessageId(int lastMessageId) {
        this.lastMessageId = lastMessageId;
    }

    public String getLastCallbackMessage() {
        return lastCallbackMessage;
    }

    public void setLastCallbackMessage(String lastCallbackMessage) {
        this.lastCallbackMessage = lastCallbackMessage;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getConfirmedContent() {
        return confirmedContent;
    }

    public void setConfirmedContent(String confirmedContent) {
        this.confirmedContent = confirmedContent;
    }
}
