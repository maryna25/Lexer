import java.io.*;
import java.util.ArrayList;

// numbers like 1.
// eof
// <<, <<<

public class Lexer {

    ArrayList<Token> tokens = new ArrayList<>();
    String buffer = "";
    State curState = State.START;
    boolean fileEnded = false;

    public Lexer(String filePath){
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            int c;

            while ((c = br.read()) != -1) {
                analyzeChar((char)c);
            }

            br.close();
            fileEnded = true;
            analyzeChar('\n');
            tokens.remove(tokens.size() - 1);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Token> getTokens(){
        return  tokens;
    }


    public void analyzeChar(Character character){
        boolean analyzed = false;
        while (!analyzed) {
            switch (curState) {
                case START: {
                    if (character == '!' || character == '%' || character == '=' || character == '*' || character == '>' || character == '<')
                        curState = State.DOUBLE_ASSIGN;
                    else if (character >= '1' && character <= '9')
                        curState = State.NUMBER;
                    else if (character == '.')
                        curState = State.DOT;
                    else if ((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || character == '_')
                        curState = State.IDENTIFIER;
                    else if (character == '0')
                        curState = State.ZERO;
                    else if (character == '?' || character == ':') {
                        tokens.add(new Token(Type.OPERATOR, Character.toString(character)));
                        setStart();
                    }
                    else if (character == '/')
                        curState = State.COMMENT;
                    else if (character == '+' || character == '&' || character == '|')
                        curState = State.OPER_FIRST_SYM;
                    else if (character == '-')
                        curState = State.MINUS;
                    else if (character == '\'')
                        curState = State.CHAR_LIT;
                    else if (character == '\"')
                        curState = State.STRING_LIT;
                    else
                        checkTerminateSymbol(character);
                    analyzed = true;
                }
                break;

                case IDENTIFIER: {
                    if (!((character >= 'a' && character <= 'z') || (character >= '0' && character <= '9') || (character >= 'A' && character <= 'Z') || character == '_')) {
                        if (Patterns.isPunctuation(character) || character == '.') {
                            tokens.add(new Token(Type.IDENTIFIER, buffer));
                            setStart();
                            analyzed = false;
                        } else {
                            tokens.add(new Token(Type.ERROR, buffer));
                            setStart();
                            analyzed = true;
                        }
                    } else
                        analyzed = true;
                }
                break;

                case DOUBLE_ASSIGN: {
                    if (character == '=') {
                        tokens.add(new Token(Type.OPERATOR, buffer + Character.toString(character)));
                        setStart();
                        analyzed = true;
                    }
                    else {
                        tokens.add(new Token(Type.OPERATOR, buffer));
                        setStart();
                        analyzed = false;
                    }
                }
                break;

                case COMMENT: {
                    if (character == '/') {
                        curState = State.ONE_LINE_COMMENT;
                        analyzed = true;
                    }
                    else if (character == '=') {
                        tokens.add(new Token(Type.OPERATOR, buffer + Character.toString(character)));
                        setStart();
                        analyzed = true;
                    }
                    else if (character == '*') {
                        curState = State.MULTI_LINE_COMMENT;
                        analyzed = true;
                    }
                    else {
                        tokens.add(new Token(Type.OPERATOR, buffer));
                        setStart();
                        analyzed = false;
                    }
                }
                break;

                case ONE_LINE_COMMENT: {
                    if (character == '\r' || character == '\n') {
                        tokens.add(new Token(Type.COMMENT, buffer));
                        setStart();
                        analyzed = false;
                    }
                    else
                        analyzed = true;
                }
                break;

                case MULTI_LINE_COMMENT: {
                    if (character == '*') {
                        curState = State.MULTI_LINE_COMMENT_STAR;
                    }
                    else if (fileEnded) {
                        tokens.add(new Token(Type.ERROR, buffer));
                    }
                    analyzed = true;
                }
                break;

                case MULTI_LINE_COMMENT_STAR: {
                    if (character == '/') {
                        tokens.add(new Token(Type.COMMENT, buffer + Character.toString(character)));
                        setStart();
                    } else {
                        curState = State.MULTI_LINE_COMMENT;
                    }
                    analyzed = true;
                }
                break;

                case OPER_FIRST_SYM: {
                    if (character == '=' || Character.toString(character).equals(buffer)) {
                        tokens.add(new Token(Type.OPERATOR, buffer + Character.toString(character)));
                        setStart();
                        analyzed = true;
                    }
                    else {
                        tokens.add(new Token(Type.OPERATOR, buffer));
                        setStart();
                        analyzed = false;
                    }
                }
                break;

                case CHAR_LIT: {
                    if (character == '\\') {
                        curState = State.CHAR_T;
                        analyzed = true;
                    } else if (character == '\n' || character == '\'') {
                        tokens.add(new Token(Type.ERROR, buffer));
                        setStart();
                        analyzed = false;
                    } else {
                        curState = State.END_CHAR_LIT;
                        analyzed = true;
                    }

                }
                break;

                case CHAR_T: {
                    if (character == 'n' || character == 't' || character == '\\' || character == '\'' || character == '\"') {
                        curState = State.END_CHAR_LIT;
                    } else {
                        curState = State.ERR_COMM;
                    }
                    analyzed = true;
                }
                break;

                case END_CHAR_LIT: {
                    if (character == '\'') {
                        tokens.add(new Token(Type.LITERAL, buffer + Character.toString(character)));
                        setStart();
                    } else {
                        curState = State.ERR_COMM;
                    }
                    analyzed = true;
                }
                break;

                case ERR_COMM: {
                    if (character == '\n') {
                        tokens.add(new Token(Type.ERROR, buffer));
                        setStart();
                        analyzed = false;
                    }
                    else
                        analyzed = true;
                }
                break;

                case STRING_LIT: {
                    if (character == '\n') {
                        tokens.add(new Token(Type.ERROR, buffer));
                        setStart();
                        analyzed = false;
                    } else if (character == '\\') {
                        curState = State.STRING_LIT_SYM;
                        analyzed = true;
                    } else {
                        curState = State.STRING_LIT_BODY;
                        analyzed = true;
                    }

                }
                break;

                case STRING_LIT_SYM: {
                    if (character == 'n' || character == 't' || character == '\\' || character == '\'' || character == '\"') {
                        curState = State.STRING_LIT_BODY;
                        analyzed = true;
                    }
                    else {
                        curState = State.ERR_COMM;
                        analyzed = true;
                    }
                }
                break;

                case STRING_LIT_BODY: {
                    if (character == '\r' || character == '\n') {
                        tokens.add(new Token(Type.ERROR, buffer));
                        setStart();
                        analyzed = false;
                    } else if (character == '\\') {
                        curState = State.STRING_LIT_SYM;
                        analyzed = true;
                    } else if (character == '\"') {
                        tokens.add(new Token(Type.LITERAL, buffer + Character.toString(character)));
                        setStart();
                        analyzed = true;
                    } else
                        analyzed = true;
                }
                break;

                case MINUS: {
                    if (character == '=' || character == '-' || character == '>') {
                        tokens.add(new Token(Type.OPERATOR, buffer + Character.toString(character)));
                        setStart();
                        analyzed = true;
                    }
                    else if (character >= '1' && character <= '9') {
                        curState = State.NUMBER;
                        analyzed = true;
                    } else if (character == '0') {
                        curState = State.ZERO;
                        analyzed = true;
                    } else {
                        tokens.add(new Token(Type.OPERATOR, buffer));
                        setStart();
                        analyzed = false;
                    }
                }
                break;

                case ZERO: {
                    if (character == '.') {
                        curState = State.NUMBER_D;
                    }
                    else if (character == 'x' || character == 'X') {
                        curState = State.HEX;
                    }
                    else if (character >= '0' && character <= '9') {
                        curState = State.NUMBER;
                    }
                    else {
                        checkNumber(character);
                    }
                    analyzed = true;
                }
                break;

                case HEX: {
                    if ((character >= '0' && character <= '9') || (character >= 'A' && character <= 'F') || (character >= 'a' && character <= 'f'))
                        curState = State.HEX_N;
                    else {
                        curState = State.ERROR;
                    }
                    analyzed = true;
                }
                break;

                case HEX_N: {
                    if (!((character >= '0' && character <= '9') || (character >= 'A' && character <= 'F') || (character >= 'a' && character <= 'f'))) {
                        checkNumber(character);
                        analyzed = true;
                    }
                }
                break;

                case ERROR: {
                    if (Patterns.isPunctuation(character)) {
                        tokens.add(new Token(Type.ERROR, buffer));
                        setStart();
                        analyzed = false;
                    }
                    else
                        analyzed = true;
                }
                break;

                case NUMBER_D: {
                    if (character >= '0' && character <= '9')
                        curState = State.NUMBER_DOT;
                    else {
                        curState = State.DOT_ERR;
                    }
                    analyzed = true;
                }
                break;

                case NUMBER_DOT: {
                    if (!(character >= '0' && character <= '9')) {
                        checkNumber(character);
                        analyzed = false;
                    }
                    else
                        analyzed = true;
                }
                break;

                case DOT_ERR: {
                    if (character == ' ' || character == '\n') {
                        tokens.add(new Token(Type.ERROR, buffer));
                        setStart();
                        analyzed = false;
                    } else
                        analyzed = true;
                }
                break;

                case NUMBER: {
                    if (character == '.') {
                        curState = State.NUMBER_D;
                        analyzed = true;
                    }
                    else if (!(character >= '0' && character <= '9')) {
                        checkNumber(character);
                        analyzed = false;
                    } else
                        analyzed = true;
                }
                break;

                case DOT: {
                    if (character >= '0' && character <= '9') {
                        curState = State.NUMBER_DOT;
                        analyzed = true;
                    }
                    else {
                        tokens.add(new Token(Type.OPERATOR, buffer));
                        setStart();
                        analyzed = false;
                    }
                }
                break;

                default: {
                    System.out.println("Oops");
                }
                break;
            }
        }

        if(curState != State.START) {
            buffer += character;
        }
    }

    public void setStart(){
        buffer = "";
        curState = State.START;
    }

    public void checkNumber(Character character){
        if(Patterns.isPunctuation(character)) {
            tokens.add(new Token(Type.NUMBER, buffer));
            setStart();
        }
        else {
            curState = State.ERROR;
            buffer+=character;
        }

    }

    public void checkTerminateSymbol(char character){
        if(Patterns.isPunctuation(character))
            tokens.add(new Token(Type.PUNCTUATION, Character.toString(character)));
        else
            tokens.add(new Token(Type.ERROR, Character.toString(character)));
        setStart();
    }

    public void printTokens(){
        for (Token t: this.tokens)
            if (!(t.getTokenType() == Type.PUNCTUATION && t.getContent().equals(" ")))
                System.out.println(t.getTokenType().toString() + ": " + t.getContent());
    }

    public void printTokensByType() {
        ArrayList<ArrayList<String>> tokensByType = new ArrayList<>();
        int typesCount = Type.values().length;
        for (int i = 0; i < typesCount; i++) {
            tokensByType.add(new ArrayList<>());
        }

        for (Token token : tokens)
            tokensByType.get(token.getTokenType().ordinal()).add(token.getContent());

        for (int i = 0; i < tokensByType.size(); i++) {
            System.out.println(Type.values()[i]);
            for (String token : tokensByType.get(i))
                System.out.println(token);
            System.out.println("---------------------");
        }
    }

    public void generateHTML() {
        String html = "<html><head><title>Lexer</title><link href =\"style.css\" rel=\"stylesheet\" type=\"text/css\"></head><body><h1>Lexer </h1><div>";
        for (Token token : tokens) {
            if (token.getTokenType() == Type.PUNCTUATION && token.getContent().equals("\\n"))
                html += "</div>\n<div>";
            else
                html += String.format("<p class=\"%s\"><b>%s</b></p>", token.getTokenType(), token.getContent());
        }
        html += "</div></body></html>";

        FileWriter fileWriter;
        BufferedWriter bufferedWriter;
        try {
            fileWriter = new FileWriter("output/output.html");
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(html);
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
