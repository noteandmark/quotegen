package com.andmark.quotebot.service;

import com.andmark.quotebot.service.enums.BotState;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BotAttributes {

    private int lastMessageId;
    //TODO: возможно, стоит хранить всю цитату?
    private Long quoteId;
    private List<String> imageUrls;
    private String lastCallbackMessage;
    private String confirmedContent;
    private String confirmedUrl;
    private BotState currentState;

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

    public BotState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(BotState currentState) {
        this.currentState = currentState;
    }

    public String getConfirmedUrl() {
        return confirmedUrl;
    }

    public void setConfirmedUrl(String confirmedUrl) {
        this.confirmedUrl = confirmedUrl;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public void setQuoteId(Long quoteId) {
        this.quoteId = quoteId;
    }
}
