package de.hu_berlin.slice.plugin.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.part.ViewPart;

import com.google.common.base.Throwables;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;

import de.hu_berlin.slice.ast.ASTService;
import de.hu_berlin.slice.plugin.AnalysisScopeFactory;
import de.hu_berlin.slice.plugin.BundleService;
import de.hu_berlin.slice.plugin.GuiceModule;
import de.hu_berlin.slice.plugin.PluginImages;
import de.hu_berlin.slice.plugin.ProjectService;
import de.hu_berlin.slice.plugin.WorkspaceService;
import de.hu_berlin.slice.plugin.context.EditorContextFactory;
import de.hu_berlin.slice.plugin.context.EditorContextFactory.EditorContext;

/**
 * Slice View
 *
 * @author IShowerNaked
 */
public class SliceView extends ViewPart {

    /** The ID of the view as specified by the extension. */
    public static final String ID = "de.hu_berlin.slice.plugin.view.SliceView";

    @Inject
    IWorkbench workbench;

    // -------------------
    // -- DI stuff here --
    // -------------------

    Injector injector = Guice.createInjector(new GuiceModule());

    EditorContextFactory editorContextFactory = injector.getInstance(EditorContextFactory.class);
    ASTService           astService           = injector.getInstance(ASTService.class);
    BundleService        bundleService        = injector.getInstance(BundleService.class);
    ProjectService       projectService       = injector.getInstance(ProjectService.class);
    WorkspaceService     workspaceService     = injector.getInstance(WorkspaceService.class);
    AnalysisScopeFactory analysisScopeFactory = injector.getInstance(AnalysisScopeFactory.class);

    // -------------------
    // -- UI stuff here --
    // -------------------

    private SourceViewer console;

    // @see configureActions()
    private Action clearViewAction;
    private Action refreshViewAction;
    private Action sliceForwardAction;
    private Action sliceBackwardAction;

    @Override
    public void createPartControl(Composite parent) {

        // we use a simple source viewer as a console for debug output
        console = new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

        IDocument document = new Document();
        console.setDocument(document);

        configureActions();
        configureActionBars();
    }

    //
    // Set-up for action bar contributions
    //

    private void configureActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalPullDown(bars.getMenuManager());
        fillLocalToolBar(bars.getToolBarManager());
    }

    private void fillLocalPullDown(IMenuManager manager) {
        // nothing here yet
        // but later this will be a nice place to add expert functionality such as settings, filters etc.
    }

    /**
     * Not yet in use.
     */
    @SuppressWarnings("unused")
    private void fillContextMenu(IMenuManager manager) {
        manager.add(clearViewAction);
        manager.add(refreshViewAction);
        // allow other plug-ins to add functionality here
        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(sliceBackwardAction);
        manager.add(sliceForwardAction);
        manager.add(new Separator());
        manager.add(clearViewAction);
        manager.add(refreshViewAction);
    }

    private void configureActions() {

        //
        // Action to clear the view
        //
        clearViewAction = createPlaceholderAction("Clear view button clicked!");
        clearViewAction.setText("Clear");
        clearViewAction.setToolTipText("Clears the slice result view.");
        clearViewAction.setImageDescriptor(PluginImages.DESC_CLEAR);

        //
        // Action to refresh the view.
        //
        refreshViewAction = createPlaceholderAction("Refresh view button clicked!");
        refreshViewAction.setText("Refresh");
        refreshViewAction.setToolTipText("Refreshes the slice result view.");
        refreshViewAction.setImageDescriptor(PluginImages.DESC_UPDATE);
        // enable refresh on pressing F5 etc.
        refreshViewAction.setActionDefinitionId(IWorkbenchCommandConstants.FILE_REFRESH);

        //
        // Action to perform backward slice
        //
        sliceBackwardAction = new Action() {
            @Override
            public void run() {
                demo(); // demo
            }
        };
        sliceBackwardAction.setText("Slice backwards");
        sliceBackwardAction.setToolTipText("Performs a backward slice.");
        sliceBackwardAction.setImageDescriptor(PluginImages.DESC_RUN_BACKWARD);

        //
        // Action to perform forward slice
        //
        sliceForwardAction = createPlaceholderAction("Slice forward button clicked!");
        sliceForwardAction.setText("Slice forward");
        sliceForwardAction.setToolTipText("Performs a forward slice.");
        sliceForwardAction.setImageDescriptor(PluginImages.DESC_RUN_FORWARD);
    }

    //
    // Action implementations.
    //

    /**
     * Demo, wrong place :)
     */
    private void demo() {

        List<String> out = new ArrayList<>();

        try {

            EditorContext editorContext = editorContextFactory.create(workbench);

            ITextSelection    textSelection     = editorContext.getTextSelection();
            ICompilationUnit  compilationUnit   = editorContext.getCompilationUnit();
            IJavaProject      javaProject       = editorContext.getJavaProjectContext().getJavaProject();
            ASTNode           ast               = editorContext.getAST();
            Statement         statementNode     = editorContext.getStatementNode();
            MethodDeclaration methodDeclaration = editorContext.getMethodDeclaration();

            out.add("Compilation unit: "                 + compilationUnit.getSource());
            out.add("Text selected: "                    + textSelection.getText());
            out.add("- offset: "                         + textSelection.getOffset());
            out.add("- length: "                         + textSelection.getLength());
            out.add("Project name: "                     + javaProject.getElementName());
            out.add("Statement selected: "               + statementNode.toString());
            out.add("Statement offset: "                 + statementNode.getStartPosition());
            out.add("Statement length: "                 + statementNode.getLength());
            out.add("Method this statement belongs to: " + methodDeclaration.toString());

            //
            //
            //
            try {
                List<String> classPathList = projectService.resolveClassPathList(javaProject);

                String classPath = String.join(File.pathSeparator, classPathList);
                out.add("Project class paths: " + classPath);

                File exclusionsFile = bundleService.getFileByPath("dat/Java60RegressionExclusions.txt");
                out.add("Exclusions file '" + exclusionsFile.getName() + "' loaded from bundle.");

                AnalysisScope analysisScope = analysisScopeFactory.create(javaProject, exclusionsFile);

                ClassHierarchy classHierarchy = ClassHierarchyFactory.make(analysisScope);

                // "LMainlol"
                Iterable<Entrypoint> entrypoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(analysisScope, classHierarchy);
                for (Entrypoint ep : entrypoints) {
                    out.add("entrypoint: " + ep.getMethod().getSignature());
                }

                out.add(classHierarchy.toString());
            }
            catch (JavaModelException e) {
                out.add("Unable to resolve class paths.");
            }
        }
        catch (Exception e) {
            out.add("-- An error occured! --\n");
            out.add("message: " + e.getMessage());
            out.add("class: " + e.getClass().getName());
            out.add("stacktrace: " + Throwables.getStackTraceAsString(e));
        }

        console.getDocument().set(String.join("\n", out));
    }

    //
    // Utility methods.
    //

    private void alert(String msg) {
        MessageDialog.openInformation(console.getControl().getShell(), "Slice View", msg);
    }

    private Action createPlaceholderAction(String msg) {
        return new Action() {
            @Override
            public void run() {
                alert(msg);
            }
        };
    }

    //
    // Other methods.
    //

    @Override
    public void setFocus() {
        console.getControl().setFocus();
    }
}