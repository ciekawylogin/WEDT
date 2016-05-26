package wedt.randomforest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wedt.utils.FileUtils;
import wedt.utils.model.AddressBlock;
import wedt.utils.model.FileContent;
import wedt.utils.model.TokenLine;
import wedt.utils.model.TokenType;

import java.io.Serializable;
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
     * Jaka część wszystkich tokenów używana będzie w pojedynczej generacji drzewa.
     */
    private final double tokensForSingleTree = 0.8;
    /**
     * O ile tokenów będzie zadawane pytanie w węźle.
     * Jest to procentowa liczba pól (głębokość) o jakie teraz pytamy.
     */
    private final double numberOfTokensInQuestion = 0.7;
    /**
     * Ile pojedynczych drzew decyzyjnych należy wygenerować.
     * Jest to procentowa liczba wszstkich załadowanych adresów.
     */
    private final double numberOfTrees = 0.7;
    private final Random random = new Random(0);

    public static void main(String[] args) {
        RandomForest randomForest = new RandomForest(new FileUtils());

        randomForest.learnFromDirectory("./test_data", "decisionTrees_serial");
        List<TreeNode> decisionTrees = randomForest.learnFromSerializeFile("decisionTrees_serial");
    }

    public List<TreeNode> learnFromSerializeFile(String fileToBeSaved) {
        List<TreeNode> decisionTrees = (List<TreeNode>) fileUtils.readSerializationFromFile(fileToBeSaved);
        log.info("Number of decision trees read from file=" + decisionTrees.size());
        return decisionTrees;
    }

    public List<TreeNode> learnFromDirectory(String directoryName, String fileToBeSaved) {
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
        log.info("Total number of addressBlocks=" + addressBlocks.size());

        List<TreeNode> decisionTrees = new ArrayList<>();
        int numberOfTreesToGenerate = (int) (addressBlocks.size() * numberOfTrees);
        for (int i = 0; i < numberOfTreesToGenerate; i++) {
            TreeNode treeNode = buildSingleDecisionTree(addressBlocks, new ArrayList<>(tokenTypeSet));
            decisionTrees.add(treeNode);
        }
        log.info("Generated number of decisionTrees=" + numberOfTreesToGenerate);
        if(fileToBeSaved != null) {
            fileUtils.saveSerializationToFile((Serializable) decisionTrees, fileToBeSaved);
        }

        return decisionTrees;
    }

    private TreeNode buildSingleDecisionTree(List<AddressBlock> addressBlocks, List<TokenType> tokenTypes) {
        List<AddressBlock> addressBlocksUsed = drawAddressBlocks(addressBlocks);
        List<TokenType> tokenTypesUsed = drawTokenTypes(tokenTypes);

        TreeNode treeRoot = buildSingleTreeNode(addressBlocksUsed, tokenTypesUsed, 0);
        //log.info("Tree was built: \n" + treeRoot.printValue(""));
        return treeRoot;
    }

    private List<AddressBlock> drawAddressBlocks(List<AddressBlock> addressBlocks) {
        List<AddressBlock> result = new ArrayList<>();
        int size = addressBlocks.size();

        for (AddressBlock addressBlock : addressBlocks) {
            result.add(addressBlocks.get(random.nextInt(size)));
        }

        return result;
    }

    private List<TokenType> drawTokenTypes(List<TokenType> tokenTypes) {
        List<TokenType> result = new ArrayList<>();

        for (TokenType tokenType : tokenTypes) {
            if (random.nextDouble() < tokensForSingleTree) {
                result.add(tokenType);
            }
        }

        return result;
    }

    private TreeNode buildSingleTreeNode(List<AddressBlock> addressBlocks,
                                     List<TokenType> tokenTypes, int depthIndex) {
        int maxLength = 0;

        for (AddressBlock addressBlock : addressBlocks) {
            maxLength = Math.max(maxLength, addressBlock.getTokenLines().size());
        }

        if(maxLength - depthIndex <= 0) {
            //liść
            return new TreeNode(addressBlocks);
        } else {
            //o jaką głębość bedzie zadawane pytanie
            int tokenLinesDepthQuestion = random.nextInt(maxLength - depthIndex);
            //o jakie tokeny bedzie zadawane pytanie (alternatywa tokenów)
            Set<TokenType> tokenTypeQuestionSet = new HashSet<>();
            int tokensAmountInQuestion = 1 + (int)( tokenLinesDepthQuestion * numberOfTokensInQuestion);
            for (int i = 0; i < tokensAmountInQuestion; i++) {
                tokenTypeQuestionSet.add(tokenTypes.get(random.nextInt(tokenTypes.size())));
            }

            //teraz stawiane jest pytanie czy jakikolwiek token z listy tokenTypeQuestionSet występuje w
            //kolejnych tylu tokenLinesDepthQuestion liniach
            //zaczynając od pozycji depthIndex na liście tokenów
            List<AddressBlock> containsList = new ArrayList<>();
            List<AddressBlock> doesntContainList = new ArrayList<>();
            boolean containsTmp;

            for (AddressBlock addressBlock : addressBlocks) {
                containsTmp = false;
                for(int i = depthIndex; i <= tokenLinesDepthQuestion && i < addressBlock.getTokenLines().size(); i++) {
                    TokenLine tokenLine = addressBlock.getTokenLines().get(i);
                    if(tokenTypeQuestionSet.contains(tokenLine.getTokenType())) {
                        containsList.add(addressBlock);
                        containsTmp = true;
                        break;
                    }
                }
                if(!containsTmp) {
                    doesntContainList.add(addressBlock);
                }
            }

            TreeNode leftTreeNode = buildSingleTreeNode(containsList, tokenTypes, depthIndex+1);
            TreeNode rightTreeNode = buildSingleTreeNode(doesntContainList, tokenTypes, depthIndex+1);
            return new TreeNode(addressBlocks,
                    leftTreeNode,
                    rightTreeNode,
                    tokenTypeQuestionSet,
                    tokenLinesDepthQuestion);
        }
    }
}
