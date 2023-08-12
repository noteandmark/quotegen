package com.andmark.quotebot.service.googleapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCustomSearchService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.search.engine.id}")
    private String searchEngineId;

    private final RestTemplate restTemplate;

    public GoogleCustomSearchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<String> searchImagesByKeywords(String keywords) {
        GoogleCustomSearchResponse response = searchImages(keywords);
        if (response != null) {
            return extractImageUrls(response);
        } else {
            return Collections.emptyList();
        }
    }

    public GoogleCustomSearchResponse searchImages(String keywords) {
        String apiUrl = "https://www.googleapis.com/customsearch/v1";
        String requestUrl = apiUrl + "?key=" + apiKey + "&cx=" + searchEngineId + "&searchType=image&q=" + keywords;

        ResponseEntity<GoogleCustomSearchResponse> responseEntity = restTemplate.getForEntity(requestUrl, GoogleCustomSearchResponse.class);
        return responseEntity.getBody();
    }

    private List<String> extractImageUrls(GoogleCustomSearchResponse response) {
        List<String> imageUrls = new ArrayList<>();
        // Extract image URLs from the response
        if (response.getItems() != null) {
            for (GoogleCustomSearchResponse.Item item : response.getItems()) {
                if (item.getImage() != null && item.getImage().getThumbnailLink() != null) {
                    imageUrls.add(item.getImage().getThumbnailLink());
                }
            }
        }
        return imageUrls;
    }

}
