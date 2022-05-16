package com.generate.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author derrick
 */
@Setter
@Getter
@Component
@ConfigurationProperties(value = "structure")
public class StructureProperty {
    private String packageName;
    private String basePackage;
    private String entityPackage;
    private String repositoryPackage;
    private String servicePackage;
    private String mapperPackage;
    private String implPackage;
    private String entitySuffix;
    private String repositorySuffix;
    private String repositoryImplSuffix;
    private String serviceSuffix;
    private String serviceImplSuffix;
    private String mapperSuffix;
    private String tablePrefix;
    private String outputPath;
    private String compileOut;
}
