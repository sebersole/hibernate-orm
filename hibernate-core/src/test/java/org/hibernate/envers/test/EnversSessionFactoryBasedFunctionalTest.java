/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Metamodel;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.boot.AuditService;
import org.hibernate.envers.configuration.EnversSettings;
import org.hibernate.internal.util.StringHelper;
import org.junit.jupiter.api.Tag;

import org.jboss.logging.Logger;

import org.hibernate.testing.junit5.StandardTags;
import org.hibernate.testing.junit5.dynamictests.AbstractDynamicTest;
import org.hibernate.testing.junit5.dynamictests.DynamicAfterAll;
import org.hibernate.testing.junit5.envers.EnversSessionFactoryProducer;
import org.hibernate.testing.junit5.envers.EnversSessionFactoryScope;
import org.hibernate.testing.junit5.envers.Strategy;

/**
 * Envers base test case that uses a native Hibernate {@link SessionFactory} configuration.
 *
 * @author Chris Cranford
 */
@Tag(StandardTags.ENVERS)
public class EnversSessionFactoryBasedFunctionalTest
		extends AbstractDynamicTest<EnversDynamicExecutionContext>
		implements EnversSessionFactoryProducer {

	private static final Logger log = Logger.getLogger( EnversSessionFactoryBasedFunctionalTest.class );
	private static final Class<?>[] NO_CLASSES = new Class<?>[0];
	private static final String[] NO_MAPPINGS = new String[0];

	private EnversSessionFactoryScope sessionFactoryScope;
	private String auditStrategyName;
	private AuditReader auditReader;

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

	@DynamicAfterAll
	public void releaseSessionFactory() {
		if ( auditReader != null ) {
			auditReader.close();
			auditReader = null;
		}

		log.debugf( "Releasing SessionFactory (%s)", auditStrategyName );
		sessionFactoryScope.releaseSessionFactory();
	}

	@Override
	protected Collection<EnversDynamicExecutionContext> getExecutionContexts() {
		List<EnversDynamicExecutionContext> contexts = new ArrayList<>();
		for ( Strategy strategy : Strategy.values() ) {
			contexts.add( new EnversDynamicExecutionContext( strategy ) );
		}
		return contexts;
	}

	@Override
	protected void injectExecutionContext(EnversDynamicExecutionContext context) {
		sessionFactoryScope = new EnversSessionFactoryScope( this, context.getStrategy() );
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
		return "org/hibernate/envers/test/";
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

	protected Session openSession() {
		return sessionFactoryScope.getSessionFactory().openSession();
	}

	protected AuditReader getAuditReader() {
		if ( auditReader == null ) {
			auditReader = sessionFactoryScope.getSessionFactory().openAuditReader();
		}
		return auditReader;
	}

	protected Metamodel getMetamodel() {
		return sessionFactoryScope.getSessionFactory().getMetamodel();
	}

	protected AuditService getAuditService() {
		return sessionFactoryScope.getSessionFactory().getServiceRegistry().getService( AuditService.class );
	}

	private void applyMetadataSources(MetadataSources metadataSources) {
		for ( String mappingFile : getMappings() ) {
			metadataSources.addResource( getBaseForMappings() + mappingFile );
		}
		for ( Class<?> annotatedClass : getAnnotatedClasses() ) {
			metadataSources.addAnnotatedClass( annotatedClass );
		}
	}
}
