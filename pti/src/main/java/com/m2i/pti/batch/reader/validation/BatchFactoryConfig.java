package com.m2i.pti.batch.reader.validation;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchFactoryConfig {

    @Bean
    public StepBuilderFactory stepBuilderFactory(JobRepository jobRepository) {
        return new StepBuilderFactory(jobRepository);
    }

    @Bean
    public JobBuilderFactory jobBuilderFactory(JobRepository jobRepository) {
        return new JobBuilderFactory(jobRepository);
    }
}
