package org.xyz.proxy.db.protocol.core.packet.generic;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xyz.proxy.db.protocol.mysql.packet.MySQLPacket;
import org.xyz.proxy.db.protocol.mysql.payload.MySQLPacketPayload;
import org.xyz.proxy.infra.util.exception.external.sql.vendor.VendorError;

/**
 * ERR packet protocol for MySQL.
 *
 * @see <a href="https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_basic_err_packet.html">ERR Packet</a>
 */
@RequiredArgsConstructor
@Getter
public final class MySQLErrPacket implements MySQLPacket {

    /**
     * Header of ERR packet.
     */
    public static final int HEADER = 0xff;

    private static final String SQL_STATE_MARKER = "#";

    private final int errorCode;

    private final String sqlState;

    private final String errorMessage;

    public MySQLErrPacket(final VendorError vendorError, final Object... errorMessageArgs) {
        this(vendorError.getVendorCode(), vendorError.getSqlState().getValue(), String.format(vendorError.getReason(), errorMessageArgs));
    }

    public MySQLErrPacket(final MySQLPacketPayload payload) {
        Preconditions.checkArgument(HEADER == payload.readInt1(), "Header of MySQL ERR packet must be `0xff`.");
        errorCode = payload.readInt2();
        payload.readStringFix(1);
        sqlState = payload.readStringFix(5);
        errorMessage = payload.readStringEOF();
    }

    @Override
    public void write(final MySQLPacketPayload payload) {
        payload.writeInt1(HEADER);
        payload.writeInt2(errorCode);
        payload.writeStringFix(SQL_STATE_MARKER);
        payload.writeStringFix(sqlState);
        payload.writeStringEOF(errorMessage);
    }
}