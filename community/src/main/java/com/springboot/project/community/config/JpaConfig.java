package com.springboot.project.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화 설정
 * - createdAt, updatedAt 필드를 자동으로 관리
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {

}

