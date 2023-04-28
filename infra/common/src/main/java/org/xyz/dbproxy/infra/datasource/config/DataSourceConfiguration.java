package org.xyz.dbproxy.infra.datasource.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Data source configuration.
 */
@RequiredArgsConstructor
@Getter
public final class DataSourceConfiguration {

    private final ConnectionConfiguration connection;

    private final PoolConfiguration pool;
}

