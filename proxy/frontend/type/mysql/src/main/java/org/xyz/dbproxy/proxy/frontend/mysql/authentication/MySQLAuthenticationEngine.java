package org.xyz.dbproxy.proxy.frontend.mysql.authentication;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.xyz.dbproxy.authority.rule.AuthorityRule;
import org.xyz.dbproxy.db.protocol.constant.CommonConstants;
import org.xyz.dbproxy.db.protocol.mysql.constant.*;
import org.xyz.dbproxy.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.xyz.dbproxy.db.protocol.mysql.packet.handshake.*;
import org.xyz.dbproxy.db.protocol.mysql.payload.MySQLPacketPayload;
import org.xyz.dbproxy.db.protocol.payload.PacketPayload;
import org.xyz.dbproxy.infra.metadata.user.DbProxyUser;
import org.xyz.dbproxy.infra.metadata.user.Grantee;
import org.xyz.dbproxy.proxy.backend.context.ProxyContext;
import org.xyz.dbproxy.proxy.frontend.authentication.*;
import org.xyz.dbproxy.proxy.frontend.connection.ConnectionIdGenerator;
import org.xyz.dbproxy.proxy.frontend.mysql.authentication.authenticator.MySQLAuthenticatorType;
import org.xyz.dbproxy.proxy.frontend.mysql.command.query.binary.MySQLStatementIDGenerator;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.AccessDeniedException;
import java.util.Optional;

/**
 * Authentication engine for MySQL.
 * MYSQL认证引擎
 */
@Slf4j
public final class MySQLAuthenticationEngine implements AuthenticationEngine {

    private final MySQLAuthenticationPluginData authPluginData = new MySQLAuthenticationPluginData();

    private MySQLConnectionPhase connectionPhase = MySQLConnectionPhase.INITIAL_HANDSHAKE;

    private byte[] authResponse;

    private AuthenticationResult currentAuthResult;

    // https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_connection_phase.html#sect_protocol_connection_phase_initial_handshake
    // 握手请求
    @Override
    public int handshake(final ChannelHandlerContext context) {
        // 生成 连接ID（thread id）
        int result = ConnectionIdGenerator.getInstance().nextId();
        connectionPhase = MySQLConnectionPhase.AUTH_PHASE_FAST_PATH;
        context.writeAndFlush(new MySQLHandshakePacket(result, authPluginData));
        // 给当前连接ID绑定一个SQL语句ID生成器
        MySQLStatementIDGenerator.getInstance().registerConnection(result);
        return result;
    }

    @Override
    public AuthenticationResult authenticate(final ChannelHandlerContext context, final PacketPayload payload) {
        AuthorityRule rule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        if (MySQLConnectionPhase.AUTH_PHASE_FAST_PATH == connectionPhase) { // 当前阶段处于MySQLConnectionPhase.AUTH_PHASE_FAST_PATH，解析MySQLHandshakeResponse41Packet
            currentAuthResult = authenticatePhaseFastPath(context, payload, rule);
            if (!currentAuthResult.isFinished()) {
                return currentAuthResult;
            }
        } else if (MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH == connectionPhase) {    // 当前阶段处于MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH，解析MySQLAuthSwitchResponsePacket
            authenticateMismatchedMethod((MySQLPacketPayload) payload);
        }
        Grantee grantee = new Grantee(currentAuthResult.getUsername(), getHostAddress(context));
        if (!login(rule, grantee, authResponse)) {
            throw new AccessDeniedException(currentAuthResult.getUsername(), grantee.getHostname(), 0 != authResponse.length);
        }
        if (!authorizeDatabase(rule, grantee, currentAuthResult.getDatabase())) {
            throw new DatabaseAccessDeniedException(currentAuthResult.getUsername(), grantee.getHostname(), currentAuthResult.getDatabase());
        }
        writeOKPacket(context);
        return AuthenticationResultBuilder.finished(grantee.getUsername(), grantee.getHostname(), currentAuthResult.getDatabase());
    }

