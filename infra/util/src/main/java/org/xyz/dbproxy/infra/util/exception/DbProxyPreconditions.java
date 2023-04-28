package org.xyz.dbproxy.infra.util.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

/**
 * ShardingSphere preconditions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DbProxyPreconditions {

    /**
     * Ensures the truth of an expression involving the state of the calling instance.
     *
     * @param <T> type of exception
     * @param expectedExpression expected expression
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkState(final boolean expectedExpression, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (!expectedExpression) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param <T> type of exception
     * @param reference object reference to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkNotNull(final Object reference, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (null == reference) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
}
