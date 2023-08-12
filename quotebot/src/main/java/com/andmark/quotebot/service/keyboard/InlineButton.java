package com.andmark.quotebot.service.keyboard;

public class InlineButton {
    private String name;
    private String callbackData;

    public InlineButton(String name, String callbackData) {
        this.name = name;
        this.callbackData = callbackData;
    }

    public String getName() {
        return name;
    }

    public String getCallbackData() {
        return callbackData;
    }
}
