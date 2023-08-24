package com.andmark.quotebot.service.googleapi;

import com.andmark.quotebot.dto.GoogleCustomSearchResponse;
import com.andmark.quotebot.util.BotAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class GoogleCustomSearchService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.search.engine.id}")
    private String searchEngineId;

    private final RestTemplate restTemplate;
    private final BotAttributes botAttributes;

    public GoogleCustomSearchService(RestTemplate restTemplate, BotAttributes botAttributes) {
        this.restTemplate = restTemplate;
        this.botAttributes = botAttributes;
    }

    public List<String> searchImagesByKeywords(String keywords) {
        GoogleCustomSearchResponse response = searchImages(keywords);
        if (response == null) {
            log.warn("Response is null. Returning empty list.");
            return Collections.emptyList();
        }
        if (response.getItems() == null) {
            log.warn("No items found in the response. Returning empty list.");
            return Collections.emptyList();
        }
        log.debug("Received response from image search with keywords.");
        return extractImageUrls(response);
    }

    public GoogleCustomSearchResponse searchImages(String keywords) {
        String apiUrl = "https://www.googleapis.com/customsearch/v1";
        String requestUrl = apiUrl + "?key=" + apiKey + "&cx=" + searchEngineId + "&searchType=image&q=" + keywords;
        log.debug("requestUrl = {}", requestUrl);
        ResponseEntity<GoogleCustomSearchResponse> responseEntity = restTemplate.getForEntity(requestUrl, GoogleCustomSearchResponse.class);
        return responseEntity.getBody();
    }

    private List<String> extractImageUrls(GoogleCustomSearchResponse response) {
        List<String> imageUrls = new ArrayList<>();
        // Extract image URLs from the response
        if (response.getItems() != null) {
            for (GoogleCustomSearchResponse.Item item : response.getItems()) {
                if (item.getImage() != null && item.getImage().getThumbnailLink() != null) {
                    log.debug("add imageUrls = {}", item.getImage().getThumbnailLink());
                    imageUrls.add(item.getImage().getThumbnailLink());
                }
            }
        } else {
            log.warn("response.getItems() is null");
        }
        return imageUrls;
    }
}
