package wedt.tokenizer.intuition;

import wedt.utils.model.TokenType;

public class DateIntuition extends Intuition {
    public boolean apply(String token) {
        return token.matches("\\d{2}[-/.]\\d{2}[-/.]\\d{2}\\d{2}?");
    }

    public DateIntuition() {
        super(TokenType.DATE);
    }
}