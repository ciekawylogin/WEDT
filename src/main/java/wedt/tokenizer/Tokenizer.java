package wedt.tokenizer;
import wedt.tokenizer.dictionaries.Dictionary;
import wedt.tokenizer.intuition.*;
import wedt.utils.model.TokenType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Tokenizer {

    static String [] files = {
        "agora-alt.ocr.txt",
        "agora.ocr.txt",
        "akfa.ocr.txt",
        "alfa.ocr.txt",
        "anmit.ocr.txt",
        "arisco.ocr.txt",
        "atut.ocr.txt",
        "augusto.ocr.txt",
        "azet.ocr.txt",
        "bartnik.ocr.txt",
        "batorski.ocr.txt",
        "celowy-zwiazek-gmin.ocr.txt",
        "coi.ocr.txt",
        "dajar.ocr.txt",
        "dar-natury.ocr.txt",
        "dom-bud-ii.ocr.txt",
        "drspil.ocr.txt",
        "druczki.ocr.txt",
        "ecologika.ocr.txt",
        "eko-inwest.ocr.txt",
        "ekol.ocr.txt",
        "eko-region.ocr.txt",
        "eksploatator-1.ocr.txt",
        "eksploatator-2.ocr.txt",
        "energa.ocr.txt",
        "energa-osw.ocr.txt",
        "energopol.ocr.txt",
        "erif.ocr.txt",
        "euro-compass.ocr.txt",
        "expert.ocr.txt",
        "famex.ocr.txt",
        "flash.ocr.txt",
        "gcp.ocr.txt",
        "grabowiecki.ocr.txt",
        "infoserwis.ocr.txt",
        "infosystem.ocr.txt",
        "intersecurit.ocr.txt",
        "isa.ocr.txt",
        "iso-tech.ocr.txt",
        "jarosz.ocr.txt",
        "kantor-piatek.ocr.txt",
        "konkret-1.ocr.txt",
        "konkret-2.ocr.txt",
        "konkret-3.ocr.txt",
        "kontakt.ocr.txt",
        "kopel.ocr.txt",
        "krezymon.ocr.txt",
        "ksiazek.ocr.txt",
        "lars-laj.ocr.txt",
        "maszyny-drogowe.ocr.txt",
        "max-usluga.ocr.txt",
        "meblolux.ocr.txt",
        "moje-dzialdowo.ocr.txt",
        "moto46.ocr.txt",
        "mroczkowska.ocr.txt",
        "mystek.ocr.txt",
        "nadlesnictwo-gryfice.ocr.txt",
        "netia.ocr.txt",
        "notor.ocr.txt",
        "oddk.ocr.txt",
        "ognik.ocr.txt",
        "orange-color-alt.ocr.txt",
        "orange-color.ocr.txt",
        "orange.ocr.txt",
        "oskp.ocr.txt",
        "pge-black.ocr.txt",
        "pge-multi-1.ocr.txt",
        "pge-multi-2.ocr.txt",
        "pge-multi-3.ocr.txt",
        "pge-multi-4.ocr.txt",
        "pge-multi-5.ocr.txt",
        "pge-multi-6.ocr.txt",
        "pge-multi-7.ocr.txt",
        "pge.ocr.txt",
        "pgk-bilgoraj.ocr.txt",
        "pgnig.ocr.txt",
        "pik.ocr.txt",
        "pkp.ocr.txt",
        "pks-kamien-alt.ocr.txt",
        "pks-kamien.ocr.txt",
        "plus.ocr.txt",
        "poczta.ocr.txt",
        "polska-press-alt.ocr.txt",
        "polska-press.ocr.txt",
        "presscom.ocr.txt",
        "pro-tech.ocr.txt",
        "pwpw.ocr.txt",
        "rsoft.ocr.txt",
        "rzsw.ocr.txt",
        "sanepid.ocr.txt",
        "sigma.ocr.txt",
        "signum.ocr.txt",
        "skatom.ocr.txt",
        "solarium-ibiza.ocr.txt",
        "spaceone.ocr.txt",
        "taxpress.ocr.txt",
        "teczowa.ocr.txt",
        "tmobile.ocr.txt",
        "wodociagi-dziwnow-alt.ocr.txt",
        "wodociagi-dziwnow.ocr.txt",
        "wodpol.ocr.txt",
        "wydawnictwa-akcydensowe.ocr.txt",
        "zaklad-poligraficzny.ocr.txt",
        "zakl-tech-obl.ocr.txt",
        "zambrow.ocr.txt",
        "zejmo.ocr.txt",
        "zeto.ocr.txt",
        "zgk-zwierzyniec.ocr.txt"
    };

    private Tokenizer() {
        dictionaries.add(new Dictionary(TokenType.COUNTRY_CODE, "country_codes", true));
        dictionaries.add(new Dictionary(TokenType.MUNICIPALITY, "municipalities", true));
        dictionaries.add(new Dictionary(TokenType.FIRST_NAME, "first_names", true));
        dictionaries.add(new Dictionary(TokenType.CITY, "cities", true));
        dictionaries.add(new Dictionary(TokenType.STREET_NAME, "streets", true));
        dictionaries.add(new Dictionary(TokenType.STREET_INDICATOR, "street_indicators", false));
        dictionaries.add(new Dictionary(TokenType.COMPANY_TYPE, "company_types", true));
        dictionaries.add(new Dictionary(TokenType.MUNICIPALITY_INDICATOR, "municipality_indicators", false));
        dictionaries.add(new Dictionary(TokenType.PHONE_INDICATOR, "phone_indicators", false));
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
            for(String filename: files) {
                tokenizer.recognizeTokens(filename);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Dictionary> dictionaries = new LinkedList<>();
    private List<Intuition> intuitions = new LinkedList<>();

    private void recognizeTokens(String filename) throws IOException {
        File inputFile = new File("test_data/" + filename);
        FileOutputStream outputFile = new FileOutputStream(new File("result/" + filename));
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
                outputFile.write((token.name() + " " + tokens[i] + "\n").getBytes());
                //System.out.println(token.name() + " " + tokens[i]);
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
