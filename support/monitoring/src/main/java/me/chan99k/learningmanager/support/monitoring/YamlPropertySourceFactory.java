package me.chan99k.learningmanager.support.monitoring;

import java.util.Objects;
import java.util.Properties;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

public class YamlPropertySourceFactory implements PropertySourceFactory {

	@Override
	public PropertySource<?> createPropertySource(String name, EncodedResource encodedResource) {
		YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
		factory.setResources(encodedResource.getResource());
		Properties properties = factory.getObject();
		String sourceName = name != null ? name : encodedResource.getResource().getFilename();
		return new PropertiesPropertySource(Objects.requireNonNull(sourceName), Objects.requireNonNull(properties));
	}
}
