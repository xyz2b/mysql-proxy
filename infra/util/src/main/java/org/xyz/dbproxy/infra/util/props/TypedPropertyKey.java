package org.xyz.dbproxy.infra.util.props;

/**
 * Typed property key.
 */
public interface TypedPropertyKey {

    /**
     * Get property key.
     *
     * @return property key
     */
    String getKey();

    /**
     * Get default property value.
     *
     * @return default property value
     */
    String getDefaultValue();

    /**
     * Get property type.
     *
     * @return property type
     */
    Class<?> getType();
}
