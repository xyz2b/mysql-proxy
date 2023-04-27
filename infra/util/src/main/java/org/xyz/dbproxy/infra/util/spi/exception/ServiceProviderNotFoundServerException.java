package org.xyz.dbproxy.infra.util.spi.exception;

import org.xyz.dbproxy.infra.util.exception.external.server.DbProxyServerException;

/**
 * Service provider not found exception.
 */
public final class ServiceProviderNotFoundServerException extends DbProxyServerException {

    private static final long serialVersionUID = -3730257541332863236L;

    private static final String ERROR_CATEGORY = "SPI";

    private static final int ERROR_CODE = 1;

    public ServiceProviderNotFoundServerException(final Class<?> clazz) {
        super(ERROR_CATEGORY, ERROR_CODE, String.format("No implementation class load from SPI `%s`.", clazz.getName()));
    }

    public ServiceProviderNotFoundServerException(final Class<?> clazz, final String type) {
        super(ERROR_CATEGORY, ERROR_CODE, String.format("No implementation class load from SPI `%s` with type `%s`.", clazz.getName(), type));
    }
}