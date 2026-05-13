package io.github.mirvmir.properties;

import io.github.mirvmir.common.properties.AppProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class DbProperties {

    private final AppProperties appProperties;

    private String dbWebUrl;
    private String dbDockerUrl;
    private String dbUser;
    private String dbPassword;

    public String getUrl() {
        return "web".equals(appProperties.getAppMode()) ? dbWebUrl : dbDockerUrl;
    }

    public String getUser() {
        return dbUser;
    }

    public String getPassword() {
        return dbPassword;
    }
}