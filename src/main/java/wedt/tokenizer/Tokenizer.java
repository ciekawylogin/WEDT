package wedt.tokenizer;
import wedt.tokenizer.dictionaries.Dictionary;
import wedt.tokenizer.intuition.*;
import wedt.utils.model.TokenType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Tokenizer {

    private Tokenizer() {
        dictionaries.add(new Dictionary(TokenType.COUNTRY_CODE, "country_codes", true));
        dictionaries.add(new Dictionary(TokenType.MUNICIPALITY, "municipalities", true));
        dictionaries.add(new Dictionary(TokenType.CITY, "cities", true));
        dictionaries.add(new Dictionary(TokenType.STREET_NAME, "streets", true));
        dictionaries.add(new Dictionary(TokenType.STREET_INDICATOR, "street_indicators", false));
        dictionaries.add(new Dictionary(TokenType.COMPANY_TYPE, "company_types", true));
        dictionaries.add(new Dictionary(TokenType.MUNICIPALITY_INDICATOR, "municipality_indicators", false));
        dictionaries.add(new Dictionary(TokenType.PHONE_INDICATOR, "phone_indicators", false));
        dictionaries.add(new Dictionary(TokenType.FIRST_NAME, "first_names", true));
        dictionaries.add(new Dictionary(TokenType.LAST_NAME, "last_names", true));

        intuitions.add(new AccountNumberIntuition());
        intuitions.add(new PostalCodeIntuition());
        intuitions.add(new DateIntuition());
        intuitions.add(new NumberIntuition());
        intuitions.add(new NumberSequenceIntuition());
        intuitions.add(new AmountIntuition());
    }

    public static void main(String[] args) {
        try {
            Tokenizer tokenizer = new Tokenizer();
            tokenizer.recognizeTokens();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private List<Dictionary> dictionaries = new LinkedList<>();
    private List<Intuition> intuitions = new LinkedList<>();

    private void recognizeTokens() throws FileNotFoundException {
        String inputFileName = "agora-alt.ocr.txt";
        File inputFile = new File("test_data/" + inputFileName);
        Scanner sc = new Scanner(inputFile);
        while(sc.hasNext()){
            String line = sc.nextLine();
            line = line.replaceAll("(\\d+)[.,](\\d+)", "$1DOTDOTDOT$2");
            line = line.replaceAll("Sp\\.", "SpDOTDOTDOT");
            line = line.replaceAll("S\\.A", "SDOTDOTDOTA");

            // TODO ugliest code ever \/
            line = line.replaceAll("(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)", "$1$2$3$4$5$6$7");
            line = line.replaceAll("(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)", "$1$2$3$4$5$6");
            line = line.replaceAll("(\\d+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)", "$1$2$3$4$5");
            line = line.replaceAll("(\\d+)-(\\d+)-(\\d+)-(\\d+)", "$1$2$3$4");
            line = line.replaceAll("(\\d+)-(\\d+)-(\\d+)", "$1$2$3");

            line = line.replaceAll("(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)", "$1$2$3$4$5$6$7");
            line = line.replaceAll("(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)", "$1$2$3$4$5$6");
            line = line.replaceAll("(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)", "$1$2$3$4$5");
            line = line.replaceAll("(\\d+)\\s(\\d+)\\s(\\d+)\\s(\\d+)", "$1$2$3$4");
            line = line.replaceAll("(\\d+)\\s(\\d+)\\s(\\d+)", "$1$2$3");
            
            line = line.replaceAll("pl\\.", "plac");
            String [] tokens = line.split("[; \t\\(\\).,]");
            for(int i=0; i < tokens.length; ++i) {
                tokens[i] = tokens[i].replaceAll("DOTDOTDOT", ".");
                tokens[i] = tokens[i].trim().replaceAll("^[-:_=+'\".,<>*!/]+", "").replaceAll("[-:_=+'\".,<>*!/]+$", "");
                if(tokens[i].equals("")) {
                    continue;
                }
                TokenType token = recognizeToken(tokens[i]);
                System.out.println(token.name() + " " + tokens[i]);
            }
        }
    }

    private TokenType recognizeToken(String token) {
        for(Intuition intuition: intuitions) {
            if(intuition.apply(token)) {
                return intuition.getTokenType();
            }
        }
        for(Dictionary dictionary: dictionaries) {
            if((!dictionary.requiresCapital() || beginsWithCapital(token)) && dictionary.contains(token)) {
                return dictionary.getTokenType();
            }
        }
        return TokenType.UNKNOWN;
    }

    private boolean beginsWithCapital(String token) {
        String firstChar = token.substring(0, 1);
        return firstChar.toUpperCase().equals(firstChar);
    }

}
