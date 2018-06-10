public class Token {
    private String content;
    private Type tokenType;

    public Token(Type tokenType, String content){
        this.content = content;

        if(tokenType.equals(Type.IDENTIFIER)){
            if(Patterns.isLiteral(content)){
                this.tokenType = Type.LITERAL;
            }
            else
            if(Patterns.isKeyword(content)){
                this.tokenType = Type.KEYWORD;
            }
            else
                this.tokenType = tokenType;
        }  else
            this.tokenType = tokenType;

        if(tokenType.equals(Type.PUNCTUATION)){
            switch (content) {
                case "\n":
                    this.content = "\\" + "n";
                    break;
                case "\t":
                    this.content = "\\" + "t";
                    break;
            }
        }

    }

    public Type getTokenType() {
        return tokenType;
    }

    public String getContent() {
        return content;
    }
}
