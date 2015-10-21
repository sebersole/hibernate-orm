/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.Set;

import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEntityMappings;
import org.hibernate.boot.jaxb.mapping.spi.JaxbPersistenceUnitDefaults;
import org.hibernate.boot.jaxb.spi.Binding;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.model.source.internal.annotations.bind.FetchProfileProcessor;
import org.hibernate.boot.model.source.internal.annotations.bind.FilterDefinitionProcessor;
import org.hibernate.boot.model.source.internal.annotations.bind.IdGeneratorProcessor;
import org.hibernate.boot.model.source.internal.annotations.bind.ModelProcessor;
import org.hibernate.boot.model.source.internal.annotations.bind.QueryProcessor;
import org.hibernate.boot.model.source.internal.annotations.bind.ResultSetMappingProcessor;
import org.hibernate.boot.model.source.internal.annotations.bind.TypeDefinitionProcessor;
import org.hibernate.boot.model.source.spi.MetadataSourceProcessor;
import org.hibernate.cfg.AnnotationBinder;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

/**
 * Main class responsible to creating and binding the Hibernate meta-model from annotations.
 * This binder only has to deal with the (jandex) annotation index/repository. XML configuration is already processed
 * and pseudo annotations are created.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class AnnotationMetadataSourceProcessorImpl implements MetadataSourceProcessor {
	private static final Logger log = Logger.getLogger( AnnotationMetadataSourceProcessorImpl.class );

	private final RootAnnotationBindingContext rootAnnotationBindingContext;

	public AnnotationMetadataSourceProcessorImpl(
			ManagedResources managedResources,
			final MetadataBuildingContextRootImpl rootMetadataBuildingContext,
			IndexView jandexView) {

		// we do need to look through the XML mapping bindings to find any defined <persistence-unit-defaults/>
		JaxbPersistenceUnitDefaults persistenceUnitDefaults = null;
		for ( Binding binding : managedResources.getXmlMappingBindings() ) {
			if ( binding.getRoot() instanceof JaxbEntityMappings ) {
				final JaxbEntityMappings entityMappings = (JaxbEntityMappings) binding.getRoot();
				if ( entityMappings.getPersistenceUnitMetadata() != null ) {
					persistenceUnitDefaults = entityMappings.getPersistenceUnitMetadata().getPersistenceUnitDefaults();
					if ( persistenceUnitDefaults != null ) {
						log.debugf( "Found persistence-unit-defaults [%s], skipping any others" + binding.getOrigin() );
						break;
					}
				}
			}
		}

		this.rootAnnotationBindingContext = new RootAnnotationBindingContext(
				rootMetadataBuildingContext,
				jandexView,
				persistenceUnitDefaults
		);
	}

	@Override
	public void prepare() {
		rootAnnotationBindingContext.getMetadataCollector().getDatabase().adjustDefaultNamespace(
				rootAnnotationBindingContext.getMappingDefaults().getImplicitCatalogName(),
				rootAnnotationBindingContext.getMappingDefaults().getImplicitSchemaName()
		);

		AnnotationBinder.bindDefaults( rootAnnotationBindingContext );
	}

	@Override
	public void processTypeDefinitions() {
		TypeDefinitionProcessor.bind( rootAnnotationBindingContext );
	}

	@Override
	public void processQueryRenames() {

	}

	@Override
	public void processNamedQueries() {
		QueryProcessor.bind( rootAnnotationBindingContext );
	}

	@Override
	public void processAuxiliaryDatabaseObjectDefinitions() {

	}

	@Override
	public void processIdentifierGenerators() {
		IdGeneratorProcessor.bind( rootAnnotationBindingContext );
	}

	@Override
	public void processFilterDefinitions() {
		FilterDefinitionProcessor.bind( rootAnnotationBindingContext );
	}

	@Override
	public void processFetchProfiles() {
		FetchProfileProcessor.bind( rootAnnotationBindingContext );
	}

	@Override
	public void prepareForEntityHierarchyProcessing() {

	}

	@Override
	public void processEntityHierarchies(Set<String> processedEntityNames) {
		ModelProcessor.bindModel( rootAnnotationBindingContext, processedEntityNames );
	}

	@Override
	public void postProcessEntityHierarchies() {

	}

	@Override
	public void processResultSetMappings() {
		// Simply registers a SecondPass to process the result-set-mappings later, since they rely
		// on entities having already been bound
		ResultSetMappingProcessor.bind( rootAnnotationBindingContext );
	}

	@Override
	public void finishUp() {

	}
}
