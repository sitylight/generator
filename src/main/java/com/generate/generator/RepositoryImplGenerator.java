package com.generate.generator;

import com.generate.dynamic.DynamicClassLoader;
import com.generate.enums.TypeEnum;
import com.generate.properties.ParameterProperty;
import com.generate.properties.StructureProperty;
import com.generate.utils.ModuleCommonUtil;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author derrick
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepositoryImplGenerator {
    private final DynamicClassLoader dynamicClassLoader;
    private final StructureProperty structureProperty;
    private final ParameterProperty parameterProperty;

    public void createAndCompile(List<String> modules) throws ClassNotFoundException, IOException {
        Map<String, List<TypeEnum>> moduleTypesMap = new HashMap<>(16);
        for (String module : modules) {
            String nameOfEntity = ModuleCommonUtil.getNameByModule(module, TypeEnum.ENTITY);
            String nameOfRepository = ModuleCommonUtil.getNameByModule(module, TypeEnum.REPOSITORY);
            String nameOfMapper = ModuleCommonUtil.getNameByModule(module, TypeEnum.MAPPER);
            String nameOfBaseRepositoryImpl = ModuleCommonUtil.getBaseName(TypeEnum.REPOSITORY_IMPL);
            boolean notCompiled =
                    dynamicClassLoader.notCompiled(nameOfEntity) || dynamicClassLoader.notCompiled(nameOfRepository)
                            || dynamicClassLoader.notCompiled(nameOfMapper) || dynamicClassLoader.notCompiled(
                            nameOfBaseRepositoryImpl);
            if (notCompiled) {
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.ENTITY));
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.REPOSITORY));
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.MAPPER));
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getBaseJavaFile(TypeEnum.REPOSITORY_IMPL));
                dynamicClassLoader.compile();
            }
            Class<?> entityClass = dynamicClassLoader.loadClass(nameOfEntity);
            Class<?> repositoryClass = dynamicClassLoader.loadClass(nameOfRepository);
            Class<?> mapperClass = dynamicClassLoader.loadClass(nameOfMapper);
            Class<?> baseRepositoryImplClass = dynamicClassLoader.loadClass(nameOfBaseRepositoryImpl);

            String className = module.concat(structureProperty.getRepositoryImplSuffix());
            TypeSpec typeSpec = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
                    .addJavadoc(parameterProperty.getAuthor()).addAnnotation(Repository.class)
                    .superclass(ParameterizedTypeName.get(baseRepositoryImplClass, entityClass, mapperClass))
                    .addSuperinterface(repositoryClass).build();

            JavaFile javaFile = JavaFile.builder(ModuleCommonUtil.getTypePackage(TypeEnum.REPOSITORY_IMPL), typeSpec)
                    .build();
            javaFile.writeTo(new File(structureProperty.getOutputPath()));

            dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.REPOSITORY_IMPL));

        }
        dynamicClassLoader.compile();
    }

}
