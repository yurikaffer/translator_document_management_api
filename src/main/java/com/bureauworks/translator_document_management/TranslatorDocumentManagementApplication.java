package com.bureauworks.translator_document_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EntityScan(basePackages = "com.bureauworks.translator_document_management.entity")
@EnableJpaRepositories(basePackages = "com.bureauworks.translator_document_management.repository")
@EnableAsync
@EnableCaching
public class TranslatorDocumentManagementApplication {
	public static void main(String[] args) {
		SpringApplication.run(TranslatorDocumentManagementApplication.class, args);
	}
}
