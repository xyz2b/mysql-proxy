package org.xyz.dbproxy.db.protocol.mysql.packet.generic;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.db.protocol.mysql.packet.MySQLPacket;
import org.xyz.dbproxy.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * EOF packet protocol for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_eof_packet.html">EOF Packet</a>
 */
@RequiredArgsConstructor
@Getter
public final class MySQLEofPacket implements MySQLPacket {

    /**
     * Header of EOF packet.
     */
    public static final int HEADER = 0xfe;

    private final int warnings;

    private final int statusFlags;

    public MySQLEofPacket(final int statusFlags) {
        this(0, statusFlags);
    }

    // 将MySQLPacketPayload 转成 MySQLEofPacket（decode）

    public MySQLEofPacket(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(HEADER == payload.readInt1(), "Header of MySQL EOF packet must be `0xfe`.");
        warnings = payload.readInt2();
        statusFlags = payload.readInt2();
    }

    // 将MySQLEofPacket 转成 MySQLPacketPayload（encode）
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeInt2(warnings);
        payload.writeInt2(statusFlags);
    }
}

