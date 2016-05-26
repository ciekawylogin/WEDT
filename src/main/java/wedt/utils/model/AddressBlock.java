package wedt.utils.model;

import lombok.Getter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class AddressBlock implements Serializable {
    @Getter
    private final List<TokenLine> tokenLines = new LinkedList<>();

    public void addTokenLine(TokenLine tokenLine) {
        tokenLines.add(tokenLine);
    }

    public String printValue() {
        return tokenLines.toString();
    }
}
