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
public class ServiceGenerator {
    private final StructureProperty structureProperty;
    private final DynamicClassLoader dynamicClassLoader;
    private final ParameterProperty parameterProperty;

    public void createAndCompile(List<String> modules) throws ClassNotFoundException, IOException {
        for (String module : modules) {
            String name = ModuleCommonUtil.getNameByModule(module, TypeEnum.ENTITY);
            String nameOfBaseService = ModuleCommonUtil.getBaseName(TypeEnum.SERVICE);
            if (dynamicClassLoader.notCompiled(name) || dynamicClassLoader.notCompiled(nameOfBaseService)) {
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.ENTITY));
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getBaseJavaFile(TypeEnum.SERVICE));
                dynamicClassLoader.compile();
            }
            Class<?> entityClass = dynamicClassLoader.loadClass(name);
            Class<?> baseServiceClass = dynamicClassLoader.loadClass(nameOfBaseService);

            TypeSpec typeSpec = TypeSpec.interfaceBuilder(module.concat(structureProperty.getServiceSuffix()))
                    .addModifiers(Modifier.PUBLIC).addJavadoc(parameterProperty.getAuthor())
                    .addSuperinterface(ParameterizedTypeName.get(baseServiceClass, entityClass)).build();

            String servicePackage = ModuleCommonUtil.getTypePackage(TypeEnum.SERVICE);
            JavaFile javaFile = JavaFile.builder(servicePackage, typeSpec).build();
            javaFile.writeTo(new File(structureProperty.getOutputPath()));
            dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.SERVICE));

        }
        dynamicClassLoader.compile();
    }
}
