package net.trajano.ms.engine.test;

import net.trajano.ms.engine.internal.spring.CdiScopeMetadataResolver;
import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ScopeMetadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class CdiScopeMetadataResolverTest {

    @Test
    public void testNonAnnotationBeanDefinition() {

        BeanDefinition bean = mock(BeanDefinition.class);
        final CdiScopeMetadataResolver cdiScopeMetadataResolver = new CdiScopeMetadataResolver();
        final ScopeMetadata scopeMetadata = cdiScopeMetadataResolver.resolveScopeMetadata(bean);
        assertNotNull(scopeMetadata);
        assertEquals("singleton", scopeMetadata.getScopeName());

    }

}
