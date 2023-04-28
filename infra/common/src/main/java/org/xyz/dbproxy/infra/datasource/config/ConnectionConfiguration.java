package org.xyz.dbproxy.infra.datasource.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Connection configuration.
 */
@RequiredArgsConstructor
@Getter
public final class ConnectionConfiguration {

    private final String url;

    private final String username;

    private final String password;
}
