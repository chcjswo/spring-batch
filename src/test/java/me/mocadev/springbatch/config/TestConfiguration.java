package me.mocadev.springbatch.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author chcjswo
 * @version 1.0.0
 * @blog https://mocadev.tistory.com
 * @github https://github.com/chcjswo
 * @since 2022-05-12
 **/
@Configuration
@EnableBatchProcessing
@EnableAutoConfiguration
@EnableJpaAuditing
@EntityScan("me.mocadev.springbatch.pass.repository")
@EnableJpaRepositories("me.mocadev.springbatch.pass.repository")
@EnableTransactionManagement
public class TestConfiguration {

}
