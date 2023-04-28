package org.xyz.dbproxy.infra.metadata.user;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import lombok.Getter;

/**
 * Grantee.
 * 授权哪些来源host可以用username访问数据库
 */
public final class Grantee {

    @Getter
    private final String username;

    @Getter
    private final String hostname;

    // 是否不限制来源host
    private final boolean isUnlimitedHost;

    private final int hashCode;

    private final String toString;

    public Grantee(final String username, final String hostname) {
        this.username = username;
        this.hostname = Strings.isNullOrEmpty(hostname) ? "%" : hostname;
        isUnlimitedHost = "%".equals(this.hostname);
        hashCode = isUnlimitedHost ? username.toUpperCase().hashCode() : Objects.hashCode(username.toUpperCase(), hostname.toUpperCase());
        toString = username + "@" + hostname;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Grantee) {
            Grantee grantee = (Grantee) obj;
            return grantee.username.equalsIgnoreCase(username) && isPermittedHost(grantee);
        }
        return false;
    }

    private boolean isPermittedHost(final Grantee grantee) {
        return isUnlimitedHost || grantee.hostname.equalsIgnoreCase(hostname);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return toString;
    }
}

