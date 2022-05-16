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
import org.springframework.stereotype.Service;

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
public class ServiceImplGenerator {
    private final StructureProperty structureProperty;
    private final DynamicClassLoader dynamicClassLoader;
    private final ParameterProperty parameterProperty;

    public void createAndCompile(List<String> modules) throws ClassNotFoundException, IOException {
        Map<String, List<TypeEnum>> moduleTypesMap = new HashMap<>(16);
        for (String module : modules) {
            String nameOfEntity = ModuleCommonUtil.getNameByModule(module, TypeEnum.ENTITY);
            String nameOfRepository = ModuleCommonUtil.getNameByModule(module, TypeEnum.REPOSITORY);
            String nameOfService = ModuleCommonUtil.getNameByModule(module, TypeEnum.SERVICE);
            String nameOfBaseServiceImpl = ModuleCommonUtil.getBaseName(TypeEnum.SERVICE_IMPL);
            boolean notCompiled =
                    dynamicClassLoader.notCompiled(nameOfEntity) || dynamicClassLoader.notCompiled(nameOfRepository)
                            || dynamicClassLoader.notCompiled(nameOfService) || dynamicClassLoader.notCompiled(
                            nameOfBaseServiceImpl);
            if (notCompiled) {
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.ENTITY));
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.REPOSITORY));
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.SERVICE));
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getBaseJavaFile(TypeEnum.SERVICE_IMPL));
                dynamicClassLoader.compile();
            }
            Class<?> entityClass = dynamicClassLoader.loadClass(nameOfEntity);
            Class<?> repositoryClass = dynamicClassLoader.loadClass(nameOfRepository);
            Class<?> serviceClass = dynamicClassLoader.loadClass(nameOfService);
            Class<?> baseServiceImplClass = dynamicClassLoader.loadClass(nameOfBaseServiceImpl);

            String className = module.concat(structureProperty.getServiceImplSuffix());

            TypeSpec typeSpec = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC)
                    .addJavadoc(parameterProperty.getAuthor()).addAnnotation(Service.class)
                    .superclass(ParameterizedTypeName.get(baseServiceImplClass, entityClass, repositoryClass))
                    .addSuperinterface(ParameterizedTypeName.get(serviceClass)).build();

            JavaFile javaFile = JavaFile.builder(ModuleCommonUtil.getTypePackage(TypeEnum.SERVICE_IMPL), typeSpec).build();
            javaFile.writeTo(new File(structureProperty.getOutputPath()));

            dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(module, TypeEnum.SERVICE_IMPL));

        }
        dynamicClassLoader.compile();
    }
}
