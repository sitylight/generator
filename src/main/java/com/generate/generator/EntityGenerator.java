package com.generate.generator;

import com.generate.dynamic.DynamicClassLoader;
import com.generate.enums.TypeEnum;
import com.generate.properties.ParameterProperty;
import com.generate.properties.StructureProperty;
import com.generate.utils.DatabaseUtil;
import com.generate.utils.ModuleCommonUtil;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.lang.model.element.Modifier;
import javax.persistence.Column;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author derrick
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EntityGenerator {

    private final StructureProperty structureProperty;
    private final DynamicClassLoader dynamicClassLoader;
    private final ParameterProperty parameterProperty;
    public static Map<String, Class<?>> convertClass = new HashMap<>(16);

    private static final List<String> FILTER_OUTS = Lists.newArrayList("id");

    static {

        convertClass.put("varchar", String.class);
        convertClass.put("text", String.class);
        convertClass.put("int8", Long.class);
        convertClass.put("timestamp", LocalDateTime.class);
        convertClass.put("jsonb", HashMap.class);
        convertClass.put("int2", Integer.class);
        convertClass.put("int4", Integer.class);
        convertClass.put("date", LocalDate.class);
        convertClass.put("boolean", Boolean.class);
        convertClass.put("bool", Boolean.class);
    }

    public void createAndCompileEntityFile(Map<String, String> tableModuleMap) throws IOException, ClassNotFoundException {
        File parentDir = new File(
                structureProperty.getOutputPath() + "/" + structureProperty.getPackageName().replace(".", "/") + "/"
                        + structureProperty.getEntityPackage().replace(".", "/"));
        if (parentDir.exists() || parentDir.mkdirs()) {
            for (Map.Entry<String, String> entry : tableModuleMap.entrySet()) {
                String entityName = entry.getValue() + structureProperty.getEntitySuffix();
                createPojo(entry.getKey(), entityName);
                dynamicClassLoader.addCompileFile(ModuleCommonUtil.getJavaFileByModule(entry.getValue(), TypeEnum.ENTITY));
            }
            dynamicClassLoader.compile();
        }
    }

    private void createPojo(String tableName, String entityName) throws IOException, ClassNotFoundException {
        LinkedHashMap<String, String> columns = DatabaseUtil.getColumnsInTable(tableName);
        List<FieldSpec> fieldSpecs = new ArrayList<>(columns.size());
        columns.forEach((k, v) -> {
            if (!FILTER_OUTS.contains(k)) {
                FieldSpec fieldSpec = FieldSpec.builder(convertClass.get(v),
                                CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, k), Modifier.PRIVATE)
                        .addAnnotation(Column.class).build();
                fieldSpecs.add(fieldSpec);
            }
        });
        Class<?> baseEntityClass = dynamicClassLoader.loadClass(ModuleCommonUtil.getBaseName(TypeEnum.ENTITY));

        AnnotationSpec annotationSpec = AnnotationSpec.builder(EqualsAndHashCode.class).addMember("callSuper", "$L", true)
                .build();
        TypeSpec pojo = TypeSpec.classBuilder(entityName).addModifiers(Modifier.PUBLIC).addAnnotation(Data.class)
                .addJavadoc(parameterProperty.getAuthor()).addAnnotation(annotationSpec).addFields(fieldSpecs)
                .superclass(baseEntityClass).build();

        String entityPackage = structureProperty.getPackageName() + "." + structureProperty.getEntityPackage();

        JavaFile javaFile = JavaFile.builder(entityPackage, pojo).build();
        javaFile.writeTo(new File(structureProperty.getOutputPath()));
    }
}
