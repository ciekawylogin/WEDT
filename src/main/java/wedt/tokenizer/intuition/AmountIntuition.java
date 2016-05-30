package wedt.tokenizer.intuition;

import wedt.utils.model.TokenType;

public class AmountIntuition extends Intuition {
    public boolean apply(String token) {
        return token.matches("\\d+\\.\\d+");
    }

    public AmountIntuition() {
        super(TokenType.DECIMAL);
    }
}