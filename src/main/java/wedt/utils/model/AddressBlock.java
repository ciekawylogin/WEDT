package wedt.utils.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddressBlock implements Serializable {
    private List<TokenLine> tokenLines = new LinkedList<>();
    private int startLine = -1;

    public void addTokenLine(TokenLine tokenLine) {
        tokenLines.add(tokenLine);
    }

    public String printValue() {
        String result = "Start Line: " + startLine + "\n";
        for (TokenLine tokenLine: tokenLines) {
            result += tokenLine.getValue() + " ";
        }
        return result+"\n";
    }
}
