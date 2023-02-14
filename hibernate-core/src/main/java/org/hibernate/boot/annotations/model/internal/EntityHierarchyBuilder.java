/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.hibernate.boot.annotations.model.AccessTypeDeterminationException;
import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.IdentifiableTypeMetadata;
import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.ClassDetailsRegistry;
import org.hibernate.boot.annotations.source.spi.FieldDetails;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.MethodDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.collections.CollectionHelper;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;

import static org.hibernate.boot.annotations.AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER;
import static org.hibernate.boot.annotations.AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED;

/**
 * Builds {@link EntityHierarchy} references from
 * {@linkplain ClassDetailsRegistry#forEachManagedClass managed classes}.
 *
 * @author Steve Ebersole
 */
public class EntityHierarchyBuilder {

	/**
	 * Pre-processes the annotated entities from the index and create a set of entity hierarchies which can be bound
	 * to the metamodel.
	 *
	 * @param typeConsumer Callback for any identifiable-type metadata references
	 * @param processingContext The binding context, giving access to needed services and information
	 *
	 * @return a set of {@code EntityHierarchySource} instances.
	 */
	public static Set<EntityHierarchy> createEntityHierarchies(
			Consumer<IdentifiableTypeMetadata> typeConsumer,
			AnnotationProcessingContext processingContext) {
		return new EntityHierarchyBuilder( processingContext ).process( typeConsumer );
	}

	private final AnnotationProcessingContext processingContext;

	private final Set<ClassDetails> allKnownMappedSuperclassTypes = new HashSet<>();

	public EntityHierarchyBuilder(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;
	}

	private Set<EntityHierarchy> process(Consumer<IdentifiableTypeMetadata> typeConsumer) {
		final Set<ClassDetails> rootEntityClassDetails = collectRootEntityTypes();
		final Set<EntityHierarchy> hierarchies = CollectionHelper.setOfSize( rootEntityClassDetails.size() );

		rootEntityClassDetails.forEach( (rootEntityManagedClass) -> {
			final AccessType defaultAccessType = determineDefaultAccessTypeForHierarchy( rootEntityManagedClass );
			hierarchies.add( new EntityHierarchyImpl( rootEntityManagedClass, defaultAccessType, typeConsumer, processingContext ) );
		} );

		if ( ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED ) {
			warnAboutUnusedMappedSuperclasses( hierarchies );
		}

		return hierarchies;
	}

	private void warnAboutUnusedMappedSuperclasses(Set<EntityHierarchy> hierarchies) {
		assert ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED;

		for ( EntityHierarchy hierarchy : hierarchies ) {
			walkUp( hierarchy.getRoot() );
			walkDown( hierarchy.getRoot() );
		}

		// At this point, anything left in `allKnownMappedSuperclassTypes` is unused...
		for ( ClassDetails unusedType : allKnownMappedSuperclassTypes ) {
			ANNOTATION_SOURCE_LOGGER.debugf(
					"Encountered MappedSuperclass [%s] which was unused in any entity hierarchies",
					unusedType.getName()
			);
		}

		allKnownMappedSuperclassTypes.clear();
	}

	private void walkDown(IdentifiableTypeMetadata type) {
		for ( IdentifiableTypeMetadata subType : type.getSubTypes() ) {
			allKnownMappedSuperclassTypes.remove( type.getManagedClass() );
			walkDown( subType );
		}
	}

	private void walkUp(IdentifiableTypeMetadata type) {
		if ( type != null ) {
			allKnownMappedSuperclassTypes.remove( type.getManagedClass() );
			walkUp( type.getSuperType() );
		}
	}

	private AccessType determineDefaultAccessTypeForHierarchy(ClassDetails rootEntityType) {
		assert rootEntityType != null;

		ClassDetails current = rootEntityType;
		while ( current != null ) {
			// look for `@Access` on the class
			final AnnotationUsage<Access> accessAnnotation = current.getAnnotation( JpaAnnotations.ACCESS );
			if ( accessAnnotation != null ) {
				return accessAnnotation.getValueAttributeValue().getValue();
			}

			// look for `@Id` or `@EmbeddedId`
			final AnnotationTarget idMember = determineIdMember( current );
			if ( idMember != null ) {
				switch ( idMember.getKind() ) {
					case FIELD: {
						return AccessType.FIELD;
					}
					case METHOD: {
						return AccessType.PROPERTY;
					}
					default: {
						throw new IllegalStateException( "@Id / @EmbeddedId found on target other than field or method : " + idMember );
					}
				}
			}

			current = current.getSuperType();
		}

		// 2.3.1 Default Access Type
		//    It is an error if a default access type cannot be determined and an access type is not explicitly specified
		//    by means of annotations or the XML descriptor.

		throw new AccessTypeDeterminationException( rootEntityType );
	}

	private AnnotationTarget determineIdMember(ClassDetails current) {
		final List<MethodDetails> methods = current.getMethods();
		for ( int i = 0; i < methods.size(); i++ ) {
			final MethodDetails methodDetails = methods.get( i );
			if ( methodDetails.getAnnotation( JpaAnnotations.ID ) != null
					|| methodDetails.getAnnotation( JpaAnnotations.EMBEDDED_ID ) != null ) {
				return methodDetails;
			}
		}

		final List<FieldDetails> fields = current.getFields();
		for ( int i = 0; i < fields.size(); i++ ) {
			final FieldDetails fieldDetails = fields.get( i );
			if ( fieldDetails.getAnnotation( JpaAnnotations.ID ) != null
					|| fieldDetails.getAnnotation( JpaAnnotations.EMBEDDED_ID ) != null ) {
				return fieldDetails;
			}
		}

		return null;
	}

	private Set<ClassDetails> collectRootEntityTypes() {
		final Set<ClassDetails> collectedTypes = new HashSet<>();

		// todo (annotation-source) : have this handle other types such as converters, etc

		final ClassDetailsRegistry classDetailsRegistry = processingContext.getClassDetailsRegistry();
		classDetailsRegistry.forEachManagedClass( (managedType) -> {
			if ( managedType.getAnnotation( JpaAnnotations.MAPPED_SUPERCLASS ) != null ) {
				allKnownMappedSuperclassTypes.add( managedType );
			}
			else if ( managedType.getAnnotation( JpaAnnotations.ENTITY ) != null ) {
				if ( isRoot( managedType ) ) {
					collectedTypes.add( managedType );
				}
			}
		} );

		return collectedTypes;
	}

	private boolean isRoot(ClassDetails classInfo) {
		// perform a series of opt-out checks against the super-type hierarchy

		// an entity is considered a root of the hierarchy if:
		// 		1) it has no super-types
		//		2) its super types contain no entities (MappedSuperclasses are allowed)

		if ( classInfo.getSuperType() == null ) {
			return true;
		}

		ClassDetails current = classInfo.getSuperType();
		while (  current != null ) {
			if ( current.getAnnotation( JpaAnnotations.ENTITY ) != null ) {
				// a super type has `@Entity`, cannot be root
				return false;
			}
			current = current.getSuperType();
		}

		// if we hit no opt-outs we have a root
		return true;
	}


	/**
	 * Used in tests
	 */
	public static Set<EntityHierarchy> createEntityHierarchies(AnnotationProcessingContext processingContext) {
		return new EntityHierarchyBuilder( processingContext ).process( EntityHierarchyBuilder::ignore );
	}

	private static void ignore(IdentifiableTypeMetadata it) {}
}
