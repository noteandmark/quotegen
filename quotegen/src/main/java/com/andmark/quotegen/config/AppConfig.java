package com.andmark.quotegen.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "prod"}) // Both profiles will use this component
public class AppConfig {

}
