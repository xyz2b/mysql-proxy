package org.xyz.mysqlproxy.net.constants;

public class BackendConnState {
    // 后端连接尚未初始化，即收到MySQL服务端的握手包但是还未发送Client握手响应
    public static final int BACKEND_HANDSHAKE_RESPONSE_NOT_SEND = 0;
    // 后端连接初始化成功，即已发送Client握手响应，但还未收到了MySQL服务端返回的响应，认证还未通过
    public static final int BACKEND_HANDSHAKE_RESPONSE_SEND = 1;
}
