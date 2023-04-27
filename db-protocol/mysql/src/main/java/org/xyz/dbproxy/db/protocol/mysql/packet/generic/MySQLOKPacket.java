package org.xyz.dbproxy.db.protocol.mysql.packet.generic;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.dbproxy.db.protocol.mysql.packet.MySQLPacket;
import org.xyz.dbproxy.db.protocol.mysql.payload.MySQLPacketPayload;

/**
 * OK packet protocol for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_ok_packet.html">OK Packet</a>
 */
@RequiredArgsConstructor
@Getter
public final class MySQLOKPacket implements MySQLPacket {

    /**
     * Header of OK packet.
     */
    public static final int HEADER = 0x00;

    private final long affectedRows;

    private final long lastInsertId;

    private final int statusFlag;

    private final int warnings;

    private final String info;

    public MySQLOKPacket(final int statusFlag) {
        this(0L, 0L, statusFlag, 0, "");
    }

    public MySQLOKPacket(final long affectedRows, final long lastInsertId, final int statusFlag) {
        this(affectedRows, lastInsertId, statusFlag, 0, "");
    }

    // 将MySQLPacketPayload 转成 MySQLOKPacket（decode）
    public MySQLOKPacket(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(HEADER == payload.readInt1(), "Header of MySQL OK packet must be `0x00`.");
        affectedRows = payload.readIntLenenc();
        lastInsertId = payload.readIntLenenc();
        statusFlag = payload.readInt2();
        warnings = payload.readInt2();
        info = payload.readStringEOF();
    }

    // 将MySQLOKPacket 转成 MySQLPacketPayload（encode）
    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeIntLenenc(affectedRows);
        payload.writeIntLenenc(lastInsertId);
        payload.writeInt2(statusFlag);
        payload.writeInt2(warnings);
        payload.writeStringEOF(info);
    }
}
