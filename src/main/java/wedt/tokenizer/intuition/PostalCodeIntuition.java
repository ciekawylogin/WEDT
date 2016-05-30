package wedt.tokenizer.intuition;

import wedt.utils.model.TokenType;

public class PostalCodeIntuition extends Intuition {
    public boolean apply(String token) {
        return token.matches("\\d\\d-\\d\\d\\d");
    }

    public PostalCodeIntuition() {
        super(TokenType.POSTAL_CODE);
    }
}
