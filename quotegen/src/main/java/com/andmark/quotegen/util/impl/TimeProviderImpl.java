package com.andmark.quotegen.util.impl;

import com.andmark.quotegen.util.TimeProvider;
import org.springframework.stereotype.Component;

@Component
public class TimeProviderImpl implements TimeProvider {

    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

}
