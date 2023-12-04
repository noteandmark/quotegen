package com.andmark.quotegen.dto;

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

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {
        @JsonProperty("link")
        private String link;

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        @JsonProperty("image")
        private Image image;

        public Image getImage() {
            return image;
        }

        public void setImage(Image image) {
            this.image = image;
        }
    }

    public static class Image {
        @JsonProperty("thumbnailLink")
        private String thumbnailLink;

        public String getThumbnailLink() {
            return thumbnailLink;
        }

        public void setThumbnailLink(String thumbnailLink) {
            this.thumbnailLink = thumbnailLink;
        }
    }

}
