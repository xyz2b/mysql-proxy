package org.xyz.dbproxy.infra.util.reflection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Reflection utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtils {

    /**
     * Get field value.
     *
     * @param target target
     * @param fieldName field name
     * @param <T> type of field value
     * @return field value
     */
    public static <T> Optional<T> getFieldValue(final Object target, final String fieldName) {
        return findField(fieldName, target.getClass()).map(optional -> getFieldValue(target, optional));
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows(IllegalAccessException.class)
    private static <T> T getFieldValue(final Object target, final Field field) {
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        T result = (T) field.get(target);
        if (!accessible) {
            field.setAccessible(false);
        }
        return result;
    }

    private static Optional<Field> findField(final String fieldName, final Class<?> targetClass) {
        Class<?> currentTargetClass = targetClass;
        while (Object.class != currentTargetClass) {
            try {
                return Optional.of(currentTargetClass.getDeclaredField(fieldName));
            } catch (final NoSuchFieldException ignored) {
                currentTargetClass = currentTargetClass.getSuperclass();
            }
        }
        return Optional.empty();
    }

    /**
     * Get static field value.
     *
     * @param target target
     * @param fieldName field name
     * @param <T> type of field value
     * @return field value
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T> T getStaticFieldValue(final Class<?> target, final String fieldName) {
        Field field = target.getDeclaredField(fieldName);
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        T result = (T) field.get(target);
        if (!accessible) {
            field.setAccessible(false);
        }
        return result;
    }

    /**
     * Set static field value.
     *
     * @param target target
     * @param fieldName field name
     * @param value value
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static void setStaticFieldValue(final Class<?> target, final String fieldName, final Object value) {
        Field field = target.getDeclaredField(fieldName);
        boolean accessible = field.isAccessible();
        if (!accessible) {
            field.setAccessible(true);
        }
        field.set(target, value);
        if (!accessible) {
            field.setAccessible(false);
        }
    }

    /**
     * Invoke method.
     *
     * @param method method
     * @param target target
     * @param args arguments
     * @param <T> type of invoke result
     * @return invoke result
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T> T invokeMethod(final Method method, final Object target, final Object... args) {
        boolean accessible = method.isAccessible();
        if (!accessible) {
            method.setAccessible(true);
        }
        T result = (T) method.invoke(target, args);
        if (!accessible) {
            method.setAccessible(false);
        }
        return result;
    }
}
