package com.generate.runner;

import com.generate.generator.BaseGenerator;
import com.generate.generator.EntityGenerator;
import com.generate.generator.MapperGenerator;
import com.generate.generator.RepositoryGenerator;
import com.generate.generator.RepositoryImplGenerator;
import com.generate.generator.ServiceGenerator;
import com.generate.generator.ServiceImplGenerator;
import com.generate.properties.ParameterProperty;
import com.generate.properties.StructureProperty;
import com.generate.utils.DatabaseUtil;
import com.google.common.base.CaseFormat;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author derrick
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GeneratorRunner implements CommandLineRunner {
    private final EntityGenerator entityGenerator;
    private final StructureProperty structureProperty;
    private final ParameterProperty parameterProperty;
    private final RepositoryGenerator repositoryGenerator;
    private final MapperGenerator mapperGenerator;
    private final RepositoryImplGenerator repositoryImplGenerator;
    private final ServiceGenerator serviceGenerator;
    private final ServiceImplGenerator serviceImplGenerator;
    private final BaseGenerator baseGenerator;

    @Override
    public void run(String... args) throws Exception {
        List<String> tableList = new ArrayList<>();
        if (StringUtils.endsWithIgnoreCase(parameterProperty.getTables(), "all")) {
            tableList.addAll(DatabaseUtil.getAllTableNames());
        } else {
            Arrays.stream(parameterProperty.getTables().split(",")).map(String::trim).forEach(tableList::add);
        }
        Map<String, String> tableModuleMap = new HashMap<>();
        for (String table : tableList) {
            tableModuleMap.put(table, CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,
                    StringUtils.substringAfter(table, structureProperty.getTablePrefix())));
        }
        baseGenerator.createAndCompile();
        entityGenerator.createAndCompileEntityFile(tableModuleMap);

        List<String> modules = new ArrayList<>(tableModuleMap.values());
        repositoryGenerator.genRepositoryInterface(modules);
        mapperGenerator.createMapperAndCompile(modules);
        repositoryImplGenerator.createAndCompile(modules);
        serviceGenerator.createAndCompile(modules);
        serviceImplGenerator.createAndCompile(modules);
    }
}
