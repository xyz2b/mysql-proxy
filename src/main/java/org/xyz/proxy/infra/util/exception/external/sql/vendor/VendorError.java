package org.xyz.proxy.infra.util.exception.external.sql.vendor;

import org.xyz.proxy.infra.util.exception.external.sql.sqlstate.SQLState;

/**
 * Vendor error.
 */
public interface VendorError {

    /**
     * Get SQL state.
     *
     * @return SQL state
     */
    SQLState getSqlState();

    /**
     * Get database vendor code.
     *
     * @return vendor code
     */
    int getVendorCode();

    /**
     * Get reason.
     *
     * @return reason
     */
    String getReason();
}

