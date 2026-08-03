package com.hean.consigueventas.oonabe.profileProfesional.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class ProfileImageWebConfig implements WebMvcConfigurer {

    private final Path storageDirectory;
    private final String urlPrefix;

    public ProfileImageWebConfig(
            @Value("${app.storage.profile-images-directory:images/profile-images}") String storageDirectory,
            @Value("${app.storage.profile-images-url-prefix:/images/profile-images}") String urlPrefix
    ) {
        this.storageDirectory = Paths.get(storageDirectory).toAbsolutePath().normalize();
        this.urlPrefix = urlPrefix;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations(storageDirectory.toUri().toString());
    }
}
