package org.xyz.dbproxy.proxy.frontend.authentication;

import io.netty.channel.ChannelHandlerContext;
import org.xyz.dbproxy.db.protocol.payload.PacketPayload;

/**
 * Authentication engine.
 */
public interface AuthenticationEngine {

    /**
     * Handshake.
     *
     * @param context channel handler context
     * @return connection ID
     */
    int handshake(ChannelHandlerContext context);

    /**
     * Authenticate.
     *
     * @param context channel handler context
     * @param payload packet payload
     * @return authentication result
     */
    AuthenticationResult authenticate(ChannelHandlerContext context, PacketPayload payload);
}
