package wedt.tokenizer.dictionaries;

import wedt.utils.model.TokenType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class Dictionary {
    private TokenType tokenType;
    private String name;
    private boolean requiresCapital;

    private Set<String> words;

    String fixCommonTypos(String word) {
        return word.
                replaceAll("ó", "o").
                replaceAll("0", "o");
    }

    public Dictionary(TokenType tokenType, String name, boolean requiresCapital) {
        this.tokenType = tokenType;
        this.name = name;
        this.requiresCapital = requiresCapital;
        this.words = new HashSet<String>();
        try {
            File file = new File("dictionaries/" + name + ".txt");
            Scanner sc = new Scanner(file);
            while(sc.hasNext()){
                String line = sc.nextLine();
                this.words.add(fixCommonTypos(line.trim().toLowerCase()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean contains(String word) {
        return words.contains(fixCommonTypos(
                word.trim().toLowerCase()
        ));
    }

    public String getName() {
        return name;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public boolean requiresCapital() {
        return requiresCapital;
    }
}
