package com.andmark.quotegen.util;

import com.andmark.quotegen.config.NgrokConfiguration;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Region;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.andmark.quotegen.config.AppConfig.ngrokAuthToken;
import static com.github.alexdlaird.util.StringUtils.isNotBlank;
import static java.util.Objects.nonNull;

@Component
@Slf4j
public class NgrokWebServerEventListener {
    private NgrokConfiguration ngrokConfiguration;
    private NgrokUrlHolder ngrokUrlHolder;
    private NgrokClient ngrokClient;
    private int serverPort;

    @Autowired
    public void setNgrokConfiguration(NgrokConfiguration ngrokConfiguration) {
        this.ngrokConfiguration = ngrokConfiguration;
    }

    @Autowired
    public void setNgrokUrlHolder(NgrokUrlHolder ngrokUrlHolder) {
        this.ngrokUrlHolder = ngrokUrlHolder;
    }

    @EventListener
    public void onApplicationEvent(WebServerInitializedEvent event) {
        log.debug("ngrok event WebServerInitializedEvent");
        if (ngrokConfiguration != null && ngrokConfiguration.isEnabled() && isNotBlank(ngrokAuthToken)) {
            log.debug("ngrok config enabled");
            JavaNgrokConfig javaNgrokConfig = new JavaNgrokConfig.Builder()
                    .withRegion(nonNull(ngrokConfiguration.getRegion()) ? Region.valueOf(ngrokConfiguration.getRegion().toUpperCase()) : null)
                    .build();
            ngrokClient = new NgrokClient.Builder()
                    .withJavaNgrokConfig(javaNgrokConfig)
                    .build();
            serverPort = event.getWebServer().getPort();
            log.debug("serverPort = {}", serverPort);
            createNgrokTunnel(serverPort);
        } else {
            log.warn("ngrok configuration is not enabled or invalid");
        }
    }

    public String getWebLink() {
        log.debug("ngrok event WebServerInitializedEvent");
        if (ngrokConfiguration != null && ngrokConfiguration.isEnabled() && isNotBlank(ngrokAuthToken)) {
            log.debug("ngrok config enabled");
            if (isNgrokTunnelOpen()) {
                log.debug("ngrok tunnel is open, using existing tunnel");
            } else {
                log.debug("ngrok tunnel is not open, creating new tunnel");
                createNgrokTunnel(serverPort);
            }
            return ngrokUrlHolder.getPublicUrl();
        } else {
            log.warn("ngrok configuration is not enabled or invalid");
            return null;
        }
    }

    @PreDestroy
    public void closeNgrokTunnel() {
        if (ngrokClient != null) {
            ngrokClient.disconnect(ngrokUrlHolder.getPublicUrl());
            log.info("Closed ngrok tunnel");
        }
    }

    private void createNgrokTunnel(int port) {
        CreateTunnel createTunnel = new CreateTunnel.Builder()
                .withAddr(port)
                .build();
        Tunnel tunnel = ngrokClient.connect(createTunnel);
        ngrokUrlHolder.setPublicUrl(tunnel.getPublicUrl());
        log.info(String.format("ngrok tunnel \"%s\" -> \"http://127.0.0.1:%d\"", tunnel.getPublicUrl(), port));
        initWebhooks(tunnel.getPublicUrl());
    }

    private void initWebhooks(String publicUrl) {
        // Update inbound traffic via APIs to use the public-facing ngrok URL
        ngrokUrlHolder.setPublicUrl(publicUrl);
    }

    private boolean isNgrokTunnelOpen() {
        return ngrokClient != null && ngrokClient.getTunnels().stream()
                .anyMatch(tunnel -> tunnel.getPublicUrl().startsWith("https://"));
    }
}
