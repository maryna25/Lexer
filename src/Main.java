public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer("./input/test1.txt");
        lexer.printTokens();
//        lexer.printTokensByType();
        lexer.generateHTML();
    }
}