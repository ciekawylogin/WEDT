package wedt.tokenizer.intuition;

import wedt.utils.model.TokenType;

public class NumberSequenceIntuition extends Intuition {
    public boolean apply(String token) {
        return token.matches("(\\d+/)+\\d+");
    }

    public NumberSequenceIntuition() {
        super(TokenType.NUMBER_SEQ);
    }
}
