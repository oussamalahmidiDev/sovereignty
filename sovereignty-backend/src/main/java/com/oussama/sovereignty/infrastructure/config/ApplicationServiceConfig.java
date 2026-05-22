package com.oussama.sovereignty.infrastructure.config;

import com.oussama.sovereignty.application.common.UseCase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(
        basePackages = "com.oussama.sovereignty.application",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = UseCase.class
        )
)
public class ApplicationServiceConfig {}
