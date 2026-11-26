package com.cinema.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns(
                        "/admin/css/**",
                        "/admin/js/**",
                        "/admin/img/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/uploads/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String projectPath = Paths.get("").toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        "classpath:/static/uploads/",
                        "classpath:/static/admin/img/uploads/",
                        "classpath:/static/img/uploads/",
                        projectPath + "uploads/",
                        "file:" + projectPath + "uploads/"
                );

        registry.addResourceHandler("/img/**")
                .addResourceLocations(
                        "classpath:/static/img/",
                        "classpath:/static/admin/img/"
                );

        registry.addResourceHandler("/css/**", "/client/css/**")
                .addResourceLocations(
                        "classpath:/static/css/",
                        "classpath:/static/client/css/"
                );

        registry.addResourceHandler("/js/**", "/client/js/**")
                .addResourceLocations(
                        "classpath:/static/js/",
                        "classpath:/static/client/js/"
                );

        registry.addResourceHandler("/admin/css/**")
                .addResourceLocations("classpath:/static/admin/css/");

        registry.addResourceHandler("/admin/js/**")
                .addResourceLocations("classpath:/static/admin/js/");

        registry.addResourceHandler("/admin/img/**")
                .addResourceLocations("classpath:/static/admin/img/");
    }
}
