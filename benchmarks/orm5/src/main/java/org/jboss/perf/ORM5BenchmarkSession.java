/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.jboss.perf;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;

import org.hibernate.bytecode.enhance.spi.EnhancementContext;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.hibernate.jpa.boot.spi.Bootstrap;
import org.hibernate.jpa.boot.spi.PersistenceUnitDescriptor;

/**
 * @author Andrea Boriero
 */
public class ORM5BenchmarkSession extends BaseBenchmarkSession {

	@Override
	protected EntityManagerFactory buildEntityManagerFactory() {
		return Bootstrap.getEntityManagerFactoryBuilder(
				buildPersistenceUnitDescriptor(),
				buildSettings()
		).build();
	}

	@Override
	protected BenchmarkHQLParserTree buildHqlParser() {
		return new ORM5HQLParser();
	}

	@Override
	protected BenchmarkHQLSemanticModelInterpreter buildHqInterpreter() {
		BenchmarkHQLSemanticModelInterpreter hqlInterpreter = new ORM5HQLSemanticModelInterpreter();
		hqlInterpreter.configure( entityManagerFactory );
		return hqlInterpreter;
	}

	private Map buildSettings() {
		Map settings = getConfig();
		addMappings( settings );
		if ( createSchema ) {
			settings.put( org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO, "create-drop" );
		}
		settings.put( org.hibernate.cfg.AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true" );
		settings.put( org.hibernate.cfg.AvailableSettings.DIALECT, Dialect.getDialect().getClass().getName() );
		return settings;
	}

	private Map getConfig() {
		Map<Object, Object> config = Environment.getProperties();
		ArrayList<Class> classes = new ArrayList<>();

		classes.addAll( Arrays.asList( annotatedClasses ) );
		config.put( org.hibernate.jpa.AvailableSettings.LOADED_CLASSES, classes );

		return config;
	}

	private void addMappings(Map settings) {
		if ( hbmfiles != null ) {
			settings.put( org.hibernate.jpa.AvailableSettings.HBXML_FILES, String.join( ",", hbmfiles ) );
		}
	}

	private PersistenceUnitDescriptor buildPersistenceUnitDescriptor() {
		return new TestingPersistenceUnitDescriptorImpl( getClass().getSimpleName() );
	}

	public static class TestingPersistenceUnitDescriptorImpl implements PersistenceUnitDescriptor {
		private final String name;

		public TestingPersistenceUnitDescriptorImpl(String name) {
			this.name = name;
		}

		@Override
		public URL getPersistenceUnitRootUrl() {
			return null;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getProviderClassName() {
			return HibernatePersistenceProvider.class.getName();
		}

		@Override
		public boolean isUseQuotedIdentifiers() {
			return false;
		}

		@Override
		public boolean isExcludeUnlistedClasses() {
			return false;
		}

		@Override
		public PersistenceUnitTransactionType getTransactionType() {
			return null;
		}

		@Override
		public ValidationMode getValidationMode() {
			return null;
		}

		@Override
		public SharedCacheMode getSharedCacheMode() {
			return null;
		}

		@Override
		public List<String> getManagedClassNames() {
			return null;
		}

		@Override
		public List<String> getMappingFileNames() {
			return null;
		}

		@Override
		public List<URL> getJarFileUrls() {
			return null;
		}

		@Override
		public Object getNonJtaDataSource() {
			return null;
		}

		@Override
		public Object getJtaDataSource() {
			return null;
		}

		@Override
		public Properties getProperties() {
			return null;
		}

		@Override
		public ClassLoader getClassLoader() {
			return null;
		}

		@Override
		public ClassLoader getTempClassLoader() {
			return null;
		}

		@Override
		public void pushClassTransformer(EnhancementContext enhancementContext) {
		}
	}
}
