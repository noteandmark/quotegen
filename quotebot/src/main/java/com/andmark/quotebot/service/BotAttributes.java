package com.andmark.quotebot.service;

import com.andmark.quotebot.service.enums.BotState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Component
@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class BotAttributes {

    private Long chatId;
    private int lastMessageId;
    //TODO: возможно, стоит хранить всю цитату?
    private Long quoteId;
    private List<String> imageUrls;
    private String lastCallbackMessage;
    private String confirmedContent;
    private String confirmedUrl;
    private BotState currentState;
}
