package wedt.markov;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import wedt.utils.FileUtils;
import wedt.utils.model.AddressBlock;
import wedt.utils.model.FileContent;
import wedt.utils.model.TokenLine;
import wedt.utils.model.TokenType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor(staticName = "of")
class PositionGuess<F> {
    private F guessValue;
    private Double probability;
}

@Data
@AllArgsConstructor(staticName = "of")
class RangeGuess<F> {
    private F rangeStart;
    private F rangeEnd;
    private Double probability;
}

@Slf4j
public class Markov {

    static final int N = 4;
    private static final int NUM_ADDRESSES = 5;
    private static final double THRESHOLD = 0.001;

    private Map<NGram, Integer> nOccurrences = new HashMap<>();
    private Map<NGram, Integer> nMinusOneOccurrences = new HashMap<>();

    private Map<Integer, Integer> addressLengths = new HashMap<>();
    private Integer addressBlocksCount = 0;

    void learnFromDir(String dir) {
        FileUtils utils = new FileUtils();
        List<FileContent> filesContents = utils.loadDirectory(dir);
        filesContents.forEach(this::learnFromFileContent);
    }

    private void learnFromFileContent(FileContent fileContent) {
        List<TokenLine> tokens = fileContent.getTokenLines();
        readNGrams(tokens, N, nOccurrences);
        readNGrams(tokens, N-1, nMinusOneOccurrences);

        List<AddressBlock> addressBlocks = fileContent.getAddressBlocks();
        for (AddressBlock addressBlock: addressBlocks) {
            int size = addressBlock.getTokenLines().size();
            if(addressLengths.containsKey(size)) {
                addressLengths.put(size, addressLengths.get(size) + 1);
            } else {
                addressLengths.put(size, 1);
            }
            addressBlocksCount++;
        }
    }

    private void readNGrams(List<TokenLine> tokens, int n, Map<NGram, Integer> occurrences) {
        List<NGram> nGrams = getPossibleNGrams(tokens, n);
        for(NGram nGram: nGrams) {
            if(occurrences.containsKey(nGram)) {
                occurrences.put(nGram, occurrences.get(nGram) + 1);
            } else {
                occurrences.put(nGram, 1);
            }
        }
    }

    private List<NGram> getPossibleNGrams(List<TokenLine> tokens, int n) {
        final int size = tokens.size();
        List<NGram> result = new LinkedList<>();
        for(int i = 0; i < size - n + 1; ++i) {
            List<TokenType> types = tokens.
                    subList(i, i + n).
                    stream().
                    map(TokenLine::getTokenType).
                    collect(Collectors.toCollection(LinkedList::new));
            result.add(new NGram(types));
        }
        return result;
    }

