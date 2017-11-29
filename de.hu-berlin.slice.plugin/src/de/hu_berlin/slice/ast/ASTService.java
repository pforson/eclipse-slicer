package de.hu_berlin.slice.ast;

import javax.inject.Singleton;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.text.ITextSelection;

/**
 * Utility class for AST construction and AST traversal.
 *
 * @author IShowerNaked
 */
@Singleton
public class ASTService {

    public ASTNode createAST(ICompilationUnit compilationUnit) {

        ASTParser parser = ASTParser.newParser(AST.JLS9);
        parser.setSource(compilationUnit);
        ASTNode astNode = parser.createAST(null);

        return astNode;
    }

    /**
     * Searches the given ASTNode and its subtree for the statement node that corresponds to the selected text portion.
     *
     * @param rootNode Some AST node that acts as the root node during traversal.
     * @param textSelection The text selection portion.
     * @return The statement node or NULL if no such node was found.
     */
    public Statement findStatementNodeForSelection(ASTNode rootNode, ITextSelection textSelection) {

        // We use NodeFinder to find the innermost node that fully contains the selection.
        NodeFinder nodeFinder = new NodeFinder(rootNode, textSelection.getOffset(), textSelection.getLength());
        ASTNode foundNode = nodeFinder.getCoveringNode();

        if (null == foundNode) {
            return null; // The selection does not correspond to any AST node.
        }

        // Determine the statement the selected node is a child of.
        // We achieve this by walking up the tree and stop traveling upon reaching a Statement node.
        ASTNode returnNode = foundNode;
        while (! (returnNode instanceof Statement)) {
            returnNode = returnNode.getParent();
            if (returnNode == returnNode.getRoot()) {
                return null; // We can't go up any further => no Statement node found. :<
            }
        }

        // At this point we can guarantee that returnNode is a Statement node. :>
        return (Statement)returnNode;
    }
}