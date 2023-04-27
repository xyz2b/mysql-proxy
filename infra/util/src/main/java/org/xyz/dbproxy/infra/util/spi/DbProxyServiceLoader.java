package org.xyz.dbproxy.infra.util.spi;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import org.xyz.dbproxy.infra.util.spi.annotation.SingletonSPI;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DbProxy service loader.
 * SPI加载器，加载服务实现者，封装了ServiceLoader.load()的逻辑
 * @param <T> type of service
 */
public final class DbProxyServiceLoader<T> {

    private static final Map<Class<?>, DbProxyServiceLoader<?>> LOADERS = new ConcurrentHashMap<>();

    private final Class<T> serviceInterface;

    @Getter
    private final Collection<T> services;

    private DbProxyServiceLoader(final Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
        validate();
        services = load();
    }

    private void validate() {
        Preconditions.checkNotNull(serviceInterface, "SPI interface is null.");
        Preconditions.checkArgument(serviceInterface.isInterface(), "SPI interface `%s` is not interface.", serviceInterface);
    }

    private Collection<T> load() {
        Collection<T> result = new LinkedList<>();
        for (T each : ServiceLoader.load(serviceInterface)) {
            result.add(each);
        }
        return result;
    }

    /**
     * Get service instances.
     *
     * @param serviceInterface service interface
     * @param <T> type of service interface
     * @return service instances
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">JDK-8161372</a>
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> getServiceInstances(final Class<T> serviceInterface) {
        DbProxyServiceLoader<?> result = LOADERS.get(serviceInterface);
        return (Collection<T>) (null != result ? result.getServiceInstances() : LOADERS.computeIfAbsent(serviceInterface, DbProxyServiceLoader::new).getServiceInstances());
    }

    private Collection<T> getServiceInstances() {
        return null == serviceInterface.getAnnotation(SingletonSPI.class) ? createNewServiceInstances() : getSingletonServiceInstances();
    }

    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private Collection<T> createNewServiceInstances() {
        Collection<T> result = new LinkedList<>();
        for (Object each : services) {
            result.add((T) each.getClass().getDeclaredConstructor().newInstance());
        }
        return result;
    }

    private Collection<T> getSingletonServiceInstances() {
        return services;
    }
}
