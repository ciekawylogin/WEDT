package wedt.tokenizer.intuition;

import wedt.utils.model.TokenType;

public class NumberIntuition extends Intuition {
    public boolean apply(String token) {
        return token.matches("\\d+");
    }

    public NumberIntuition() {
        super(TokenType.NUMBER);
    }
}
