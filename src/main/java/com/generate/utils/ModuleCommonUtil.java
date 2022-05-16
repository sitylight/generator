package com.generate.utils;

import com.generate.enums.TypeEnum;
import com.generate.properties.StructureProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author derrick
 */
public class ModuleCommonUtil {
    private static final String JAVA_EXT = ".java";

    /**
     * 格式为 xxx.xxx.xxx :  com.example.Test
     */
    public static String getNameByModule(String module, TypeEnum type) {
        return StructureHolder.TYPE_PACKAGE_MAP.get(type) + "." + module + StructureHolder.TYPE_SUFFIX_MAP.get(type);
    }

    public static File getJavaFileByModule(String module, TypeEnum type) {
        String path = StructureHolder.TYPE_JAVA_PATH_MAP.get(type) + "/" + module + StructureHolder.TYPE_SUFFIX_MAP.get(type)
                + JAVA_EXT;
        return new File(path);
    }

    public static String getTypePackage(TypeEnum type) {
        return StructureHolder.TYPE_PACKAGE_MAP.get(type);
    }

    public static String getJavaFileNameByModule(String module, TypeEnum type) {
        return module.concat(StructureHolder.TYPE_SUFFIX_MAP.get(type));
    }

    public static String getBaseJavaFilePath(TypeEnum type) {
        return StructureHolder.BASE_JAVA_PATH_MAP.get(type);
    }

    public static File getBaseJavaFile(TypeEnum type) {
        return new File(StructureHolder.BASE_JAVA_PATH_MAP.get(type));
    }

    public static String getBaseName(TypeEnum type) {
        return StructureHolder.BASE_NAME_MAP.get(type);
    }
    private static class StructureHolder {
        public static final Map<TypeEnum, String> TYPE_JAVA_PATH_MAP = new HashMap<>();
        public static final Map<TypeEnum, String> TYPE_PACKAGE_MAP = new HashMap<>();
        public static final Map<TypeEnum, String> TYPE_SUFFIX_MAP = new HashMap<>();
        public static final Map<TypeEnum, String> BASE_JAVA_PATH_MAP = new HashMap<>();
        public static final Map<TypeEnum, String> BASE_NAME_MAP = new HashMap<>();

        static {
            StructureProperty structureProperty = SpringBeanUtil.getBean(StructureProperty.class);

            for (TypeEnum type : TypeEnum.values()) {
                TYPE_JAVA_PATH_MAP.put(type, typePath(type, structureProperty));

                TYPE_PACKAGE_MAP.put(type, typePackage(type, structureProperty));
            }

            TYPE_SUFFIX_MAP.put(TypeEnum.ENTITY, structureProperty.getEntitySuffix());
            TYPE_SUFFIX_MAP.put(TypeEnum.REPOSITORY, structureProperty.getRepositorySuffix());
            TYPE_SUFFIX_MAP.put(TypeEnum.REPOSITORY_IMPL, structureProperty.getRepositoryImplSuffix());
            TYPE_SUFFIX_MAP.put(TypeEnum.SERVICE, structureProperty.getServiceSuffix());
            TYPE_SUFFIX_MAP.put(TypeEnum.SERVICE_IMPL, structureProperty.getServiceImplSuffix());
            TYPE_SUFFIX_MAP.put(TypeEnum.MAPPER, structureProperty.getMapperSuffix());

            String basePath =
                    structureProperty.getOutputPath() + "/" + structureProperty.getPackageName().replace(".", "/") + "/"
                            + structureProperty.getBasePackage().replace(".", "/");

            BASE_JAVA_PATH_MAP.put(TypeEnum.ENTITY, basePath + "/BaseEntity.java");
            BASE_JAVA_PATH_MAP.put(TypeEnum.SERVICE, basePath + "/BaseService.java");
            BASE_JAVA_PATH_MAP.put(TypeEnum.SERVICE_IMPL, basePath + "/BaseServiceImpl.java");
            BASE_JAVA_PATH_MAP.put(TypeEnum.REPOSITORY, basePath + "/BaseRepository.java");
            BASE_JAVA_PATH_MAP.put(TypeEnum.REPOSITORY_IMPL, basePath + "/BaseRepositoryImpl.java");

            String basePackageName = structureProperty.getPackageName() + "." + structureProperty.getBasePackage();
            BASE_NAME_MAP.put(TypeEnum.ENTITY, basePackageName + ".BaseEntity");
            BASE_NAME_MAP.put(TypeEnum.REPOSITORY, basePackageName + ".BaseRepository");
            BASE_NAME_MAP.put(TypeEnum.REPOSITORY_IMPL, basePackageName + ".BaseRepositoryImpl");
            BASE_NAME_MAP.put(TypeEnum.SERVICE, basePackageName + ".BaseService");
            BASE_NAME_MAP.put(TypeEnum.SERVICE_IMPL, basePackageName + ".BaseServiceImpl");

        }

        private static String typePath(TypeEnum type, StructureProperty structureProperty) {
            String javaPath = structureProperty.getOutputPath() + "/" + structureProperty.getPackageName().replace(".", "/");
            boolean implNotEmpty = StringUtils.isNotEmpty(structureProperty.getImplPackage());
            String implDir = implNotEmpty ? "/" + structureProperty.getImplPackage() : "";
            switch (type) {
            case ENTITY:
                return javaPath + "/" + structureProperty.getEntityPackage().replace(".", "/");
            case SERVICE:
                return javaPath + "/" + structureProperty.getServicePackage().replace(".", "/");
            case SERVICE_IMPL:
                return javaPath + "/" + (structureProperty.getServicePackage() + implDir).replace(".", "/");
            case REPOSITORY:
                return javaPath + "/" + structureProperty.getRepositoryPackage().replace(".", "/");
            case REPOSITORY_IMPL:
                return javaPath + "/" + (structureProperty.getRepositoryPackage() + implDir).replace(".", "/");
            case MAPPER:
                return javaPath + "/" + structureProperty.getMapperPackage().replace(".", "/");
            default:
                return null;
            }
        }

        private static String typePackage(TypeEnum type, StructureProperty structureProperty) {
            String basePackage = structureProperty.getPackageName();
            String implPackage = StringUtils.isNotEmpty(structureProperty.getImplPackage()) ?
                    "." + structureProperty.getImplPackage() :
                    "";
            switch (type) {
            case ENTITY:
                return basePackage + "." + structureProperty.getEntityPackage();
            case SERVICE:
                return basePackage + "." + structureProperty.getServicePackage();
            case SERVICE_IMPL:
                return basePackage + "." + structureProperty.getServicePackage() + implPackage;
            case REPOSITORY:
                return basePackage + "." + structureProperty.getRepositoryPackage();
            case REPOSITORY_IMPL:
                return basePackage + "." + structureProperty.getRepositoryPackage() + implPackage;
            case MAPPER:
                return basePackage + "." + structureProperty.getMapperPackage();
            default:
                return null;
            }
        }
    }

}
