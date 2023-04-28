package org.xyz.dbproxy.infra.datasource.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

/**
 * Pool configuration.
 */
@RequiredArgsConstructor
@Getter
public final class PoolConfiguration {

    private final Long connectionTimeoutMilliseconds;

    private final Long idleTimeoutMilliseconds;

    private final Long maxLifetimeMilliseconds;

    private final Integer maxPoolSize;

    private final Integer minPoolSize;

    private final Boolean readOnly;

    private final Properties customProperties;
}
