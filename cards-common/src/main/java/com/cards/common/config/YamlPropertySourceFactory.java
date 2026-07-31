package com.cards.common.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Spring {@link PropertySourceFactory} that loads a YAML file into the Environment as flat properties.
 * Used so {@code error-codes.yml} can be bound by {@code @ConfigurationProperties}.
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

    /**
     * Parses the given YAML resource and returns it as a {@link PropertiesPropertySource}.
     *
     * @param name     optional property-source name; if null, the resource filename is used
     * @param resource YAML file to load
     * @return a property source backed by the YAML contents (empty if parsing yields null)
     * @throws IOException if the resource cannot be read
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());
        Properties properties = factory.getObject();
        String sourceName = name != null ? name : resource.getResource().getFilename();
        return new PropertiesPropertySource(sourceName == null ? "error-codes" : sourceName,
                properties == null ? new Properties() : properties);
    }
}
