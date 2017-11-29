package de.hu_berlin.slice.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.Bundle;

import de.hu_berlin.slice.Activator;

/**
 * @author IShowerNaked
 */
public class BundleService {

	public File getFileByPath(String path) throws URISyntaxException, IOException {

		Bundle bundle = Activator.getDefault().getBundle();
		URL exclusionsURL = bundle.getEntry("dat/Java60RegressionExclusions.txt");

		return new File(FileLocator.resolve(exclusionsURL).toURI());
	}
}