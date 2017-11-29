package de.hu_berlin.slice.plugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author IShowerNaked
 */
public class ProjectService {

    public List<String> resolveClassPathList(IJavaProject javaProject) throws JavaModelException {

        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

        List<String> classPathList = new ArrayList<>();

        IClasspathEntry[] classPathEntries = javaProject.getResolvedClasspath(true);
        for (IClasspathEntry classpathEntry : classPathEntries) {
            IPath path = classpathEntry.getPath();
            IResource resource = workspaceRoot.findMember(path);
            if (null == resource) {
                classPathList.add(path.toOSString());
            }
            else {
                classPathList.add(resource.getLocation().toOSString());
            }
        }

        return classPathList;
    }
}