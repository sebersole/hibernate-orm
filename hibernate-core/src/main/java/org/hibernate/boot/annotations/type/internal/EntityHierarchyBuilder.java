/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.type.internal;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.boot.annotations.AnnotationSourceLogging;
import org.hibernate.boot.annotations.source.spi.AnnotationProcessingContext;
import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.ManagedClass;
import org.hibernate.boot.annotations.source.spi.ManagedClassRegistry;
import org.hibernate.boot.annotations.spi.RootAnnotationBindingContext;
import org.hibernate.boot.annotations.type.spi.EntityHierarchy;
import org.hibernate.boot.annotations.type.spi.IdentifiableTypeMetadata;
import org.hibernate.internal.util.collections.CollectionHelper;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;

/**
 * Builds {@link EntityHierarchy} references from
 * {@linkplain ManagedClass managed classes}
 *
 * @author Steve Ebersole
 */
public class EntityHierarchyBuilder {
	/**
	 * Pre-processes the annotated entities from the index and create a set of entity hierarchies which can be bound
	 * to the metamodel.
	 *
	 * @param rootBindingContext The binding context, giving access to needed services and information
	 *
	 * @return a set of {@code EntityHierarchySource} instances.
	 */
	public static Set<EntityHierarchy> createEntityHierarchies(RootAnnotationBindingContext rootBindingContext) {
		return new EntityHierarchyBuilder( rootBindingContext ).process();
	}

	private final RootAnnotationBindingContext rootBindingContext;
	private final Set<ManagedClass> allKnownMappedSuperclassTypes = new HashSet<>();

	public EntityHierarchyBuilder(RootAnnotationBindingContext rootBindingContext) {
		this.rootBindingContext = rootBindingContext;
	}

	private Set<EntityHierarchy> process() {
		final Set<ManagedClass> rootEntityManagedClasses = collectRootEntityTypes();
		final Set<EntityHierarchy> hierarchies = CollectionHelper.setOfSize( rootEntityManagedClasses.size() );

		rootEntityManagedClasses.forEach( (rootEntityManagedClass) -> {
			final AccessType defaultAccessType = determineDefaultAccessTypeForHierarchy( rootEntityManagedClass );
			final EntityTypeMetadataImpl rootEntityTypeMetadata = new EntityTypeMetadataImpl( rootEntityManagedClass, defaultAccessType, rootBindingContext );
			hierarchies.add( new EntityHierarchyImpl( rootEntityTypeMetadata, rootBindingContext ) );
		} );

		warnAboutUnusedMappedSuperclasses( hierarchies );

		return hierarchies;
	}

	private void warnAboutUnusedMappedSuperclasses(Set<EntityHierarchy> hierarchies) {
		if ( !AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED ) {
			return;
		}

		for ( EntityHierarchy hierarchy : hierarchies ) {
			walkUp( hierarchy.getRoot() );
			walkDown( hierarchy.getRoot() );
		}

		// At this point, anything left in `allKnownMappedSuperclassTypes` is unused...
		for ( ManagedClass unusedType : allKnownMappedSuperclassTypes ) {
			AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER.debugf(
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

	private AccessType determineDefaultAccessTypeForHierarchy(ManagedClass rootEntityType) {
		ManagedClass current = rootEntityType;
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

		// todo (annotation-source) : what's best here?
		return null;
	}

	private AnnotationTarget determineIdMember(ManagedClass current) {
		final AnnotationUsage<Id> idAnnotation = current.getAnnotation( JpaAnnotations.ID );
		if ( idAnnotation != null ) {
			return idAnnotation.getAnnotationTarget();
		}

		final AnnotationUsage<EmbeddedId> embeddedIdAnnotation = current.getAnnotation( JpaAnnotations.EMBEDDED_ID );
		if ( embeddedIdAnnotation != null ) {
			return embeddedIdAnnotation.getAnnotationTarget();
		}

		return null;
	}

	private Set<ManagedClass> collectRootEntityTypes() {
		final Set<ManagedClass> collectedTypes = new HashSet<>();

		// todo (annotation-source) : have this handle other types such as converters, etc

		final AnnotationProcessingContext processingContext = rootBindingContext.getAnnotationProcessingContext();
		final ManagedClassRegistry managedClassRegistry = processingContext.getManagedClassRegistry();
		managedClassRegistry.forEachManagedClass( (managedType) -> {
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

	private boolean isRoot(ManagedClass classInfo) {
		// perform a series of opt-out checks against the super-type hierarchy

		// an entity is considered a root of the hierarchy if:
		// 		1) it has no super-types
		//		2) its super types contain no entities (MappedSuperclasses are allowed)

		if ( classInfo.getSuperType() == null ) {
			return true;
		}

		ManagedClass current = classInfo.getSuperType();
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
}
