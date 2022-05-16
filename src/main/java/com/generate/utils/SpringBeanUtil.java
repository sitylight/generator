package com.generate.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author derrick
 */
@Component
public class SpringBeanUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBeanUtil.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        return Optional.of(applicationContext.getBean(clazz))
                .orElseThrow(() -> new RuntimeException("can not find bean: " + clazz.getSimpleName()));
    }

    public static <T> T getBeanByName(String name, Class<T> classz) {
        return Optional.of(applicationContext.getBean(name, classz))
                .orElseThrow(() -> new RuntimeException("can not find bean: " + name));
    }

}
