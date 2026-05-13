package io.github.mirvmir.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Getter
public class HibernateProperties {
    private String driver;
    private String dialect;
    private String hbm2ddlAuto;
}