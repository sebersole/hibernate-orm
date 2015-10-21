/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.jandex.internal.ResourceLocator;
import org.hibernate.boot.jandex.spi.JandexIndexBuilder;
import org.hibernate.boot.jandex.spi.JandexIndexBuilderFactory;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.MetadataBuildingOptions;
import org.hibernate.cfg.AttributeConverterDefinition;

/**
 * @author Steve Ebersole
 */
public class ManagedResourcesImpl implements ManagedResources {
	private final JandexIndexBuilder jandexIndexBuilder;

	private Map<Class, AttributeConverterDefinition> attributeConverterDefinitionMap = new HashMap<Class,AttributeConverterDefinition>();
	private Set<Class> annotatedClassReferences = new LinkedHashSet<Class>();
	private Set<String> annotatedClassNames = new LinkedHashSet<String>();
	private Set<String> annotatedPackageNames = new LinkedHashSet<String>();
	private List<Binding> mappingFileBindings = new ArrayList<Binding>();

	public static ManagedResourcesImpl baseline(MetadataSources sources, MetadataBuildingOptions metadataBuildingOptions) {
		final JandexIndexBuilder jandexIndexBuilder = JandexIndexBuilderFactory.buildJandexIndexBuilder( metadataBuildingOptions );
		final ResourceLocator resourceLocator = new ResourceLocator(
				metadataBuildingOptions.getServiceRegistry().getService( ClassLoaderService.class )
		);

		final ManagedResourcesImpl impl = new ManagedResourcesImpl( jandexIndexBuilder );

		for ( AttributeConverterDefinition attributeConverterDefinition : metadataBuildingOptions.getAttributeConverters() ) {
			impl.addAttributeConverterDefinition( attributeConverterDefinition );
		}
		impl.annotatedClassReferences.addAll( sources.getAnnotatedClasses() );
		impl.annotatedClassNames.addAll( sources.getAnnotatedClassNames() );
		impl.annotatedPackageNames.addAll( sources.getAnnotatedPackages() );
		impl.mappingFileBindings.addAll( sources.getXmlBindings() );
		return impl;
	}

	private ManagedResourcesImpl(JandexIndexBuilder jandexIndexBuilder) {
		this.jandexIndexBuilder = jandexIndexBuilder;
	}

	@Override
	public Collection<AttributeConverterDefinition> getAttributeConverterDefinitions() {
		return Collections.unmodifiableCollection( attributeConverterDefinitionMap.values() );
	}

	@Override
	public Collection<Class> getAnnotatedClassReferences() {
		return Collections.unmodifiableSet( annotatedClassReferences );
	}

	@Override
	public Collection<String> getAnnotatedClassNames() {
		return Collections.unmodifiableSet( annotatedClassNames );
	}

	@Override
	public Collection<String> getAnnotatedPackageNames() {
		return Collections.unmodifiableSet( annotatedPackageNames );
	}

	@Override
	public Collection<Binding> getXmlMappingBindings() {
		return Collections.unmodifiableList( mappingFileBindings );
	}

	@Override
	public JandexIndexBuilder getJandexIndexBuilder() {
		return jandexIndexBuilder;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// package private

	void addAttributeConverterDefinition(AttributeConverterDefinition attributeConverterDefinition) {
		attributeConverterDefinitionMap.put(
				attributeConverterDefinition.getAttributeConverter().getClass(),
				attributeConverterDefinition
		);
	}

	void addAnnotatedClassReference(Class annotatedClassReference) {
		annotatedClassReferences.add( annotatedClassReference );
	}

	void addAnnotatedClassName(String annotatedClassName) {
		annotatedClassNames.add( annotatedClassName );
	}

	void addAnnotatedPackageName(String annotatedPackageName) {
		annotatedPackageNames.add( annotatedPackageName );
	}

	void addXmlBinding(Binding binding) {
		mappingFileBindings.add( binding );
	}

	/**
	 * The idea here is that scanning is not complete as well.  Initially, in {@link #baseline},
	 * we only know about things we were explicitly told about.  At this point we can assume
	 * we know about everything (all packages, classes and XML mappings).
	 */
	public void finishPreparation() {
		for ( AttributeConverterDefinition definition : attributeConverterDefinitionMap.values() ) {
			final String converterClassName = definition.getAttributeConverter().getClass().getName();
			jandexIndexBuilder.indexClass( converterClassName );
		}

		for ( Class annotatedClassReference : annotatedClassReferences ) {
			jandexIndexBuilder.indexClass( annotatedClassReference );
		}

		for ( String annotatedClassName : annotatedClassNames ) {
			jandexIndexBuilder.indexClass( annotatedClassName );
		}

		for ( String annotatedPackageName : annotatedPackageNames ) {
			jandexIndexBuilder.indexPackage( annotatedPackageName );
		}

		// todo : call jandexIndexBuilder for any known class references in the XML mappings

	}
}