    private AuthenticationResult authenticatePhaseFastPath(final ChannelHandlerContext context, final PacketPayload payload, final AuthorityRule rule) {
        MySQLHandshakeResponse41Packet handshakeResponsePacket;
        try {
            // 解析握手包响应
            handshakeResponsePacket = new MySQLHandshakeResponse41Packet((MySQLPacketPayload) payload);
        } catch (IndexOutOfBoundsException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Received bad handshake from client {}: \n{}", context.channel(), ByteBufUtil.prettyHexDump(payload.getByteBuf().resetReaderIndex()));
            }
            throw new HandshakeException();
        }
        String database = handshakeResponsePacket.getDatabase();
        authResponse = handshakeResponsePacket.getAuthResponse();
        setCharacterSet(context, handshakeResponsePacket);
        if (!Strings.isNullOrEmpty(database) && !ProxyContext.getInstance().databaseExists(database)) {
            throw new UnknownDatabaseException(database);
        }
        String username = handshakeResponsePacket.getUsername();
        String hostname = getHostAddress(context);
        // 当optional值不存在时，调用orElseGet()中接口调用的返回值，如果optional的值存在时返回optional的值
        // 在已授权的用户中找寻是否有当前用户，没有的话就生成一个DbProxyUser实例
        DbProxyUser user = rule.findUser(new Grantee(username, hostname)).orElseGet(() -> new DbProxyUser(username, "", hostname));
        // 使用认证器工厂实例化认证器，MySQLAuthenticator，这里针对MYSQL就实现了两种Authentication Methods的认证器，Native Authentication和Clear text client plugin(明文)，MYSQL5.6之后默认使用Native Authentication
        Authenticator authenticator = new AuthenticatorFactory<>(MySQLAuthenticatorType.class, rule).newInstance(user);
        // 如果服务器的用户密码加密方式和客户端使用的auth plugin不同，就需要让client切换加密插件，进入MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH阶段，发送MySQLAuthSwitchRequestPacket
        if (isClientPluginAuthenticate(handshakeResponsePacket) && !authenticator.getAuthenticationMethod().getMethodName().equals(handshakeResponsePacket.getAuthPluginName())) {
            connectionPhase = MySQLConnectionPhase.AUTHENTICATION_METHOD_MISMATCH;
            context.writeAndFlush(new MySQLAuthSwitchRequestPacket(authenticator.getAuthenticationMethod().getMethodName(), authPluginData));
            return AuthenticationResultBuilder.continued(username, hostname, database);
        }
        return AuthenticationResultBuilder.finished(username, hostname, database);
    }

    private void setCharacterSet(final ChannelHandlerContext context, final MySQLHandshakeResponse41Packet handshakeResponsePacket) {
        MySQLCharacterSet characterSet = MySQLCharacterSet.findById(handshakeResponsePacket.getCharacterSet());
        context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).set(characterSet.getCharset());
        context.channel().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).set(characterSet);
    }

    private boolean isClientPluginAuthenticate(final MySQLHandshakeResponse41Packet packet) {
        return 0 != (packet.getCapabilityFlags() & MySQLCapabilityFlag.CLIENT_PLUGIN_AUTH.getValue());
    }

    private void authenticateMismatchedMethod(final MySQLPacketPayload payload) {
        authResponse = new MySQLAuthSwitchResponsePacket(payload).getAuthPluginResponse();
    }

    private boolean login(final AuthorityRule rule, final Grantee grantee, final byte[] authenticationResponse) {
        Optional<DbProxyUser> user = rule.findUser(grantee);
        // 这里才真正调用认证器的authenticate方法校验密码是否争取
        return user.isPresent()
                && new AuthenticatorFactory<>(MySQLAuthenticatorType.class, rule).newInstance(user.get()).authenticate(user.get(), new Object[]{authenticationResponse, authPluginData});
    }

    private boolean authorizeDatabase(final AuthorityRule rule, final Grantee grantee, final String databaseName) {
        // 验证对应的用户是否有访问database的权限
        return null == databaseName || new AuthorityChecker(rule, grantee).isAuthorized(databaseName);
    }

    private String getHostAddress(final ChannelHandlerContext context) {
        if (context.channel() instanceof EpollDomainSocketChannel) {
            return context.channel().parent().localAddress().toString();
        }
        SocketAddress socketAddress = context.channel().remoteAddress();
        return socketAddress instanceof InetSocketAddress ? ((InetSocketAddress) socketAddress).getAddress().getHostAddress() : socketAddress.toString();
    }

    private void writeOKPacket(final ChannelHandlerContext context) {
        context.writeAndFlush(new MySQLOKPacket(MySQLStatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
    }
}
