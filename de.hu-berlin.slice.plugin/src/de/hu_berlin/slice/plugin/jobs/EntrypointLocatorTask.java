package de.hu_berlin.slice.plugin.jobs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;

import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.SubtypesEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.shrikeBT.IInstruction;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.types.ClassLoaderReference;

import de.hu_berlin.slice.plugin.context.EditorContextFactory.EditorContext;

/**
 * @author IShowerNaked
 */
public class EntrypointLocatorTask implements ITask {

    @Override
    public void run(IProgressMonitor monitor, SlicingContext context) throws TaskException {
        monitor.subTask("Locating entrypoint...");

        try {
            EditorContext editorContext = context.editorContext;

            ASTNode          ast             = editorContext.getAST();
            ICompilationUnit compilationUnit = editorContext.getCompilationUnit();
            String           absolutePath    = compilationUnit.getCorrespondingResource().getLocation().toOSString();

            ClassHierarchy classHierarchy = context.classHierarchy;
            IClassLoader classLoader = classHierarchy.getLoader(ClassLoaderReference.Application);

            List<Entrypoint> entrypoints = new ArrayList<>();

            for (Iterator<IClass> classIterator = classLoader.iterateAllClasses(); classIterator.hasNext(); ) {

                IClass klass = classIterator.next();

                for (IMethod method : klass.getDeclaredMethods()) {

                    Entrypoint entrypoint = new SubtypesEntrypoint(method, classHierarchy);
                    entrypoints.add(entrypoint);
                }
            }

            AnalysisOptions options = new AnalysisOptions(context.analysisScope, entrypoints);

            // TODO: not sure what exactly this is for, but it seems to place a LambdaMethodTargetSelector to `options`. we must understand this sooner or later...
            Util.addDefaultSelectors(options, classHierarchy);

            CallGraphBuilder<InstanceKey> callGraphBuilder = ZeroXCFABuilder.make(classHierarchy, options, new AnalysisCacheImpl(), null, null, ZeroXInstanceKeys.ALLOCATIONS | ZeroXInstanceKeys.CONSTANT_SPECIFIC);

            CallGraph callGraph = callGraphBuilder.makeCallGraph(options, null);

            PointerAnalysis<InstanceKey> pointerAnalysis = callGraphBuilder.getPointerAnalysis();

//            for (CGNode cgEntryoint : callGraph.getEntrypointNodes()) {
//            }

            for (CGNode cgEntryoint : callGraph.getEntrypointNodes()) {
                IR ir = cgEntryoint.getIR();
                IBytecodeMethod cgBytecodeEntryoint = (IBytecodeMethod)ir.getMethod();
                IInstruction[] instructions = cgBytecodeEntryoint.getInstructions();
                for (int i = 0; i < instructions.length; ++i) {
                    int bcIndex = cgBytecodeEntryoint.getInstructionIndex(i);
                 // System.err.println("instr: " + bcIndex + " --- " + instructions[i]);
                    if (-1 != bcIndex) {
                        // int lineNumber = cgBytecodeEntryoint.getLineNmuber(bcIndex);

                     // System.err.println(cgBytecodeEntryoint.getSourcePosition(bcIndex));
                        // System.err.println(lineNumber);
                    }
                }
                // System.err.println(cgEntryoint.getMethod().toString());
            }

            System.err.println(callGraph.getNode(0).toString());

        } catch (Exception e) {
            throw new TaskException(null, e);
        }

       // Java2IRTranslator<JdtPosition> jdt2cast = makeCAstTranslator(ast, compilationUnit., source.getUnderlyingResource().getLocation().toOSString());

        // Entrypoint entrypoint = new DefaultEntrypoint(null, context.classHierarchy);

        // context.classHierarchy

        monitor.done();
    }
}