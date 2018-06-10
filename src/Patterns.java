import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Patterns {

    public static final String[] VALUES_KEY =  new String[]{"abstract", "assert", "boolean", "break", "byte", "switch",
            "case", "try", "catch", "finally", "char", "class", "continue", "default", "do", "double", "if", "else",
            "enum", "extends", "final", "float", "for", "implements", "import", "instanceOf", "int", "interface", "long",
            "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp",
            "super", "synchronized", "this", "throw", "throws", "transient", "void", "volatile", "while", "goto", "const" };

    public static final Character[] VALUES_PUN = new Character[] {'-','+','=','{','}','[',']',';',':','"','<','>','?',',','/',' ', '\n', '\t', '!','%','&','*','(',')'};

    public static final String[] VALUES_LIT = new String[]{"true", "false", "null"};


    private static Set<String> keywords = new HashSet<String>(Arrays.asList(VALUES_KEY));
    private static Set<Character> punctuation = new HashSet<Character>(Arrays.asList(VALUES_PUN));
    private static Set<String> literals = new HashSet<String>(Arrays.asList(VALUES_LIT));

    public static boolean isKeyword(String word)
    {
        return keywords.contains(word);
    }

    public static boolean isPunctuation(char word)
    {
        return punctuation.contains(word);
    }
    public static boolean isLiteral(String word)
    {
        return literals.contains(word);
    }
}
