package com.andmark.quotebot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// dto which used in YesNoMagicCommand
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class YesNoApiResponse {

    private String answer;
    private boolean forced;
    private String image;

}
