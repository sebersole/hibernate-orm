/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.boot.annotations.bind.internal.global.GlobalAnnotationProcessor;
import org.hibernate.boot.annotations.bind.xml.internal.XmlMappingProcessor;
import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.EntityTypeMetadata;
import org.hibernate.boot.annotations.model.spi.IdentifiableTypeMetadata;
import org.hibernate.boot.annotations.model.spi.ManagedTypeMetadata;
import org.hibernate.boot.annotations.source.internal.AnnotationProcessingContextImpl;
import org.hibernate.boot.annotations.source.internal.NoPackageDetailsImpl;
import org.hibernate.boot.annotations.source.internal.PackageDetailsImpl;
import org.hibernate.boot.annotations.source.internal.reflection.ClassDetailsImpl;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.PackageDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.model.convert.spi.ConverterDescriptor;
import org.hibernate.boot.model.convert.spi.ConverterRegistry;
import org.hibernate.boot.model.process.spi.ManagedResources;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.IdentifiableTypeClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;

import jakarta.persistence.AttributeConverter;

import static org.hibernate.boot.annotations.AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER;
import static org.hibernate.boot.annotations.AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED;
import static org.hibernate.boot.annotations.model.internal.EntityHierarchyBuilder.createEntityHierarchies;

/**
 * Coordinates the processing of {@linkplain ManagedResources managed-resources}
 * into Hibernate's {@linkplain org.hibernate.mapping boot model}.
 *
 * @author Steve Ebersole
 */
public class ManagedResourcesProcessor {
	private final AnnotationProcessingContext processingContext;

	// cache some frequently used references
	private final ClassDetailsRegistry classDetailsRegistry;
	private final ConverterRegistry converterRegistry;

	private final GlobalAnnotationProcessor globalAnnotationProcessor;

	/**
	 * Entry point for processing managed-resources into boot model references
	 *
	 * @param managedResources The managed-resources (entities, XML, etc) to be processed
	 * @param buildingContext Access to needed resources
	 */
	public static void bindBootModel(
			ManagedResources managedResources,
			MetadataBuildingContext buildingContext) {
		final AnnotationProcessingContextImpl processingContext = new AnnotationProcessingContextImpl( buildingContext );
		final ManagedResourcesProcessor managedResourcesProcessor = new ManagedResourcesProcessor( processingContext );

		managedResourcesProcessor.prepare( managedResources );

		final Set<EntityHierarchy> entityHierarchies = createEntityHierarchies( managedResourcesProcessor::processGlobalAnnotations, processingContext );
		final Map<EntityHierarchy,RootClass> rootClasses = managedResourcesProcessor.processHierarchies( entityHierarchies, managedResources );
		managedResourcesProcessor.processAttributes( entityHierarchies, rootClasses, managedResources );

		managedResourcesProcessor.finishUp();
	}

	private ManagedResourcesProcessor(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;

		this.classDetailsRegistry = processingContext.getClassDetailsRegistry();
		this.converterRegistry = processingContext.getMetadataBuildingContext().getMetadataCollector().getConverterRegistry();

		this.globalAnnotationProcessor = new GlobalAnnotationProcessor( processingContext );
	}

	private void prepare(ManagedResources managedResources) {
		// process any packages specified as managed-resources
		if ( CollectionHelper.isNotEmpty( managedResources.getAnnotatedPackageNames() ) ) {
			for ( String packageName : managedResources.getAnnotatedPackageNames() ) {
				processManagedPackage( packageName );
			}
		}

		// walks through the managed-resources and creates "intermediate model" references
		prepareManagedResources( managedResources );
	}

	private void processGlobalAnnotations(ManagedTypeMetadata managedTypeMetadata) {
		globalAnnotationProcessor.processGlobalAnnotation( managedTypeMetadata.getManagedClass() );
	}

	private Map<EntityHierarchy,RootClass> processHierarchies(Set<EntityHierarchy> entityHierarchies, ManagedResources managedResources) {
		if ( CollectionHelper.isEmpty( entityHierarchies ) ) {
			return Collections.emptyMap();
		}

		final HashMap<EntityHierarchy,RootClass> rootClasses = new HashMap<>();

		// Visit each discovered entity hierarchy
		for ( EntityHierarchy entityHierarchy : entityHierarchies ) {
			final EntityTypeMetadata rootEntityMetadata = entityHierarchy.getRoot();
			// this step creates the RootClass reference, as well as all the proper
			// supers and subs.  It also registers them with the metadata-collector
			final RootClass rootClass = (RootClass) TypeBinder.buildPersistentClass( rootEntityMetadata );
			rootClasses.put( entityHierarchy, rootClass );
			if ( ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED ) {
				ANNOTATION_SOURCE_LOGGER.debugf(
						"Entity hierarchy built : %s",
						rootClass.getEntityName()
				);
			}
		}
		return rootClasses;
	}

	private void processAttributes(Set<EntityHierarchy> entityHierarchies, Map<EntityHierarchy,RootClass> rootClasses, ManagedResources managedResources) {
		for ( EntityHierarchy entityHierarchy : entityHierarchies ) {
			final EntityTypeMetadata rootEntityMetadata = entityHierarchy.getRoot();
			final RootClass rootClass = rootClasses.get( entityHierarchy );
			processAttributesUp( rootEntityMetadata, rootClass );
			processAttributes( rootEntityMetadata, rootClass );
			processAttributesDown( rootEntityMetadata, rootClass );
		}
	}

