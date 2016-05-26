package wedt.randomforest;

import lombok.Value;
import wedt.utils.model.AddressBlock;
import wedt.utils.model.TokenType;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Value
public class TreeNode implements Serializable {
    private List<AddressBlock> addressBlocks;
    private TreeNode leftChild;
    private TreeNode rightChild;
    private Set<TokenType> tokenTypeSplitValue;
    private Integer depth;
    private boolean isLeaf;

    public TreeNode(List<AddressBlock> addressBlocks) {
        this.addressBlocks = addressBlocks;
        isLeaf = true;
        this.leftChild = null;
        this.rightChild = null;
        this.tokenTypeSplitValue = null;
        this.depth = null;
    }

    public TreeNode(List<AddressBlock> addressBlocks,
                    TreeNode leftChild,
                    TreeNode rightChild,
                    Set<TokenType> tokenTypeSplitValue,
                    Integer depth) {
        this.addressBlocks = addressBlocks;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.tokenTypeSplitValue = tokenTypeSplitValue;
        this.depth = depth;
        isLeaf = false;
    }

    public String printValue(String prefix) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefix).append("Token: ").append(tokenTypeSplitValue).append("\n");
        stringBuilder.append(prefix).append("Depth: ").append(depth).append("\n");
        for (AddressBlock addressBlock : addressBlocks) {
            stringBuilder.append(prefix);
            stringBuilder.append("Address:");
            stringBuilder.append(addressBlock.printValue());
            stringBuilder.append("\n");
        }
        if (leftChild != null) {
            stringBuilder.append(prefix).append("Left child (found)\n");
            stringBuilder.append(leftChild.printValue(prefix + "  "));
        }
        if (rightChild != null) {
            stringBuilder.append(prefix).append("Right child (not found)\n");
            stringBuilder.append(rightChild.printValue(prefix + "  "));
        }
        return stringBuilder.toString();
    }

    /**
     * Lisć oznaczający brak dopasowania do wzorca.
     */
    public boolean isLeafFalse() {
        return isLeaf && addressBlocks.isEmpty();
    }

    /**
     * Liść z pozytywnym dopasowaniem.
     */
    public boolean isLeafTrue() {
        return isLeaf && !addressBlocks.isEmpty();
    }


}
