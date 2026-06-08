package com.amalitech.todoApi.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;
import java.util.Properties;

/**
 * Allows @PropertySource to load YAML files in non-Boot Spring applications.
 * Usage: @PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) {
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(resource.getResource());

        Properties properties = Objects.requireNonNull(yaml.getObject(), "Failed to load YAML properties");
        String sourceName = (name != null && !name.isEmpty())
                ? name
                : resource.getResource().getFilename();

        return new PropertiesPropertySource(Objects.requireNonNull(sourceName), properties);
    }
}
