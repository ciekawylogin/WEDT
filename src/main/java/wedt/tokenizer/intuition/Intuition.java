package wedt.tokenizer.intuition;

import wedt.utils.model.TokenType;

public abstract class Intuition {
    private TokenType tokenType;

    public Intuition(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public abstract boolean apply(String token);

    public TokenType getTokenType() {
        return tokenType;
    }
}
