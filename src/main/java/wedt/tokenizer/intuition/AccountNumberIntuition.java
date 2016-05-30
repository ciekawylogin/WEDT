package wedt.tokenizer.intuition;

import wedt.utils.model.TokenType;

public class AccountNumberIntuition extends Intuition {
    public boolean apply(String token) {
        return token.matches("(PL)?\\d{26}");
    }

    public AccountNumberIntuition() {
        super(TokenType.ACCOUNT_NO);
    }
}