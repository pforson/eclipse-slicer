package de.hu_berlin.slice.plugin.context;

import javax.inject.Inject;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.texteditor.ITextEditor;

import de.hu_berlin.slice.plugin.EclipseService;
import de.hu_berlin.slice.plugin.context.JavaProjectContextFactory.JavaProjectContext;
import de.hu_berlin.slice.plugin.context.JavaProjectContextFactory.JavaProjectContextFactoryException;

/**
 * @author IShowerNaked
 */
public class EditorContextFactory {

	@Inject
	EclipseService eclipseService;

	@Inject
	JavaProjectContextFactory javaProjectContextFactory;

	/**
	 * @author IShowerNaked
	 */
	public class EditorContext {

		private ITextEditor textEditor;

		private ICompilationUnit compilationUnit;

		private ITextSelection textSelection;

		private JavaProjectContext javaProjectContext;

		public JavaProjectContext getJavaProjectContext() {
			return javaProjectContext;
		}

		public ITextEditor getTextEditor() {
			return textEditor;
		}

		public ICompilationUnit getCompilationUnit() {
			return compilationUnit;
		}

		public ITextSelection getTextSelection() {
			return textSelection;
		}
	}

	public class EditorContextFactoryException extends Exception {

		private static final long serialVersionUID = 1L;

		public EditorContextFactoryException(String msg, Exception e) {
			super(msg, e);
		}
	}

	public EditorContext create(IWorkbench workbench) throws EditorContextFactoryException {

		IEditorPart editorPart = eclipseService.getActiveEditor(workbench);
		if (null == editorPart) {
			throw new EditorContextFactoryException("No active editor available.", null);
		}


		if (! (editorPart instanceof ITextEditor)) {
			throw new EditorContextFactoryException("Active editor is not a text editor.", null);
		}

		ITextEditor textEditor = (ITextEditor)editorPart;

		ICompilationUnit compilationUnit = eclipseService.getCompilationUnitFromEditorPart(editorPart);
		if (null == compilationUnit) {
			throw new EditorContextFactoryException("Editor doest not contain a compilation unit.", null);
		}

		ISelection selection = textEditor.getSelectionProvider().getSelection();
		if (! (selection instanceof ITextSelection)) {
			throw new EditorContextFactoryException("Unable to determine to text selection.", null);
		}

		ITextSelection textSelection = (ITextSelection)selection;

		JavaProjectContext javaProjectContext;
		try {
			javaProjectContext = javaProjectContextFactory.create(compilationUnit);
		}
		catch (JavaProjectContextFactoryException e) {
			throw new EditorContextFactoryException(null, e);
		}

		// ----------------------------------
		// -- Build and return the context --
		// ----------------------------------

		EditorContext context = new EditorContext();
		context.textEditor = textEditor;
		context.compilationUnit = compilationUnit;
		context.textSelection = textSelection;
		context.javaProjectContext = javaProjectContext;

		return context;
	}
}