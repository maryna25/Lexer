public enum State {
    HEX_N,
    START,
    IDENTIFIER,
    ERROR,
    ERR_COMM,
    NUMBER_D,
    NUMBER,
    DOUBLE_ASSIGN,
    DOT,
    ZERO,
    COMMENT,
    OPER_FIRST_SYM,
    MINUS,
    CHAR_LIT,
    CHAR_T,
    END_CHAR_LIT,
    STRING_LIT,
    STRING_LIT_BODY,
    STRING_LIT_SYM,
    NUMBER_DOT,
    HEX,
    ONE_LINE_COMMENT,
    MULTI_LINE_COMMENT,
    MULTI_LINE_COMMENT_STAR
}
