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
import org.hibernate.boot.jandex.spi.JandexIndexBuilder;
import org.hibernate.boot.jaxb.mapping.JaxbEntity;
import org.hibernate.boot.jaxb.mapping.JaxbEntityMappings;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.convert.spi.ConverterDescriptor;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.internal.util.StringHelper;

import static java.lang.Boolean.TRUE;
import static org.hibernate.boot.jandex.internal.JandexIndexBuilderFactory.buildJandexIndexBuilder;

/**
 * @author Steve Ebersole
 */
public class ManagedResourcesImpl implements ManagedResources {
	private final Set<Class<?>> annotatedClassReferences = new LinkedHashSet<>();
	private final Set<String> annotatedClassNames = new LinkedHashSet<>();
	private final Set<String> annotatedPackageNames = new LinkedHashSet<>();
	private final List<Binding<?>> mappingFileBindings = new ArrayList<>();

	private final Map<Class<?>, ConverterDescriptor> attributeConverterDescriptorMap = new HashMap<>();

	private final Map<String, Class<?>> extraQueryImports;

	private final JandexIndexBuilder jandexIndexBuilder;

	public ManagedResourcesImpl(MetadataSources sources, BootstrapContext bootstrapContext) {
		this.jandexIndexBuilder = buildJandexIndexBuilder( bootstrapContext );

		this.annotatedClassReferences.addAll( sources.getAnnotatedClasses() );
		this.annotatedClassNames.addAll( sources.getAnnotatedClassNames() );
		this.annotatedPackageNames.addAll( sources.getAnnotatedPackages() );
		this.mappingFileBindings.addAll( sources.getXmlBindings() );
		this.extraQueryImports = sources.getExtraQueryImports() == null
				? Collections.emptyMap()
				: sources.getExtraQueryImports();

		bootstrapContext.getAttributeConverters().forEach( this::addAttributeConverterDefinition );
	}

	@Override
	public JandexIndexBuilder getJandexIndexBuilder() {
		return jandexIndexBuilder;
	}

	@Override
	public Collection<ConverterDescriptor> getAttributeConverterDescriptors() {
		return Collections.unmodifiableCollection( attributeConverterDescriptorMap.values() );
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
	public Collection<Binding<?>> getXmlMappingBindings() {
		return Collections.unmodifiableList( mappingFileBindings );
	}

	@Override
	public Map<String, Class<?>> getExtraQueryImports() {
		return extraQueryImports;
	}


	public void addAttributeConverterDefinition(ConverterDescriptor descriptor) {
		attributeConverterDescriptorMap.put( descriptor.getAttributeConverterClass(), descriptor );
	}

	public void addAnnotatedClassReference(Class<?> annotatedClassReference) {
		annotatedClassReferences.add( annotatedClassReference );
	}

	public void addAnnotatedClassName(String annotatedClassName) {
		annotatedClassNames.add( annotatedClassName );
	}

	public void addAnnotatedPackageName(String annotatedPackageName) {
		annotatedPackageNames.add( annotatedPackageName );
	}

	public void addXmlBinding(Binding<?> binding) {
		mappingFileBindings.add( binding );
	}

	/**
	 * When ManagedResources is first built, scanning is not yet complete.  At that point,
	 * we only know about things we were explicitly told about.
	 * <p/>
	 * Here, we can now assume all resources are known.
	 */
	public void finishPreparation() {
		attributeConverterDescriptorMap.forEach( (type, descriptor) -> jandexIndexBuilder.indexClass( type ) );

		annotatedClassReferences.forEach( jandexIndexBuilder::indexClass );
		annotatedClassNames.forEach( jandexIndexBuilder::indexClass );
		annotatedPackageNames.forEach( jandexIndexBuilder::indexPackage );

		mappingFileBindings.forEach( this::handleXmlBindings );
	}

	private void handleXmlBindings(Binding<?> binding) {
		if ( binding.getRoot() instanceof JaxbEntityMappings ) {
			final JaxbEntityMappings root = (JaxbEntityMappings) binding.getRoot();
			effectiveXmlPackageName = StringHelper.nullIfEmpty( root.getPackage() );
			try {
				root.getEntities().forEach( this::addXmlMappedEntity );
			}
			finally {
				effectiveXmlPackageName = null;
			}
		}
	}

	private String effectiveXmlPackageName;

	private void addXmlMappedEntity(JaxbEntity mapping) {
		if ( mapping.isMetadataComplete() == TRUE ) {
			// no need to index
			return;
		}

		jandexIndexBuilder.indexClass( fqn( effectiveXmlPackageName, mapping.getClazz() ) );
	}

	private String fqn(String effectivePackageName, String className) {
		if ( className.contains( "." ) || effectivePackageName == null ) {
			return className;
		}

		return StringHelper.qualify( effectivePackageName, className );
	}
}
