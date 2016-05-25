package wedt.utils;

import lombok.extern.slf4j.Slf4j;
import wedt.utils.model.FileContent;
import wedt.utils.model.TokenLine;
import wedt.utils.model.TokenType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class FileUtils {

    public List<FileContent> loadDirectory(String dirName) {
        List<FileContent> result = new ArrayList<>();
        File directory = new File(dirName);

        if (!directory.isDirectory()) {
            log.error(dirName + " is not a directory.");
        } else {
            for (File file : directory.listFiles()) {
                Optional<FileContent> fileContent = loadFile(file);
                if (fileContent.isPresent()) {
                    result.add(fileContent.get());
                } else {
                    log.warn("Skipping file " + file.getName());
                }
            }
        }

        log.info("Final result of loading directory " + dirName + " is:");
        for (FileContent fileContent : result) {
            log.info("FileName=" + fileContent.getFileName() +
                    ", allTokens=" + fileContent.getTokenLines().size() +
                    ", addressBlocks=" + fileContent.getAddressBlocks().size());
        }

        return result;
    }

    public Optional<FileContent> loadFile(File file) {
        BufferedReader reader = null;
        List<TokenLine> tokenLines = new LinkedList<>();
        boolean isBroken = false;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text;

            while ((text = reader.readLine()) != null) {
                if (!text.isEmpty()) {
                    int splitIndex = text.indexOf(" ");
                    TokenLine tokenLine;
                    if (splitIndex == -1) {
                        tokenLine = new TokenLine(TokenType.valueOf(text), null);
                    } else {
                        String tokenName = text.substring(0, splitIndex);
                        String tokenValue = text.substring(splitIndex);
                        tokenLine = new TokenLine(TokenType.valueOf(tokenName), tokenValue);
                    }
                    tokenLines.add(tokenLine);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            isBroken = true;
            log.warn("Exception while processing file " + file.getName(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.warn("Exception while closing file " + file.getName(), e);
            }
        }

        if (isBroken) {
            return Optional.empty();
        } else {
            return Optional.of(new FileContent(file.getName(), tokenLines));
        }
    }
}
