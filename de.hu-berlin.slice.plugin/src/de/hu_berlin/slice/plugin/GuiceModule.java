package de.hu_berlin.slice.plugin;

import com.google.inject.AbstractModule;

import de.hu_berlin.slice.ast.ASTService;
import de.hu_berlin.slice.plugin.context.EditorContextFactory;
import de.hu_berlin.slice.plugin.context.JavaProjectContextFactory;

/**
 * @author IShowerNaked
 */
public class GuiceModule extends AbstractModule {

	@Override
	protected void configure() {

		this.bind(ASTService.class);
		this.bind(BundleService.class);
		this.bind(EclipseService.class);
		this.bind(EditorContextFactory.class);
		this.bind(JavaProjectContextFactory.class);
		this.bind(ProjectService.class);
	}
}