    private void estimateBlocksInDir(String dirName) {
        File dir = new File(dirName);
        if(dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                estimateBlocksInFile(file);
            }
        } else {
            log.error("Given path "+ dirName +" is not a valid directory");
        }
    }

    private void estimateBlocksInFile(File file) {
        System.out.println();
        System.out.println();
        System.out.println("Estimating file: " + file.getPath());
        FileUtils fileUtils = new FileUtils();
        Optional<FileContent> fileContent = fileUtils.loadFile(file);
        if(fileContent.isPresent()) {
            List<TokenLine> tokenLines = fileContent.get().getTokenLines();
            List<NGram> nMinusOneGrams = getPossibleNGrams(tokenLines, N - 1);
            List<PositionGuess<Integer>> possibleAddressStarts = getPossibleAddressStarts(nMinusOneGrams);
            List<PositionGuess<Integer>> possibleAddressEnds = getPossibleAddressEnds(nMinusOneGrams);
            List<RangeGuess<Integer>> possibleAdresses =
                    combinePossibleAddresses(possibleAddressStarts, possibleAddressEnds);
            possibleAdresses.sort((o1, o2) -> (new Double(Math.floor(o2.getProbability() - o1.getProbability()))).intValue());
            possibleAdresses = normalizeResultProbabilities(possibleAdresses);
            int counter = 0;
            for(RangeGuess<Integer> address: possibleAdresses) {
//                if(counter ++ == NUM_ADDRESSES) {
//                    break;
//                }
                printGuess(address, tokenLines);
            }
        } else {
            log.error("Can't open file: " + file.getPath());
        }
    }

    private void printGuess(RangeGuess<Integer> address, List<TokenLine> tokens) {
        System.out.println("Guess probability: " + address.getProbability() + ", lines: ("+address.getRangeStart()+"-"+address.getRangeEnd()+")");
        for(TokenLine tokenLine: tokens.subList(address.getRangeStart(), address.getRangeEnd())) {
            System.out.print(tokenLine.getValue() + " ");
        }
        System.out.println();
        System.out.println();
    }

    private List<RangeGuess<Integer>> normalizeResultProbabilities(
            List<RangeGuess<Integer>> possibleAdresses) {
        double max = possibleAdresses.get(0).getProbability();
        List<RangeGuess<Integer>> result = new LinkedList<>();
        for (RangeGuess<Integer> address: possibleAdresses) {
            result.add(RangeGuess.of(address.getRangeStart(), address.getRangeEnd(), address.getProbability()/max));
        }
        return result;
    }

    private List<RangeGuess<Integer>> combinePossibleAddresses(
            List<PositionGuess<Integer>> possibleAddressStarts,
            List<PositionGuess<Integer>> possibleAddressEnds) {
        List<RangeGuess<Integer>> result = new LinkedList<>();
        for(PositionGuess<Integer> start: possibleAddressStarts) {
            int startPos = start.getGuessValue();
            double startProb = start.getProbability();
            for(PositionGuess<Integer> end: possibleAddressEnds) {
                int endPos = end.getGuessValue();
                double endProb = end.getProbability();
                double prob = startProb * endProb * blockLengthProbability(endPos - startPos);
                if(prob > THRESHOLD) {
                    result.add(RangeGuess.of(startPos, endPos, prob));
                }
            }
        }
        return result;
    }

    private double blockLengthProbability(int length) {
        return 1.0 * addressLengths.getOrDefault(length, 0) / addressBlocksCount;
    }

    private List<PositionGuess<Integer>> getPossibleAddressStarts(List<NGram> nMinusOneGrams) {
        List<PositionGuess<Integer>> possibleBeginnings = new LinkedList<>();
        for(int i = 0; i < nMinusOneGrams.size(); ++i) {
            List<TokenType> beginning = nMinusOneGrams.get(i).getTokenList();
            NGram nMinusOneKey = new NGram(beginning);

            beginning.add(0, TokenType.BEGIN_ADDRESS);
            NGram nKey = new NGram(beginning);
            if(nOccurrences.containsKey(nKey)) {
                Double probability = 1.0 * nOccurrences.get(nKey) / nMinusOneOccurrences.get(nMinusOneKey);
                possibleBeginnings.add(PositionGuess.of(i, probability));
            }
        }
        return possibleBeginnings;
    }

    private List<PositionGuess<Integer>> getPossibleAddressEnds(List<NGram> nMinusOneGrams) {
        List<PositionGuess<Integer>> possibleEnds = new LinkedList<>();
        for(int i = 0; i < nMinusOneGrams.size(); ++i) {
            List<TokenType> end = nMinusOneGrams.get(i).getTokenList();
            NGram nMinusOneKey = new NGram(end);

            end.add(TokenType.END_ADDRESS);
            NGram nKey = new NGram(end);
            if(nOccurrences.containsKey(nKey)) {
                Double probability = 1.0 * nOccurrences.get(nKey) / nMinusOneOccurrences.get(nMinusOneKey);
                possibleEnds.add(PositionGuess.of(i + N-1, probability));
            }
        }
        return possibleEnds;
    }

    public static void main(String[] args) {
        String dirName = args[0];
        String fileName = args[1];
        Markov markov = new Markov();
        markov.learnFromDir(dirName);
        markov.estimateBlocksInDir(fileName);
    }

}
