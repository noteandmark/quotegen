package com.andmark.quotegen.util;

import com.andmark.quotegen.config.NgrokConfiguration;
import com.github.alexdlaird.ngrok.NgrokClient;
import com.github.alexdlaird.ngrok.conf.JavaNgrokConfig;
import com.github.alexdlaird.ngrok.protocol.CreateTunnel;
import com.github.alexdlaird.ngrok.protocol.Region;
import com.github.alexdlaird.ngrok.protocol.Tunnel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.andmark.quotegen.config.AppConfig.ngrokAuthToken;
import static com.github.alexdlaird.util.StringUtils.isNotBlank;
import static java.util.Objects.nonNull;

@Component
@Slf4j
public class NgrokWebServerEventListener {
    private final NgrokConfiguration ngrokConfiguration;
    private final NgrokUrlHolder ngrokUrlHolder;

    @Autowired
    public NgrokWebServerEventListener(final NgrokConfiguration ngrokConfiguration, NgrokUrlHolder ngrokUrlHolder) {
        this.ngrokConfiguration = ngrokConfiguration;
        this.ngrokUrlHolder = ngrokUrlHolder;
    }

    @EventListener
    public void onApplicationEvent(final WebServerInitializedEvent event) {
        log.debug("ngrok event WebServerInitializedEvent");
        // java-ngrok will only be installed, and should only ever be initialized, in a dev environment
        if (ngrokConfiguration.isEnabled() && isNotBlank(ngrokAuthToken)) {
            log.debug("ngrok config enabled");
            final JavaNgrokConfig javaNgrokConfig = new JavaNgrokConfig.Builder()
                    .withRegion(nonNull(ngrokConfiguration.getRegion()) ? Region.valueOf(ngrokConfiguration.getRegion().toUpperCase()) : null)
                    .build();
            final NgrokClient ngrokClient = new NgrokClient.Builder()
                    .withJavaNgrokConfig(javaNgrokConfig)
                    .build();

            final int port = event.getWebServer().getPort();

            final CreateTunnel createTunnel = new CreateTunnel.Builder()
                    .withAddr(port)
                    .build();
            final Tunnel tunnel = ngrokClient.connect(createTunnel);

            log.info(String.format("ngrok tunnel \"%s\" -> \"http://127.0.0.1:%d\"", tunnel.getPublicUrl(), port));

            // Update any base URLs or webhooks to use the public ngrok URL
            initWebhooks(tunnel.getPublicUrl());
        }
    }

    private void initWebhooks(final String publicUrl) {
        // Update inbound traffic via APIs to use the public-facing ngrok URL
        ngrokUrlHolder.setPublicUrl(publicUrl);
    }

}
