package de.hu_berlin.slice.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.util.config.AnalysisScopeReader;

import de.hu_berlin.slice.plugin.eclipse.classpath.ClasspathLoader;
import de.hu_berlin.slice.plugin.eclipse.classpath.ClasspathScope;
import de.hu_berlin.slice.plugin.eclipse.classpath.LibraryClasspathResolver;
import de.hu_berlin.slice.plugin.eclipse.classpath.SourceClasspathResolver;

@Singleton
public class AnalysisScopeFactory {

    @Inject
    SourceClasspathResolver sourceClasspathResolver;

    @Inject
    LibraryClasspathResolver libraryClasspathResolver;

    public AnalysisScope create(IJavaProject javaProject, File exclusionsFile) throws Exception {

        String scopeFileName = "dat/SyntheticJ2SEModel.txt";
        AnalysisScope analysisScope = AnalysisScopeReader.readJavaScope(scopeFileName, exclusionsFile, this.getClass().getClassLoader());

        Map<ClasspathLoader, List<Module>> modules = getModules(javaProject);
        for (ClasspathLoader classpathLoader : modules.keySet()) {
            for (Module module : modules.get(classpathLoader)) {
                analysisScope.addToScope(classpathLoader.getClassLoaderReference(), module);
            }
        }

        return analysisScope;
    }

    public Map<ClasspathLoader, List<Module>> getModules(IJavaProject javaProject) throws Exception {

        IClasspathEntry[] classPathEntries = javaProject.getResolvedClasspath(true);

        ClasspathScope classpathScope = new ClasspathScope(javaProject);
        classpathScope.setIncludeSource(false);

        Map<ClasspathLoader, List<Module>> modules = new HashMap<>();
        for (ClasspathLoader classpathLoader : ClasspathLoader.values()) {
            modules.put(classpathLoader, new ArrayList<>());
        }

        for (IClasspathEntry classpathEntry : classPathEntries) {

            Map.Entry<ClasspathLoader, Module> moduleEntry;

            switch (classpathEntry.getEntryKind()) {
                case IClasspathEntry.CPE_SOURCE:
                    moduleEntry = sourceClasspathResolver.resolve(classpathScope, classpathEntry);
                    break;
                case IClasspathEntry.CPE_LIBRARY:
                    moduleEntry = libraryClasspathResolver.resolve(classpathScope, classpathEntry);
                    break;
                default:
                    throw new Exception();
            }

            if (null != moduleEntry) {
                modules.get(moduleEntry.getKey()).add(moduleEntry.getValue());
            }
        }

        return modules;
    }
}