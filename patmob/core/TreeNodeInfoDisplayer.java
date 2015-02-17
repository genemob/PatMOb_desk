package patmob.core;

import patmob.data.PatentTreeNode;

/**
 * This interface defines methods used to display information about the
 * components of the tree to the user.
 * @author piotr
 */
public interface TreeNodeInfoDisplayer {

    public void displayNodeInfo(PatentTreeNode node);

    public void displayText(String text);
}
