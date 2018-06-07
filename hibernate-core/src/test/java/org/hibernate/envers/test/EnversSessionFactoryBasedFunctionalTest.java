/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.configuration.EnversSettings;
import org.hibernate.envers.strategy.AuditStrategy;
import org.hibernate.internal.util.StringHelper;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;

import org.jboss.logging.Logger;

import org.hibernate.testing.junit5.SessionFactoryScope;
import org.hibernate.testing.junit5.StandardTags;
import org.hibernate.testing.junit5.envers.EnversAfterAll;
import org.hibernate.testing.junit5.envers.EnversAfterEach;
import org.hibernate.testing.junit5.envers.EnversBeforeAll;
import org.hibernate.testing.junit5.envers.EnversBeforeEach;
import org.hibernate.testing.junit5.envers.EnversSessionFactoryProducer;
import org.hibernate.testing.junit5.envers.EnversSessionFactoryScope;
import org.hibernate.testing.junit5.envers.EnversTest;
import org.hibernate.testing.junit5.envers.RequiresAuditStrategy;
import org.hibernate.testing.junit5.envers.Strategy;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

/**
 * Envers base test case that uses a native Hibernate {@link SessionFactory} configuration.
 *
 * @author Chris Cranford
 */
@Tag( StandardTags.ENVERS )
public class EnversSessionFactoryBasedFunctionalTest implements EnversSessionFactoryProducer {
	private static final Logger log = Logger.getLogger( EnversSessionFactoryBasedFunctionalTest.class );
	private static final Class<?>[] NO_CLASSES = new Class<?>[0];
	private static final String[] NO_MAPPINGS = new String[0];

	private EnversSessionFactoryScope sessionFactoryScope;
	private String auditStrategyName;

	protected SessionFactory sessionFactory() {
		return sessionFactoryScope.getSessionFactory();
	}

	@Override
	public SessionFactory produceSessionFactory(String auditStrategyName) {
		log.debugf( "Producing SessionFactory (%s)", auditStrategyName );
		this.auditStrategyName = auditStrategyName;

		final StandardServiceRegistryBuilder ssrb = new StandardServiceRegistryBuilder();
		ssrb.applySetting( AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, Boolean.TRUE.toString() );
		ssrb.applySetting( AvailableSettings.HBM2DDL_AUTO, exportSchema() ? "create" : "none" );

		final Map<String, Object> settings = new HashMap<>();
		addSettings( settings );
		ssrb.applySettings( settings );

		final StandardServiceRegistry ssr = ssrb.build();
		try {
			MetadataSources metadataSources = new MetadataSources( ssr );
			applyMetadataSources( metadataSources );

			return metadataSources.buildMetadata().buildSessionFactory();
		}
		catch ( Throwable t ) {
			t.printStackTrace();
			StandardServiceRegistryBuilder.destroy( ssr );
			throw t;
		}
	}

	@EnversAfterAll
	public void releaseSessionFactory() {
		log.debugf( "Releasing SessionFactory (%s)", auditStrategyName );
		sessionFactoryScope.releaseSessionFactory();
	}

