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
import tk.mybatis.mapper.common.Mapper;

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
public class MapperGenerator {
    private final DynamicClassLoader dynamicClassLoader;
    private final StructureProperty structureProperty;
    private final ParameterProperty parameterProperty;

    public void createMapperAndCompile(List<String> modules) throws ClassNotFoundException, IOException {
        Map<String, List<TypeEnum>> moduleTypesMap = new HashMap<>(16);
        for (String module : modules) {

            String nameOfRepository = ModuleCommonUtil.getJavaFileNameByModule(module, TypeEnum.MAPPER);
            String nameOfEntity = ModuleCommonUtil.getNameByModule(module, TypeEnum.ENTITY);
            if (dynamicClassLoader.notCompiled(nameOfEntity)) {
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.ENTITY));
                dynamicClassLoader.compile();
            }
            Class<?> clazz = dynamicClassLoader.loadClass(nameOfEntity);

            TypeSpec typeSpec = TypeSpec.interfaceBuilder(nameOfRepository).addModifiers(Modifier.PUBLIC)
                    .addJavadoc(parameterProperty.getAuthor())
                    .addSuperinterface(ParameterizedTypeName.get(Mapper.class, clazz)).build();

            JavaFile javaFile = JavaFile.builder(ModuleCommonUtil.getTypePackage(TypeEnum.MAPPER), typeSpec).build();
            javaFile.writeTo(new File(structureProperty.getOutputPath()));

            dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.MAPPER));

        }
        dynamicClassLoader.compile();
    }
}
