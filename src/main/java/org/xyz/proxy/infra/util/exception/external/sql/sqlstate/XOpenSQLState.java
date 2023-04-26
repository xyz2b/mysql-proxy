package org.xyz.proxy.infra.util.exception.external.sql.sqlstate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * XOpen standard SQL state.
 */
@RequiredArgsConstructor
@Getter
public enum XOpenSQLState implements SQLState {

    SUCCESSFUL_COMPLETION("00000"),

    GENERAL_WARNING("01000"),

    PRIVILEGE_NOT_GRANTED("01007"),

    CONNECTION_EXCEPTION("08000"),

    DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT("08004"),

    COMMUNICATION_LINK_FAILURE("08S01"),

    FEATURE_NOT_SUPPORTED("0A000"),

    MISMATCH_INSERT_VALUES_AND_COLUMNS("21S01"),

    INVALID_PARAMETER_VALUE("22023"),

    INVALID_TRANSACTION_STATE("25000"),

    INVALID_AUTHORIZATION_SPECIFICATION("28000"),

    INVALID_CURSOR_NAME("34000"),

    INVALID_CATALOG_NAME("3D000"),

    SYNTAX_ERROR("42000"),

    DUPLICATE("42S01"),

    NOT_FOUND("42S02"),

    CHECK_OPTION_VIOLATION("44000"),

    INVALID_COLUMN_NUMBER("HV008"),

    GENERAL_ERROR("HY000"),

    INVALID_DATA_TYPE("HY004");

    private final String value;
}

