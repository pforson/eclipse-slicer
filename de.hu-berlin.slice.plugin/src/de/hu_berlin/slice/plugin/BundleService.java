package de.hu_berlin.slice.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Singleton;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

import de.hu_berlin.slice.Activator;

/**
 * @author IShowerNaked
 */
@Singleton
public class BundleService {

    public File getFileByPath(String path) throws URISyntaxException, IOException {

        Bundle bundle = Activator.getDefault().getBundle();
        URL exclusionsURL = bundle.getEntry(path);

        return new File(FileLocator.resolve(exclusionsURL).toURI());
    }
}