	@TestFactory
	public final List<DynamicNode> generateTests() throws Exception {
		final Class<? extends EnversSessionFactoryBasedFunctionalTest> testClass = getClass();

		final List<Method> testMethods = findMethods( testClass, method -> method.isAnnotationPresent( EnversTest.class ) );
		final List<Method> beforeAllMethods = findMethods( testClass, method -> method.isAnnotationPresent( EnversBeforeAll.class ) );
		final List<Method> beforeEachMethods = findMethods( testClass, method -> method.isAnnotationPresent( EnversBeforeEach.class ) );
		final List<Method> afterAllMethods = findMethods( testClass, method -> method.isAnnotationPresent( EnversAfterAll.class ) );
		final List<Method> afterEachMethods = findMethods( testClass, method -> method.isAnnotationPresent( EnversAfterEach.class ) );

		final RequiresAuditStrategy requiresAuditStrategyClass = testClass.getAnnotation( RequiresAuditStrategy.class );

		final List<DynamicNode> nodes = new ArrayList<>();

		for ( Strategy strategy : Strategy.values() ) {

			// Skip generating tests for the class if the strategy requirement is not specified.
			if ( !isStrategyRequiredSatisfied( requiresAuditStrategyClass, strategy ) ) {
				continue;
			}

			final EnversSessionFactoryBasedFunctionalTest testInstance = testClass.newInstance();
			testInstance.sessionFactoryScope = new EnversSessionFactoryScope( testInstance, strategy );

			final List<DynamicTest> tests = new ArrayList<>();

			// Invoke all @EnversBeforeAll callback hooks
			beforeAllMethods.forEach( method -> {
				tests.add( dynamicTest( method.getName(), () -> method.invoke( testInstance ) ) );
			} );

			// Invoke all @EnversTest methods
			testMethods.forEach( method -> {
				final RequiresAuditStrategy requiresAuditStrategyMethod = method.getAnnotation( RequiresAuditStrategy.class );
				if ( isStrategyRequiredSatisfied( requiresAuditStrategyMethod, strategy ) ) {
					tests.add(
							dynamicTest(
									method.getName(),
									() -> {
										// invoke @EnversBeforeEach hook
										for ( Method beforeEachMethod : beforeEachMethods ) {
											beforeEachMethod.invoke( testInstance );
										}

										Throwable invocationException = null;
										try {
											method.invoke( testInstance );
										}
										catch ( Throwable t ) {
											invocationException = t;
											throw t;
										}
										finally {
											try {
												for ( Method afterEachMethod : afterEachMethods) {
													afterEachMethod.invoke( testInstance );
												}
											}
											catch ( Throwable t ) {
												if ( invocationException == null ) {
													throw t;
												}
											}
										}
									}
							)
					);
				}
			} );

			// Invoke all @EnversAfterAll callback hooks
			afterAllMethods.forEach( method -> {
				tests.add( dynamicTest( method.getName(), () -> method.invoke( testInstance ) ) );
			} );

			// Construct a container for the strategy with all the generated test method nodes.
			nodes.add( dynamicContainer( testClass.getName() + " (" + strategy + ")", tests ) );
		}

		return nodes;
	}

	/**
	 * Returns whether the schema should be created or not.
	 */
	protected boolean exportSchema() {
		return true;
	}

	/**
	 * Return a list of annotated classes that should be added to the metadata configuration.
	 */
	protected Class<?>[] getAnnotatedClasses() {
		return NO_CLASSES;
	}

	/**
	 * Return a list of HBM mappings that should be added to the metadata configuration.
	 */
	protected String[] getMappings() {
		return NO_MAPPINGS;
	}

	/**
	 * Return the base package path for HBM mappings.  The default is {@code 'org/hibernate/test'}.
	 */
	protected String getBaseForMappings() {
		return "org/hibernate/test";
	}

	/**
	 * Add additional configuration settings to configure the test.
	 *
	 * @param settings A map of string-based configuration parameters.
	 */
	protected void addSettings(Map<String, Object> settings) {
		settings.put( EnversSettings.USE_REVISION_ENTITY_WITH_NATIVE_ID, Boolean.FALSE.toString() );
		if ( !StringHelper.isEmpty( auditStrategyName ) ) {
			settings.put( EnversSettings.AUDIT_STRATEGY, auditStrategyName );
		}
	}

	protected EnversSessionFactoryScope sessionFactoryScope() {
		return sessionFactoryScope;
	}

	protected AuditReader getAuditReader() {
		return AuditReaderFactory.get( sessionFactoryScope.getSessionFactory().openSession() );
	}

	private void applyMetadataSources(MetadataSources metadataSources) {
		for ( String mappingFile : getMappings() ) {
			metadataSources.addResource( getBaseForMappings() + mappingFile );
		}
		for ( Class<?> annotatedClass : getAnnotatedClasses() ) {
			metadataSources.addAnnotatedClass( annotatedClass );
		}
	}

	private boolean isStrategyRequiredSatisfied(RequiresAuditStrategy annotation, Strategy strategy) {
		if ( annotation == null ) {
			return true;
		}

		for ( Class<? extends AuditStrategy> strategyClass : annotation.value() ) {
			if ( strategy.isStrategy( strategyClass ) ) {
				return true;
			}
		}

		return false;
	}
}
