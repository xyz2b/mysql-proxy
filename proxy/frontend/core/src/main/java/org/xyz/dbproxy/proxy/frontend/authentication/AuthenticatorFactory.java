package org.xyz.dbproxy.proxy.frontend.authentication;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.xyz.dbproxy.authority.rule.AuthorityRule;
import org.xyz.dbproxy.infra.metadata.user.DbProxyUser;

import java.util.Arrays;

/**
 * Authenticator factory.
 *
 * @param <E> type of enum
 */
@RequiredArgsConstructor
public final class AuthenticatorFactory<E extends Enum<E> & AuthenticatorType> {

    private final Class<E> authenticatorTypeClass;

    private final AuthorityRule rule;

    /**
     * Create new instance of authenticator.
     *
     * @param user user
     * @return new instance of authenticator
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public Authenticator newInstance(final DbProxyUser user) {
        E authenticatorType = getAuthenticatorType(rule.getAuthenticatorType(user));
        try {
            return authenticatorType.getAuthenticatorClass().getConstructor().newInstance();
        } catch (final NoSuchMethodException ignored) {
            return authenticatorType.getAuthenticatorClass().getConstructor(AuthorityRule.class).newInstance(rule);
        }
    }

    private E getAuthenticatorType(final String authenticationMethod) {
        try {
            return E.valueOf(authenticatorTypeClass, authenticationMethod.toUpperCase());
        } catch (final IllegalArgumentException ignored) {
            return Arrays.stream(authenticatorTypeClass.getEnumConstants()).filter(AuthenticatorType::isDefault).findAny().orElseThrow(IllegalArgumentException::new);
        }
    }
}