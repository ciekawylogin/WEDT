package wedt.utils.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
public class FileContent {
    private final String fileName;
    private final List<TokenLine> tokenLines;
    private final List<AddressBlock> addressBlocks;

    public FileContent(String fileName, List tokenLines) {
        this.fileName = fileName;
        this.tokenLines = tokenLines;
        addressBlocks = buildAddressBlocksList();
    }

    private List<AddressBlock> buildAddressBlocksList() {
        List<AddressBlock> result = new ArrayList<>();
        AddressBlock addressBlock = null;
        boolean insideAddressBlock = false;

        try {
            for (TokenLine tokenLine : tokenLines) {
                if (tokenLine.getTokenType() == TokenType.BEGIN_ADDRESS) {
                    if (insideAddressBlock == true) {
                        throw new RuntimeException("Invalid address tokens.");
                    } else {
                        addressBlock = new AddressBlock();
                        insideAddressBlock = true;
                    }
                } else if (tokenLine.getTokenType() == TokenType.END_ADDRESS) {
                    if (insideAddressBlock == false) {
                        throw new RuntimeException("Invalid address tokens.");
                    } else {
                        result.add(addressBlock);
                        insideAddressBlock = false;
                    }
                } else if (insideAddressBlock == true) {
                    addressBlock.addTokenLine(tokenLine);
                }
            }
        } catch (RuntimeException ex) {
            log.warn("Error while searching for address block inside file " + fileName);
            result.clear();
        }

        log.info("Found " + result.size() + " address blocks inside " + fileName);
        return result;
    }

    public void addTokenLine(TokenLine tokenLine) {
        this.tokenLines.add(tokenLine);
    }
}
