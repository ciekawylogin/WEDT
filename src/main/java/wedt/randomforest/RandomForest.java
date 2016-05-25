package wedt.randomforest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wedt.utils.FileUtils;
import wedt.utils.model.AddressBlock;
import wedt.utils.model.FileContent;
import wedt.utils.model.TokenLine;
import wedt.utils.model.TokenType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class RandomForest {
    private final FileUtils fileUtils;
    /**
     * Jaka czeęść wszystkich tokenów używana będzie w pojedynczej generacji drzewa.
     */
    private final double tokensForSingleTree = 0.8;
    private final Random random = new Random();

    public static void main(String[] args) {
        RandomForest randomForest = new RandomForest(new FileUtils());

        randomForest.learnFromDirectory("./test_data");
    }

    public void learnFromDirectory(String directoryName) {
        List<FileContent> fileContents = fileUtils.loadDirectory(directoryName);
        List<AddressBlock> addressBlocks = new ArrayList<>();
        Set<TokenType> tokenTypeSet = new HashSet<>();

        for (FileContent fileContent : fileContents) {
            for (AddressBlock addressBlock : fileContent.getAddressBlocks()) {
                addressBlocks.add(addressBlock);
                for (TokenLine tokenLine : addressBlock.getTokenLines()) {
                    tokenTypeSet.add(tokenLine.getTokenType());
                }
            }
        }
        log.info("Number of different address' tokens=" + tokenTypeSet.size());
        log.info(tokenTypeSet.toString());

        buildSingleDecisionTree(addressBlocks, tokenTypeSet);
    }

    private void buildSingleDecisionTree(List<AddressBlock> addressBlocks, Set<TokenType> tokenTypeSet) {
        List<AddressBlock> addressBlocksUsed = drawAddressBlocks(addressBlocks);
        Set<TokenType> tokenTypeSetUsed = drawTokenTypeSet(tokenTypeSet);
    }

    private List<AddressBlock> drawAddressBlocks(List<AddressBlock> addressBlocks) {
        List<AddressBlock> result = new ArrayList<>();
        int size = addressBlocks.size();

        for (AddressBlock addressBlock : addressBlocks) {
            addressBlocks.get(random.nextInt(size));
        }

        return result;
    }

    private Set<TokenType> drawTokenTypeSet(Set<TokenType> tokenTypeSet) {
        Set<TokenType> result = new HashSet<>();

        for (TokenType tokenType : tokenTypeSet) {
            if (random.nextDouble() < tokensForSingleTree) {
                result.add(tokenType);
            }
        }

        return result;
    }
}
