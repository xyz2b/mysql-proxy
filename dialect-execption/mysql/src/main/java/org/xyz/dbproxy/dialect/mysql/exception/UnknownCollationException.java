package org.xyz.dbproxy.dialect.mysql.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.dialect.exception.SQLDialectException;

/**
 * Unknown collation exception.
 */
@RequiredArgsConstructor
@Getter
public final class UnknownCollationException extends SQLDialectException {

    private static final long serialVersionUID = 6920150607711135228L;

    private final int collationId;
}

