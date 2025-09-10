package com.liam.gantt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 관련 설정 클래스
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.liam.gantt.repository")
public class JpaConfig {
    // JPA Auditing 기능 활성화
    // 이를 통해 @CreatedDate, @LastModifiedDate 어노테이션이 자동으로 동작
}