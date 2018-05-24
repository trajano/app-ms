package net.trajano.ms.vertx.beans;

import javax.validation.Validation;
import javax.validation.Validator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationProvider {

    @Bean
    public Validator beanValidationValidator() {

        return Validation.buildDefaultValidatorFactory().getValidator();
    }
}
