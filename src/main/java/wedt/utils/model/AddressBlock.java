package wedt.utils.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class AddressBlock implements Serializable {
    @Getter
    private List<TokenLine> tokenLines = new LinkedList<>();

    public void addTokenLine(TokenLine tokenLine) {
        tokenLines.add(tokenLine);
    }

    public String printValue() {
        String result = "";
        for (TokenLine tokenLine: tokenLines) {
            result += tokenLine.getValue() + " ";
        }
        return result+"\n";
    }
}
