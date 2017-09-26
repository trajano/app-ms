package net.trajano.ms.engine.internal.jerseyspring;

import java.util.Set;
import java.util.function.Supplier;

import org.glassfish.jersey.inject.hk2.ImmediateHk2InjectionManager;
import org.glassfish.jersey.internal.inject.Binding;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ComponentProvider;
import org.jvnet.hk2.spring.bridge.api.SpringBridge;
import org.jvnet.hk2.spring.bridge.api.SpringIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import net.trajano.ms.engine.JaxRsRoute;

public class SpringJerseyProvider implements
    ComponentProvider {

    private static class SpringManagedBeanFactory implements
        Supplier {

        private final String beanName;

        private final ApplicationContext ctx;

        private final InjectionManager injectionManager;

        private SpringManagedBeanFactory(final ApplicationContext ctx,
            final InjectionManager injectionManager,
            final String beanName) {

            this.ctx = ctx;
            this.injectionManager = injectionManager;
            this.beanName = beanName;
        }

        @Override
        public Object get() {

            final Object bean = ctx.getBean(beanName);
            if (bean instanceof Advised) {
                try {
                    // Unwrap the bean and inject the values inside of it
                    final Object localBean = ((Advised) bean).getTargetSource().getTarget();
                    injectionManager.inject(localBean);
                } catch (final Exception e) {
                    // Ignore and let the injection happen as it normally would.
                    injectionManager.inject(bean);
                }
            } else {
                injectionManager.inject(bean);
            }
            return bean;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(JaxRsRoute.class);

    private ApplicationContext ctx;

    private InjectionManager injectionManager;

    @Override
    public boolean bind(final Class<?> component,
        final Set<Class<?>> providerContracts) {

        if (AnnotationUtils.findAnnotation(component, Component.class) != null) {
            final String[] beanNames = ctx.getBeanNamesForType(component);
            if (beanNames == null || beanNames.length != 1) {
                return false;
            }
            final String beanName = beanNames[0];

            final Binding<?, ?> binding = Bindings.supplier(new SpringManagedBeanFactory(ctx, injectionManager, beanName))
                .to(component)
                .to(providerContracts);
            injectionManager.register(binding);

            return true;
        }
        return false;
    }

    @Override
    public void done() {

    }

    @Override
    public void initialize(final InjectionManager injectionManager) {

        this.injectionManager = injectionManager;
        ctx = injectionManager.getInstance(ApplicationContext.class);
        LOG.debug("SpringJerseyProvider with context={}", ctx);

        final ImmediateHk2InjectionManager hk2InjectionManager = (ImmediateHk2InjectionManager) injectionManager;
        SpringBridge.getSpringBridge().initializeSpringBridge(hk2InjectionManager.getServiceLocator());
        final SpringIntoHK2Bridge springBridge = injectionManager.getInstance(SpringIntoHK2Bridge.class);
        springBridge.bridgeSpringBeanFactory(ctx);

        injectionManager.register(Bindings.injectionResolver(new AutowiredInjectResolver(ctx)));
        injectionManager.register(Bindings.service(ctx).to(ApplicationContext.class).named("SpringContext"));
    }

}
