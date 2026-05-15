package io.github.mirvmir.identity.application.properties;

import io.github.mirvmir.common.properties.AppProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyProperties {

    private final AppProperties appProperties;

    @Value("${secrets.docker.public_key}")
    private String dockerPublicKeyPath;

    @Value("${secrets.docker.private_key}")
    private String dockerPrivateKeyPath;

    @Value("${secrets.web.public_key}")
    private String webPublicKeyPath;

    @Value("${secrets.web.private_key}")
    private String webPrivateKeyPath;

    public KeyProperties(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String getPrivateKeyPath() {
        return "web".equals(appProperties.getAppMode())
                ? webPrivateKeyPath
                : dockerPrivateKeyPath;
    }

    public String getPublicKeyPath() {
        return "web".equals(appProperties.getAppMode())
                ? webPublicKeyPath
                : dockerPublicKeyPath;
    }
}