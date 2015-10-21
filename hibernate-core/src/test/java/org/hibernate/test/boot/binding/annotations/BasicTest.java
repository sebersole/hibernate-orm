/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.boot.binding.annotations;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.internal.ClassLoaderAccessImpl;
import org.hibernate.boot.internal.InFlightMetadataCollectorImpl;
import org.hibernate.boot.internal.MetadataBuilderImpl;
import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.model.process.spi.MetadataBuildingProcess;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.bind.ModelProcessor;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.ClassLoaderAccess;
import org.hibernate.cfg.AttributeConverterDefinition;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.TypeResolver;

import org.hibernate.testing.junit4.BaseUnitTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.jboss.jandex.IndexView;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Steve Ebersole
 */
public class BasicTest extends BaseUnitTestCase {
	StandardServiceRegistry ssr;

	@Before
	public void prepare() {
		ssr = new StandardServiceRegistryBuilder()
				.applySetting( AvailableSettings.ENABLE_AUTO_INDEX_MEMBER_TYPES, true )
				.build();
	}

	@After
	public void after() {
		if ( ssr != null ) {
			StandardServiceRegistryBuilder.destroy( ssr );
		}
	}

	@Entity
	public static class NoInheritanceEntity {
		@Id
		Integer id;
		String name;
	}

	@Test
	public void testSimpleNoInheritanceEntity() {
		MetadataSources sources = new MetadataSources( ssr );
		sources.addAnnotatedClass( NoInheritanceEntity.class );

		BindingResult result = initiateBinding( sources );

		assertThat( result.processedEntityNames.size(), equalTo( 1 ) );
		assertThat( result.inFlightMetadataCollector.getEntityBindingMap().size(), equalTo( 1 ) );

		PersistentClass pc = result.inFlightMetadataCollector.getEntityBindingMap().entrySet().iterator().next().getValue();
		assertThat( pc, notNullValue() );
		assertThat( pc, instanceOf( RootClass.class ) );
		assertThat( pc.getPropertyClosureSpan(), equalTo( 1 ) );

		assertThat( pc.getIdentifier(), notNullValue() );
		assertThat( pc.getIdentifierProperty(), notNullValue() );
		assertThat( pc.getIdentifierMapper(), nullValue() );
	}

	private BindingResult initiateBinding(MetadataSources sources) {
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Pieces from MetadataBuildingProcess (which is normally part of the
		// innards of building the Metadata from sources)
		MetadataBuilderImpl.MetadataBuildingOptionsImpl options = ( (MetadataBuilderImpl) sources.getMetadataBuilder() ).getOptions();

		ManagedResources managedResources = MetadataBuildingProcess.prepare( sources, options );

		final BasicTypeRegistry basicTypeRegistry = new BasicTypeRegistry();

		final InFlightMetadataCollectorImpl metadataCollector = new InFlightMetadataCollectorImpl(
				options,
				new TypeResolver( basicTypeRegistry, new TypeFactory() )
		);
		for ( AttributeConverterDefinition attributeConverterDefinition : managedResources.getAttributeConverterDefinitions() ) {
			metadataCollector.addAttributeConverter( attributeConverterDefinition );
		}

		final ClassLoaderService classLoaderService = options.getServiceRegistry().getService( ClassLoaderService.class );

		final ClassLoaderAccess classLoaderAccess = new ClassLoaderAccessImpl(
				options.getTempClassLoader(),
				classLoaderService
		);

		final MetadataBuildingContextRootImpl rootMetadataBuildingContext = new MetadataBuildingContextRootImpl(
				options,
				classLoaderAccess,
				metadataCollector
		);

		final IndexView jandexView = managedResources.getJandexIndexBuilder().buildIndexView();
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		RootAnnotationBindingContext rootAnnotationBindingContext = new RootAnnotationBindingContext(
				rootMetadataBuildingContext,
				jandexView,
				null
		);

		Set<String> processedEntityNames = new HashSet<String>();

		ModelProcessor.bindModel( rootAnnotationBindingContext, processedEntityNames );

		return new BindingResult( metadataCollector, processedEntityNames );
	}

	static class BindingResult {
		final InFlightMetadataCollectorImpl inFlightMetadataCollector;
		final Set<String> processedEntityNames;

		public BindingResult(
				InFlightMetadataCollectorImpl inFlightMetadataCollector,
				Set<String> processedEntityNames) {
			this.inFlightMetadataCollector = inFlightMetadataCollector;
			this.processedEntityNames = processedEntityNames;
		}
	}
}
