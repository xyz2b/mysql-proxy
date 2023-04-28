package org.xyz.dbproxy.infra.config.algorithm;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;

import java.util.Properties;

/**
 * Algorithm configuration.
 */
@Getter
public final class AlgorithmConfiguration {

    private final String type;

    // Java集合库提供了一个Properties来表示一组“配置”。由于历史遗留原因，Properties内部本质上是一个Hashtable
    private final Properties props;

    public AlgorithmConfiguration(final String type, final Properties props) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(type), "Type is required.");
        this.type = type;
        this.props = null == props ? new Properties() : props;
    }
}
