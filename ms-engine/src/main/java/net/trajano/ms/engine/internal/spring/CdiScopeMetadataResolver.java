package net.trajano.ms.engine.internal.spring;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationScopeMetadataResolver;
import org.springframework.context.annotation.ScopeMetadata;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;

public class CdiScopeMetadataResolver extends AnnotationScopeMetadataResolver {

    @Override
    public ScopeMetadata resolveScopeMetadata(final BeanDefinition definition) {

        if (definition instanceof AnnotatedBeanDefinition) {
            final AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) definition;
            final ScopeMetadata metadata = new ScopeMetadata();
            final Set<String> annotationTypes = beanDefinition.getMetadata().getAnnotationTypes();

            if (annotationTypes.contains(RequestScoped.class
                .getName())) {
                metadata.setScopeName(WebApplicationContext.SCOPE_REQUEST);
                metadata.setScopedProxyMode(ScopedProxyMode.TARGET_CLASS);
            } else if (annotationTypes
                .contains(SessionScoped.class.getName())) {
                metadata.setScopeName(WebApplicationContext.SCOPE_SESSION);
            } else if (annotationTypes
                .contains(ApplicationScoped.class.getName())) {
                metadata.setScopeName(WebApplicationContext.SCOPE_APPLICATION);
            } else {
                // do the regular Spring stuff..
                return super.resolveScopeMetadata(definition);
            }
            return metadata;
        } else {
            return super.resolveScopeMetadata(definition);
        }
    }
}
