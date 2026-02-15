package com.lz.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

public class ClassExcluder {

    private final List<Class<?>> excludedClasses = new ArrayList<>();

    public void excludeByRegex(String packageToScan, String classNameRegex) throws Exception {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);

        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(packageToScan) + "/" + "**/*.class";

        Resource[] resources = resolver.getResources(packageSearchPath);

        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader metadataReader = readerFactory.getMetadataReader(resource);
                String className = metadataReader.getClassMetadata().getClassName();

                if (className.matches(classNameRegex)) {
                    Class<?> clazz = Class.forName(className);
                    excludedClasses.add(clazz);
                }
            }
        }
    }

    private String resolveBasePackage(String basePackage) {
        return ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
    }

    public List<Class<?>> getExcludedClasses() {
        return excludedClasses;
    }
}