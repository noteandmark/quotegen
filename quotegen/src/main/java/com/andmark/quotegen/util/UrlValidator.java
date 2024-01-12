package com.andmark.quotegen.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlValidator {
    private static final String URL_REGEX = "^(http|https)://[^\\s/$.?#].[^\\s]*$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    public static boolean isValidUrl(String url) {
        Matcher matcher = URL_PATTERN.matcher(url);
        return matcher.matches();
    }
}
