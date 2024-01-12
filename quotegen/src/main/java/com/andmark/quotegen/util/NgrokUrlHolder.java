package com.andmark.quotegen.util;

import org.springframework.stereotype.Component;

@Component
public class NgrokUrlHolder {
    private static NgrokUrlHolder instance;
    private String publicUrl;

    private NgrokUrlHolder() {
    }

    public static synchronized NgrokUrlHolder getInstance() {
        if (instance == null) {
            instance = new NgrokUrlHolder();
        }
        return instance;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }
}

