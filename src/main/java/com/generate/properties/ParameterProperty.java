package com.generate.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author derrick
 */
@Component
@Setter
@Getter
@ConfigurationProperties(value = "parameter")
public class ParameterProperty {
    private String tables;
    private String author;
    private boolean output;
}
