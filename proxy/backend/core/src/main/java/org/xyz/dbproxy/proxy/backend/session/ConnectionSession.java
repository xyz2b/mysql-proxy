package org.xyz.dbproxy.proxy.backend.session;

import lombok.Getter;
import lombok.Setter;

/**
 * Connection session.
 */
@Getter
@Setter
public final class ConnectionSession {
    private volatile int connectionId;

}
