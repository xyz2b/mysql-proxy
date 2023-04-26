package org.xyz.proxy.db.protocol.mysql.constant;

import io.netty.util.AttributeKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * MySQL constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLConstants {

    public static final AttributeKey<AtomicInteger> MYSQL_SEQUENCE_ID = AttributeKey.valueOf("MYSQL_SEQUENCE_ID");

    public static final AttributeKey<MySQLCharacterSet> MYSQL_CHARACTER_SET_ATTRIBUTE_KEY = AttributeKey.valueOf(MySQLCharacterSet.class.getName());

    public static final AttributeKey<Integer> MYSQL_OPTION_MULTI_STATEMENTS = AttributeKey.valueOf("MYSQL_OPTION_MULTI_STATEMENTS");
}
