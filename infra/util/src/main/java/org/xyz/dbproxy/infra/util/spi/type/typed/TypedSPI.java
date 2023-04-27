package org.xyz.dbproxy.infra.util.spi.type.typed;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

/**
 * Typed SPI.
 */
public interface TypedSPI {

    /**
     * Initialize SPI.
     *
     * @param props properties to be initialized
     */
    default void init(Properties props) {
    }

    /**
     * Get type.
     *
     * @return type
     */
    default String getType() {
        return "";
    }

    /**
     * Get type aliases.
     *
     * @return type aliases
     */
    default Collection<String> getTypeAliases() {
        return Collections.emptyList();
    }

    /**
     * Judge whether default service provider.
     *
     * @return is default service provider or not
     */
    default boolean isDefault() {
        return false;
    }
}

