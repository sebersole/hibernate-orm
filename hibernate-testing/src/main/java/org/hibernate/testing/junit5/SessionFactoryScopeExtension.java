/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.testing.junit5;

import java.util.Optional;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.create;

/**
 * The thing that actually manages lifecycle of the SessionFactory related to a
 * test class.  Work in conjunction with SessionFactoryScope and SessionFactoryScopeContainer
 *
 * @see SessionFactoryScope
 * @see SessionFactoryScopeContainer
 * @see SessionFactoryProducer
 *
 * @author Steve Ebersole
 */
public class SessionFactoryScopeExtension
		implements TestInstancePostProcessor, BeforeAllCallback, AfterEachCallback, AfterAllCallback,
				TestExecutionExceptionHandler {

	public static ExtensionContext.Namespace namespace(Object testInstance) {
		return create( SessionFactoryScopeExtension.class.getName(), testInstance );
	}

	public static final Object SESSION_FACTORY_KEY = "SESSION_FACTORY";

	private static final Object IS_LIFECYCLE_PER_CLASS_KEY = "IS_LIFECYCLE_PER_CLASS";

	public SessionFactoryScopeExtension() {
		System.out.println( "SessionFactoryScopeExtension#<init>" );
	}

	private void releaseSessionFactoryIfPresent(Object testInstance, ExtensionContext context) {
		// We need the exact same context the session factory was defined on, i.e. the class context
		// Otherwise the remove() operation on the store would not work
		if ( context.getTestMethod().isPresent() ) {
			context = context.getParent().get();
		}
		ExtensionContext.Store store = context.getStore( namespace( testInstance ) );
		final SessionFactoryScope scope = (SessionFactoryScope) store.remove( SESSION_FACTORY_KEY );
		if ( scope != null ) {
			scope.releaseSessionFactory();
		}
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// TestInstancePostProcessor

	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
		System.out.println( "SessionFactoryScopeExtension#postProcessTestInstance" );

		if ( SessionFactoryScopeContainer.class.isInstance( testInstance ) ) {
			final SessionFactoryScopeContainer scopeContainer = SessionFactoryScopeContainer.class.cast(
					testInstance );
			final SessionFactoryScope scope = new SessionFactoryScope( scopeContainer.getSessionFactoryProducer() );
			ExtensionContext.Store store = context.getStore( namespace( testInstance ) );
			store.put( SESSION_FACTORY_KEY, scope );

			scopeContainer.injectSessionFactoryScope( scope );
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// BeforeAllCallback

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		System.out.println( "SessionFactoryScopeExtension#beforeAll" );
		Optional<Object> testInstanceOptional = context.getTestInstance();
		if ( testInstanceOptional.isPresent() ) {
			Object testInstance = testInstanceOptional.get();
			ExtensionContext.Store store = context.getStore( namespace( testInstance ) );
			store.put( IS_LIFECYCLE_PER_CLASS_KEY, IS_LIFECYCLE_PER_CLASS_KEY );
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// AfterEachCallback

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		System.out.println( "SessionFactoryScopeExtension#afterEach" );
		Object testInstance = context.getRequiredTestInstance();
		ExtensionContext.Store store = context.getStore( namespace( testInstance ) );
		if ( store.get( IS_LIFECYCLE_PER_CLASS_KEY ) == null ) {
			releaseSessionFactoryIfPresent( testInstance, context );
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// AfterAllCallback

	@Override
	public void afterAll(ExtensionContext context) {
		System.out.println( "SessionFactoryScopeExtension#afterAll" );
		Optional<Object> testInstanceOptional = context.getTestInstance();
		if ( testInstanceOptional.isPresent() ) {
			Object testInstance = testInstanceOptional.get();
			ExtensionContext.Store store = context.getStore( namespace( testInstance ) );
			store.remove( IS_LIFECYCLE_PER_CLASS_KEY );
			releaseSessionFactoryIfPresent( testInstance, context );
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// TestExecutionExceptionHandler

	@Override
	public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
		Object testInstance = context.getRequiredTestInstance();
		releaseSessionFactoryIfPresent( testInstance, context );

		throw throwable;
	}
}
