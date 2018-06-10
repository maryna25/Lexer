import java.io.*;
import java.util.ArrayList;

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

        switch (curState){
            case START:{
                if(character == '!' || character == '%' || character == '=' || character == '*' || character == '>' || character == '<')
                    curState = State.DOUBLE_ASSIGN;
                else if(character >= '1' && character <= '9')
                    curState = State.NUMBER;
                else if(character == '.')
                    curState = State.DOT;
                else if((character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || character == '_')
                    curState = State.IDENTIFIER;
                else if(character == '0')
                    curState = State.ZERO;
                else if(character == '?' || character == ':')
                    curState = State.OPERATOR;
                else if(character == '/')
                    curState = State.COMMENT;
                else if(character == '+' || character == '&' || character == '|')
                    curState = State.OPER_FIRST_SYM;
                else if(character == '-')
                    curState = State.MINUS;
                else if(character == '\'')
                    curState = State.CHAR_LIT;
                else if(character == '\"')
                    curState = State.STRING_LIT;
                else
                    checkTerminateSymbol(character);
            }
            break;

            case IDENTIFIER:{
                if(!((character >= 'a' && character <= 'z') || (character >= '0' && character <= '9') || (character >= 'A' && character <= 'Z') || character == '_')) {
                    if(Patterns.isPunctuation(character) || character == '.') {
                        tokens.add(new Token(Type.IDENTIFIER, buffer));
                        setStart();
                        analyzeChar(character);
                        return;
                    }
                    else {
                        tokens.add(new Token(Type.ERROR, buffer));
                        setStart();
                    }
                }
            }
            break;

            case DOUBLE_ASSIGN:{
                if(character == '=')
                    curState = State.OPERATOR;
                else {
                    tokens.add(new Token(Type.OPERATOR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
            }
            break;

            case OPERATOR:{
                tokens.add(new Token(Type.OPERATOR, buffer.isEmpty()?Character.toString(character):buffer));
                setStart();
                analyzeChar(character);
                return;
            }

            case COMMENT:{
                if(character == '/')
                    curState = State.ONE_LINE_COMMENT;
                else if(character == '=')
                    curState = State.OPERATOR;
                else if(character == '*')
                    curState = State.MULTI_LINE_COMMENT;
                else {
                    tokens.add(new Token(Type.OPERATOR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
            }
            break;

            case ONE_LINE_COMMENT:{
                if(character == '\r' || character == '\n') {
                    tokens.add(new Token(Type.COMMENT, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
            }
            break;

            case MULTI_LINE_COMMENT:{
                if(character == '*')
                    curState = State.MULTI_LINE_COMMENT_STAR;
                else if(fileEnded){
                    tokens.add(new Token(Type.ERROR, buffer));
                    return;
                }
            }
            break;

            case END_COMMENT:{
                tokens.add(new Token(Type.COMMENT, buffer));
                setStart();
                analyzeChar(character);
                return;
            }

            case MULTI_LINE_COMMENT_STAR:{
                if(character == '/'){
                    curState = State.END_COMMENT;
                }
                else{
                    curState = State.MULTI_LINE_COMMENT;
                }
            }
            break;

            case OPER_FIRST_SYM:{
                if(character == '=' || Character.toString(character).equals(buffer))
                    curState = State.OPERATOR;
                else{
                    tokens.add(new Token(Type.OPERATOR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
            }
            break;

            case CHAR_LIT:{
                if(character == '\\') {
                    curState = State.CHAR_T;
                }
                else if (character == '\n'||character == '\''){
                    tokens.add(new Token(Type.ERROR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
                else {
                    curState = State.END_CHAR_LIT;
                }

            }
            break;

            case CHAR_T:{
                if(character == 'n' || character == 't' || character == '\\'|| character == '\''||character == '\"') {
                    curState = State.END_CHAR_LIT;
                }
                else{
                    curState = State.ERR_COMM;
                }
            }
            break;

            case END_CHAR_LIT:{
                if(character == '\'') {
                    curState = State.LITERAL;
                }
                else {
                    curState = State.ERR_COMM;
                }
            }
            break;

            case ERR_COMM:{
                if (character == '\n') {
                    tokens.add(new Token(Type.ERROR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
            }

            case STRING_LIT:{
                if (character == '\n') {
                    tokens.add(new Token(Type.ERROR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
                else if(character == '\\'){
                    curState = State.STRING_LIT_SYM;
                }
                else{
                    curState = State.STRING_LIT_BODY;
                }

            }
            break;

            case STRING_LIT_SYM:{
                if (character == 'n' || character == 't' || character == '\\'|| character == '\''||character == '\"')
                    curState = State.STRING_LIT_BODY;
                else{
                    curState = State.ERR_COMM;
                }
            }
            break;

            case STRING_LIT_BODY:{
                if(character == '\r'||character == '\n') {
                    tokens.add(new Token(Type.ERROR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
                else if(character == '\\'){
                    curState = State.STRING_LIT_SYM;
                }
                else if(character == '\"'){
                    curState = State.LITERAL;
                }
            }
            break;

            case LITERAL:{
                tokens.add(new Token(Type.LITERAL, buffer));
                setStart();
                analyzeChar(character);
                return;
            }

            case MINUS:{
                if(character == '=' || character == '-' || character == '>')
                    curState = State.OPERATOR;
                else if (character >= '1' && character <= '9'){
                    curState = State.NUMBER;
                }
                else if (character == '0'){
                    curState = State.ZERO;
                }
                else{
                    tokens.add(new Token(Type.OPERATOR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
            }
            break;

            case ZERO:{
                if(character == '.')
                    curState = State.NUMBER_D;
                else if(character == 'x' || character == 'X')
                    curState = State.HEX;
                else if(character >= '0' && character <= '9')
                    curState = State.NUMBER;
                else {
                    checkNumber(character);
                    return;
                }
            }
            break;

            case HEX:{
                if((character >= '0' && character <= '9')||(character >= 'A' && character <= 'F')||(character >= 'a' && character <= 'f'))
                    curState = State.HEX_N;
                else{
                    curState = State.ERROR;
                    return;
                }
            }
            break;

            case HEX_N:{
                if(!((character >= '0' && character <= '9')||(character >= 'A' && character <= 'F')||(character >= 'a' && character <= 'f'))){
                    checkNumber(character);
                    return;
                }
            }
            break;

            case ERROR:{
                if(Patterns.isPunctuation(character)) {
                    tokens.add(new Token(Type.ERROR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
            }
            break;

            case NUMBER_D:{
                if(character >= '0' && character <= '9')
                    curState = State.NUMBER_DOT;
                else {
                    curState = State.DOT_ERR;
                }
            }
            break;

            case NUMBER_DOT:{
                if(!(character >= '0' && character <= '9')) {
                    checkNumber(character);
                    return;
                }
            }
            break;

            case DOT_ERR:{
                if(character == ' ' || character == '\n'){
                    tokens.add(new Token(Type.ERROR, buffer));
                    setStart();
                    analyzeChar(character);
                }
            }
            break;

            case NUMBER:{
                if(character == '.')
                    curState = State.NUMBER_D;
                else if(!(character >= '0' && character <= '9')){
                    checkNumber(character);
                    return;
                }
            }
            break;

            case DOT:{
                if(character >= '0' && character <= '9')
                    curState = State.NUMBER_DOT;
                else {
                    tokens.add(new Token(Type.OPERATOR, buffer));
                    setStart();
                    analyzeChar(character);
                    return;
                }
            }
            break;

            default:
            {
                System.out.println("Oops");
            }
            break;
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
            analyzeChar(character);
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
