package com.generate.generator;

import com.generate.dynamic.DynamicClassLoader;
import com.generate.enums.TypeEnum;
import com.generate.properties.ParameterProperty;
import com.generate.properties.StructureProperty;
import com.generate.utils.ModuleCommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author derrick
 */
@Setter
@Log4j2
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaseGenerator {
    private final StructureProperty structureProperty;
    private final DynamicClassLoader dynamicClassLoader;
    private final ParameterProperty parameterProperty;

    @Value("classpath:BaseEntity.txt")
    private Resource baseEntityResource;
    @Value("classpath:BaseRepository.txt")
    private Resource baseRepositoryResource;
    @Value("classpath:BaseRepositoryImpl.txt")
    private Resource baseRepositoryImplResource;
    @Value("classpath:BaseService.txt")
    private Resource baseServiceResource;
    @Value("classpath:BaseServiceImpl.txt")
    private Resource baseServiceImplResource;

    public void createAndCompile() throws IOException {
        String packageName =
                structureProperty.getPackageName() + (StringUtils.isNotEmpty(structureProperty.getBasePackage()) ?
                        "." + structureProperty.getBasePackage() :
                        "");
        String separator = System.getProperty("line.separator");
        Map<TypeEnum, Resource> resourceMap = new HashMap<>(8);
        resourceMap.put(TypeEnum.ENTITY, baseEntityResource);
        resourceMap.put(TypeEnum.REPOSITORY, baseRepositoryResource);
        resourceMap.put(TypeEnum.REPOSITORY_IMPL, baseRepositoryImplResource);
        resourceMap.put(TypeEnum.SERVICE, baseServiceResource);
        resourceMap.put(TypeEnum.SERVICE_IMPL, baseServiceImplResource);
        List<File> files = new ArrayList<>();
        for (TypeEnum type : resourceMap.keySet()) {
            Resource resource = resourceMap.get(type);
            InputStream is = resource.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder builder = new StringBuilder();
            String data = br.readLine();
            while (data != null) {
                builder.append(data).append(separator);
                data = br.readLine();
            }
            String filePath = ModuleCommonUtil.getBaseJavaFilePath(type);
            String content = builder.toString().replace("{package}", packageName)
                    .replace("{author}", parameterProperty.getAuthor());
            File file = new File(filePath);
            if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                try (FileWriter writer = new FileWriter(filePath); BufferedWriter bufferedWriter = new BufferedWriter(
                        writer)) {
                    bufferedWriter.write(content);
                } catch (IOException e) {
                    log.error("fail to create BaseRepositoryImpl java source file");
                }
                dynamicClassLoader.addCompileFile(new File(filePath));
            }
        }
        dynamicClassLoader.compile();
    }
}
