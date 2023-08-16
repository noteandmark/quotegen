package com.andmark.quotebot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleCustomSearchResponse {

    @JsonProperty("items")
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        @JsonProperty("link")
        private String link;

        public String getLink() {
            return link;
        }

        @JsonProperty("image")
        private Image image;

        public Image getImage() {
            return image;
        }
    }

    public static class Image {
        @JsonProperty("thumbnailLink")
        private String thumbnailLink;

        public String getThumbnailLink() {
            return thumbnailLink;
        }
    }

}
