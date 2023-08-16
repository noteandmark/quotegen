package com.andmark.quotebot.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class RequestConfiguration {
    private String url;
    private HttpMethod httpMethod;
    private Object requestBody;
    private Long chatId;
    private String successMessage;
    private InlineKeyboardMarkup keyboard;

    public static class Builder {
        private String url;
        private HttpMethod httpMethod;
        private Object requestBody;
        private Long chatId;
        private String successMessage;
        private InlineKeyboardMarkup keyboard;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder httpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder requestBody(Object requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder chatId(Long chatId) {
            this.chatId = chatId;
            return this;
        }

        public Builder successMessage(String successMessage) {
            this.successMessage = successMessage;
            return this;
        }

        public Builder keyboard(InlineKeyboardMarkup keyboard) {
            this.keyboard = keyboard;
            return this;
        }

        public RequestConfiguration build() {
            RequestConfiguration config = new RequestConfiguration();
            config.url = this.url;
            config.httpMethod = this.httpMethod;
            config.requestBody = this.requestBody;
            config.chatId = this.chatId;
            config.successMessage = this.successMessage;
            config.keyboard = this.keyboard;
            return config;
        }
    }
}