	private void processAttributes(
			IdentifiableTypeMetadata identifiableTypeMetadata,
			IdentifiableTypeClass identifiableTypeMapping) {
		identifiableTypeMetadata.forEachAttribute( (index, attributeMetadata) -> {
			if ( attributeMetadata.getMember().getAnnotation( JpaAnnotations.ID ) != null
					|| attributeMetadata.getMember().getAnnotation( JpaAnnotations.EMBEDDED_ID ) != null ) {
				// for now, skip...
				return;
			}
			final Property property = PropertyBinder.buildProperty(
					attributeMetadata,
					identifiableTypeMetadata,
					identifiableTypeMapping::getImplicitTable,
					identifiableTypeMapping::findTable
			);
			identifiableTypeMapping.applyProperty( property );
		} );
	}

	private void processAttributesUp(
			IdentifiableTypeMetadata identifiableTypeMetadata,
			IdentifiableTypeClass identifiableTypeMapping) {
		final IdentifiableTypeMetadata superTypeMetadata = identifiableTypeMetadata.getSuperType();
		if ( superTypeMetadata == null ) {
			return;
		}

		final IdentifiableTypeClass superIdentifiableTypeClass = identifiableTypeMapping.getSuperType();
		assert superIdentifiableTypeClass != null;

		processAttributesUp( superTypeMetadata, superIdentifiableTypeClass );
		processAttributes( superTypeMetadata, superIdentifiableTypeClass );
	}

	private void processAttributesDown(
			IdentifiableTypeMetadata identifiableTypeMetadata,
			IdentifiableTypeClass identifiableTypeClass) {
		if ( identifiableTypeMetadata.getNumberOfSubTypes() == 0 ) {
			return;
		}

		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	private void processManagedPackage(String packageName) {
		final PackageDetails packageDetails = createManagedPackageDetails( packageName );
		globalAnnotationProcessor.processGlobalAnnotation( packageDetails );
	}

	private PackageDetails createManagedPackageDetails(String packageName) {
		final ClassLoaderService classLoaderService = processingContext.getMetadataBuildingContext()
				.getBootstrapContext()
				.getServiceRegistry()
				.getService( ClassLoaderService.class );
		final Class<?> packageInfoClass = classLoaderService.classForName( packageName + ".package-info" );

		final PackageDetails packageDetails;
		if ( packageInfoClass == null ) {
			packageDetails = new NoPackageDetailsImpl( packageName );
		}
		else {
			packageDetails = new PackageDetailsImpl( packageInfoClass, processingContext );
		}

		return packageDetails;
	}

	/**
	 * Performs a number of operations - <ol>
	 *     <li>
	 *         Iterates all known (explicit and discovered) annotated classes creating
	 *         ClassDetails references and processing "global" annotations.  This
	 *         includes additional handling for certain ClassDetail references, such as
	 *         converters.
	 *     </li>
	 *     <li>
	 *         Processes all XML mappings.  This also creates ClassDetails references,
	 *         registers converters and processes "global annotations".  See
	 *         {@link XmlMappingProcessor#processXmlMappings}
	 *     </li>
	 * </ol>>
	 */
	private void prepareManagedResources(ManagedResources managedResources) {
		final ClassDetails attributeConverterClassDetails = classDetailsRegistry.resolveManagedClass(
				AttributeConverter.class.getName(),
				() -> new ClassDetailsImpl( AttributeConverter.class, processingContext )
		);

		for ( Class<?> managedClassReference : managedResources.getAnnotatedClassReferences() ) {
			final ClassDetails classDetails = classDetailsRegistry.resolveManagedClass( managedClassReference.getName() );
			// if the Class is a converter, register it
			if ( isConverter( classDetails, attributeConverterClassDetails ) ) {
				//noinspection unchecked
				converterRegistry.addAttributeConverter( (Class<? extends AttributeConverter<?,?>>) managedClassReference );
			}
			else {
				globalAnnotationProcessor.processGlobalAnnotation( classDetails );
			}
		}

		for ( String managedClassName : managedResources.getAnnotatedClassNames() ) {
			final ClassDetails classDetails = classDetailsRegistry.resolveManagedClass( managedClassName );
			// if the Class is a converter, register it
			if ( isConverter( classDetails, attributeConverterClassDetails ) ) {
				converterRegistry.addAttributeConverter( classDetails.toJavaClass() );
			}
			else {
				globalAnnotationProcessor.processGlobalAnnotation( classDetails );
			}
		}

		if ( CollectionHelper.isNotEmpty( managedResources.getAttributeConverterDescriptors() ) ) {
			for ( ConverterDescriptor converterDescriptor : managedResources.getAttributeConverterDescriptors() ) {
				converterRegistry.addAttributeConverter( converterDescriptor );
			}
		}

		if ( CollectionHelper.isNotEmpty( managedResources.getExtraQueryImports() ) ) {
			managedResources.getExtraQueryImports().forEach( (name, target) -> {
				processingContext.getMetadataBuildingContext()
						.getMetadataCollector()
						.addImport( name, target.getName() );
			} );
		}

		// we already know all annotated classes (both listed and discovered) when we get here
		XmlMappingProcessor.processXmlMappings( managedResources.getXmlMappingBindings(), processingContext );
	}

	private static boolean isConverter(ClassDetails classDetails, ClassDetails attributeConverterClassDetails) {
		return classDetails.getAnnotation( JpaAnnotations.CONVERTER ) != null
				|| classDetails.implementsInterface( attributeConverterClassDetails );
	}

	private void finishUp() {
		// todo (annotation-source) : needed?
		// atm nothing to do
	}
}
