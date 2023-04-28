package org.xyz.dbproxy.infra.metadata.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * DbProxy user.
 */
@Getter
@EqualsAndHashCode(of = "grantee")  // 此注解会生成equals(Object other) 和 hashCode()方法。通过参数of指定仅使用哪些属性
// DbProxy上面配置的用户
public final class DbProxyUser {

    private final Grantee grantee;

    private final String password;

    private final String authenticationMethodName;

    public DbProxyUser(final String username, final String password, final String hostname) {
        this(username, password, hostname, "");
    }

    public DbProxyUser(final String username, final String password, final String hostname, final String authenticationMethodName) {
        grantee = new Grantee(username, hostname);
        this.password = password;
        this.authenticationMethodName = authenticationMethodName;
    }
}
