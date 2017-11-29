package de.hu_berlin.slice.plugin;

import javax.inject.Singleton;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author IShowerNaked
 */
@Singleton
public class EclipseService {

	/**
	 * Returns the active editor (potentially containing Java stuff ;o)
	 *   or null if no active editor was found.
	 *
	 * @return
	 */
	public IEditorPart getActiveEditor(IWorkbench workbench)
	{
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		if (null != page) {
			return page.getActiveEditor();
		}

		return null;
	}

	/**
	 * Returns the compilation unit for the given editor part.
	 *   (consisting of package declaration, import directives, top level element bla)
	 *
	 * @param editorPart
	 * @return
	 */
	public ICompilationUnit getCompilationUnitFromEditorPart(IEditorPart editorPart)
	{
		// can this be null? :<???
		IEditorInput editorInput = editorPart.getEditorInput();

		// returns the Java type root, that is either an ICompilationUnit or IClassFile
		// since IClassFile represents some binary shit we're only interested in ICompilationUnit (the source)
		ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(editorInput);

		if (typeRoot instanceof ICompilationUnit) {
			return (ICompilationUnit)typeRoot;
		}

		return null;
	}
}