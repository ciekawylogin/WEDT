package wedt.randomforest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wedt.utils.FileUtils;
import wedt.utils.model.AddressBlock;
import wedt.utils.model.FileContent;
import wedt.utils.model.TokenLine;
import wedt.utils.model.TokenType;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
        randomForest.getAddressesForFile("./test_data/agora.tokens.txt", decisionTrees);
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
        if (fileToBeSaved != null) {
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

        for (int i = 0; i < addressBlocks.size(); i++) {
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

    /**
     * @param addressBlocks lista wszystkchh bloków adresowych w danym węźle
     * @param tokenTypes    lista wszystkich dostepnych tokenów
     * @param depthIndex    głębokość szukania na adresach w danym weźle
     * @return
     */
    private TreeNode buildSingleTreeNode(List<AddressBlock> addressBlocks,
                                         List<TokenType> tokenTypes, int depthIndex) {
        int maxLength = 0;

        for (AddressBlock addressBlock : addressBlocks) {
            maxLength = Math.max(maxLength, addressBlock.getTokenLines().size());
        }

        if (maxLength - depthIndex <= 0) {
            //liść
            return new TreeNode(addressBlocks);
        } else {
            //o jaką głębość bedzie zadawane pytanie
            int tokenLinesDepthQuestion = random.nextInt(maxLength - depthIndex);
            //o jakie tokeny bedzie zadawane pytanie (alternatywa tokenów)
            Set<TokenType> tokenTypeQuestionSet = new HashSet<>();
            int tokensAmountInQuestion = 1 + (int) (tokenLinesDepthQuestion * numberOfTokensInQuestion);
            for (int i = 0; i < tokensAmountInQuestion; i++) {
                tokenTypeQuestionSet.add(tokenTypes.get(random.nextInt(tokenTypes.size())));
            }

            //teraz stawiane jest pytanie czy jakikolwiek token z listy tokenTypeQuestionSet występuje w
            //kolejnych tylu tokenLinesDepthQuestion liniach
            //zaczynając od pozycji depthIndex na liście tokenów
            List<AddressBlock> containsList = new ArrayList<>();
            List<AddressBlock> doesntContainList = new ArrayList<>();

            for (AddressBlock addressBlock : addressBlocks) {
                if (checkIfTokensContainsAnyToken(addressBlock.getTokenLines(), depthIndex,
                        tokenLinesDepthQuestion, tokenTypeQuestionSet)) {
                    containsList.add(addressBlock);
                } else {
                    doesntContainList.add(addressBlock);
                }
            }

            TreeNode leftTreeNode = buildSingleTreeNode(containsList, tokenTypes, depthIndex + 1);
            TreeNode rightTreeNode = buildSingleTreeNode(doesntContainList, tokenTypes, depthIndex + 1);
            return new TreeNode(addressBlocks,
                    leftTreeNode,
                    rightTreeNode,
                    tokenTypeQuestionSet,
                    tokenLinesDepthQuestion);
        }
    }

    /**
     * Sprawdza czy lista tokenów zawiera jakikolwiek token z podanej listy
     * na podanej głębokości.
     *
     * @return
     */
    private boolean checkIfTokensContainsAnyToken(List<TokenLine> tokenLines, int depthIndex,
                                                  int tokenLinesDepthQuestion, Set<TokenType> tokenTypeQuestionSet) {
        for (int i = depthIndex; i <= tokenLinesDepthQuestion && i < tokenLines.size(); i++) {
            TokenLine tokenLine = tokenLines.get(i);
            if (tokenTypeQuestionSet.contains(tokenLine.getTokenType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sprawdza czy adres zaczyna się w tym jednym konkretnym miejscu na liście linii tokenów.
     *
     * @return długość odnalezionego ciągu adresowego, lub 0 gdy nie znaleziono
     */
    public int checkIfAddressBegins(List<TokenLine> tokenLines, int depthIndex,
                                    TreeNode decisionTree) {
        if (decisionTree.isLeafTrue()) {
            return depthIndex;
        } else if (decisionTree.isLeafFalse()) {
            return 0;
        } else {
            if (checkIfTokensContainsAnyToken(tokenLines, depthIndex,
                    decisionTree.getDepth(), decisionTree.getTokenTypeSplitValue())) {
                return checkIfAddressBegins(tokenLines, depthIndex + 1, decisionTree.getLeftChild());
            } else {
                return checkIfAddressBegins(tokenLines, depthIndex + 1, decisionTree.getRightChild());
            }
        }
    }

    /**
     * Zwraca średnią długośc ciągu adresowego jako wynik, lub 0 gdy nei znaleziono.
     */
    public int checkIfAddressBegins(List<TokenLine> tokenLines, int depthIndex,
                                    List<TreeNode> decisionTrees) {
        int trueAnswer = 0;
        int falseAnswer = 0;
        int addressDepth = 0;

        for (TreeNode decisionTree : decisionTrees) {
            int addressFound = checkIfAddressBegins(tokenLines, depthIndex, decisionTree);
            if (checkIfAddressBegins(tokenLines, depthIndex, decisionTree) > 0) {
                trueAnswer++;
                addressDepth += addressFound;
            } else {
                falseAnswer++;
            }
        }
        return (trueAnswer > falseAnswer) ? (int) Math.floor(addressDepth / trueAnswer) : 0;
    }

    public List<AddressBlock> getAddressesFromTokenLines(List<TokenLine> tokenLines, List<TreeNode> decisionTrees) {
        List<AddressBlock> addressBlocks = new LinkedList<>();

        for (int i = 0; i < tokenLines.size(); i++) {
            int addressDepth = checkIfAddressBegins(tokenLines, i, decisionTrees);
            if (addressDepth > 0) {
                int endIndex = Math.min(i + addressDepth, tokenLines.size());
                addressBlocks.add(new AddressBlock(tokenLines.subList(i, endIndex)));
            }
        }
        return addressBlocks;
    }

    public List<AddressBlock> getAddressesForFile(String fileName, List<TreeNode> decisionTrees) {
        Optional<FileContent> fileContent = fileUtils.loadFile(new File(fileName));
        List<AddressBlock> addressBlocks = new ArrayList<>();

        if (fileContent.isPresent()) {
            addressBlocks = getAddressesFromTokenLines(fileContent.get().getTokenLines(), decisionTrees);
            log.info("Number of address blocks found for file " + fileName
                    + " is " + addressBlocks.size());
            StringBuilder stringBuilder = new StringBuilder();
            for (AddressBlock addressBlock : addressBlocks) {
                stringBuilder.append(addressBlock.printValue()).append("\n\n");
            }
            log.info(stringBuilder.toString());
        } else {
            log.warn("Unable to read file content from " + fileName);
        }
        return addressBlocks;
    }
}
