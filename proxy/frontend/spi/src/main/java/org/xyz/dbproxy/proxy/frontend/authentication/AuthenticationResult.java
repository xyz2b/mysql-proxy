package org.xyz.dbproxy.proxy.frontend.authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Authentication result.
 */
@RequiredArgsConstructor
@Getter
public final class AuthenticationResult {

    private final String username;

    private final String hostname;

    private final String database;

    private final boolean finished;
}
