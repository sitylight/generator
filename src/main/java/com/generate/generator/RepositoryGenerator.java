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

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author derrick
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RepositoryGenerator {
    private final StructureProperty structureProperty;
    private final DynamicClassLoader dynamicClassLoader;
    private final ParameterProperty parameterProperty;

    public void genRepositoryInterface(List<String> modules) throws ClassNotFoundException, IOException {
        for (String module : modules) {
            String name = ModuleCommonUtil.getNameByModule(module, TypeEnum.ENTITY);
            String baseRepositoryName = ModuleCommonUtil.getBaseName(TypeEnum.REPOSITORY);
            if (dynamicClassLoader.notCompiled(name) || dynamicClassLoader.notCompiled(baseRepositoryName)) {
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.ENTITY));
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getBaseJavaFile(TypeEnum.REPOSITORY));
                dynamicClassLoader.compile();
            }
            Class<?> entityClass = dynamicClassLoader.loadClass(name);
            Class<?> baseRepositoryClass = dynamicClassLoader.loadClass(baseRepositoryName);

            String repositoryName = module.concat(structureProperty.getRepositorySuffix());
            TypeSpec typeSpec = TypeSpec.interfaceBuilder(repositoryName).addModifiers(Modifier.PUBLIC)
                    .addJavadoc(parameterProperty.getAuthor())
                    .addSuperinterface(ParameterizedTypeName.get(baseRepositoryClass, entityClass)).build();
            String repositoryPackage = structureProperty.getPackageName() + "." + structureProperty.getRepositoryPackage();
            JavaFile javaFile = JavaFile.builder(repositoryPackage, typeSpec).build();
            javaFile.writeTo(new File(structureProperty.getOutputPath()));

            dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.REPOSITORY));

        }
        dynamicClassLoader.compile();
    }
}
