package org.xyz.dbproxy.db.protocol.constant;

import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Common constants for protocol.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommonConstants {

    // key: java.nio.charset.Charset，设置到channel属性里
    public static final AttributeKey<Charset> CHARSET_ATTRIBUTE_KEY = AttributeKey.valueOf(Charset.class.getName());

    public static final AtomicReference<String> PROXY_VERSION = new AtomicReference<>();
}