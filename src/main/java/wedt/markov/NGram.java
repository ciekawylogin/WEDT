package wedt.markov;

import lombok.Getter;
import wedt.utils.model.TokenType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Getter
public class NGram {
    private TokenType[] tokens;

    public NGram(TokenType[] tokens) {
        this.tokens = tokens;
    }

    public NGram(List<TokenType> tokens) {
        this.tokens = tokens.toArray(new TokenType[tokens.size()]);
    }

    public List<TokenType> getTokenList() {
        return new LinkedList<>(Arrays.asList(tokens));
    }

    public boolean equals(Object other) {
        if(other != null && other instanceof NGram) {
            return Arrays.equals(tokens, ((NGram)other).tokens);
        }
        return false;
    }

    public int hashCode() {
        return  Arrays.hashCode(tokens);
    }

}
