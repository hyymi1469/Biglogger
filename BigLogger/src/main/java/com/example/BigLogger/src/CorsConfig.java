package com.example.BigLogger.src;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // API 경로 전체 허용
                .allowedOrigins(
                        "http://localhost:3000",   // React 개발 환경
                        "https://your-production-domain.com" // 실제 배포 도메인 (원하면 추가)
                )
                .allowedMethods("*")   // GET, POST, PUT, DELETE, OPTIONS 전부 허용
                .allowedHeaders("*")   // 헤더 제한 없음
                .allowCredentials(true); // 인증 정보 허용 (JWT, 쿠키 등)
    }
}