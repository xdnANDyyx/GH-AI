package com.ruoyi.web.core.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer";

    @Value("${knife4j.swagger.doc.title:若依管理系统}")
    private String title;

    @Value("${knife4j.swagger.doc.description:若依管理系统接口文档}")
    private String description;

    @Value("${knife4j.swagger.doc.version:3.9.1}")
    private String version;

    @Value("${knife4j.swagger.doc.external.title:官方文档}")
    private String externalDocTitle;

    @Value("${knife4j.swagger.doc.external.url:http://doc.ruoyi.vip}")
    private String externalDocUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version(version)
                        .description(description)
                        .contact(new Contact()
                                .name("若依")
                                .url("http://www.ruoyi.vip")
                                .email("admin@ruoyi.vip"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://mit-license.org/")))
                .externalDocs(new ExternalDocumentation()
                        .description(externalDocTitle)
                        .url(externalDocUrl))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .schemaRequirement(SECURITY_SCHEME_NAME, new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("请输入JWT Token"));
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("1. 系统管理")
                .packagesToScan("com.ruoyi.web.controller.system")
                .pathsToMatch("/system/**", "/login", "/register", "/captchaImage")
                .build();
    }

    @Bean
    public GroupedOpenApi businessApi() {
        return GroupedOpenApi.builder()
                .group("2. 业务模块")
                .packagesToScan("com.ruoyi.web.controller.module")
                .pathsToMatch("/module/**", "/pay/**")
                .build();
    }

    @Bean
    public GroupedOpenApi commonApi() {
        return GroupedOpenApi.builder()
                .group("3. 公共接口")
                .packagesToScan("com.ruoyi.web.controller.common", "com.ruoyi.web.controller.convenient")
                .pathsToMatch("/common/**", "/convenient/**")
                .build();
    }

    @Bean
    public GroupedOpenApi toolApi() {
        return GroupedOpenApi.builder()
                .group("4. 工具接口")
                .packagesToScan("com.ruoyi.web.controller.tool")
                .pathsToMatch("/tool/**")
                .build();
    }


    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("5. 用户接口")
                .packagesToScan("com.ruoyi.web.controller.user")
                .pathsToMatch("/customer/**")
                .build();
    }
}
