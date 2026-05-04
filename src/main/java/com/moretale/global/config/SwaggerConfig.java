package com.moretale.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securitySchemeName);

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("로컬 개발 서버");

        Server deployServer = new Server()
                .url(serverUrl)
                .description("배포 서버 (Render / GCP)");

        return new OpenAPI()
                .info(new Info()
                        .title("MORETALE API")
                        .description("""
                                MORETALE API 명세서입니다.
                                
                                **인증 방식**
                                - Google OAuth2 로그인 후 발급받은 JWT 토큰을 사용합니다.
                                - Authorize 버튼을 눌러 `Bearer {토큰}` 형식으로 입력하세요.
                                """)
                        .version("v1.0.0"))
                .servers(List.of(localServer, deployServer))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
