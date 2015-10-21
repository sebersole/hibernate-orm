/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.persistence.AccessType;

import org.hibernate.boot.MappingException;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.SourceType;
import org.hibernate.boot.model.source.internal.annotations.impl.EntityHierarchySourceImpl;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.IdentifiableTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.MappedSuperclassTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.RootEntityTypeMetadata;
import org.hibernate.boot.model.source.spi.InheritanceType;
import org.hibernate.internal.util.collections.CollectionHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

/**
 * Given a Jandex annotation index, processes all classes with JPA relevant
 * annotations and builds a more-easily consumed forms of them as a "hierarchy"
 * representation.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 * @author Strong Liu
 */
public class EntityHierarchyBuilder {
	private static final Logger LOG = Logger.getLogger( EntityHierarchyBuilder.class );

	private final RootAnnotationBindingContext bindingContext;

	private final Set<DotName> allKnownMappedSuperclassClassNames = new HashSet<DotName>();
	private Map<ClassInfo, Map<DotName, List<AnnotationInstance>>> classAnnotationMapByClass
			= new HashMap<ClassInfo, Map<DotName, List<AnnotationInstance>>>(  );

	/**
	 * Pre-processes the annotated entities from the index and create a set of entity hierarchies which can be bound
	 * to the metamodel.
	 *
	 * @param bindingContext The binding context, giving access to needed services and information
	 *
	 * @return a set of {@code EntityHierarchySource} instances.
	 */
	public static Set<EntityHierarchySourceImpl> createEntityHierarchies(RootAnnotationBindingContext bindingContext) {
		return new EntityHierarchyBuilder( bindingContext ).process();
	}

	/**
	 * Constructs a EntityHierarchyBuilder.  While all calls flow into this class statically via
	 * {@link #createEntityHierarchies}, internally each call to that method creates an instance
	 * used to hold instance state representing that parse.
	 *
	 * @param bindingContext Access to needed services and information
	 */
	private EntityHierarchyBuilder(RootAnnotationBindingContext bindingContext) {
		this.bindingContext = bindingContext;
	}


	/**
	 * Pre-processes the annotated entities from the index and create a set of entity hierarchies which can be bound
	 * to the metamodel.
	 *
	 * @return a set of {@code EntityHierarchySource} instances.
	 */
	public Set<EntityHierarchySourceImpl> process() {
		final Set<ClassInfo> rootEntityDescriptors = findHierarchyRootDescriptors();
		final Set<RootEntityTypeMetadata> roots = new HashSet<RootEntityTypeMetadata>();

		final Set<EntityHierarchySourceImpl> hierarchies = new TreeSet<EntityHierarchySourceImpl>( RootEntityNameComparator.INSTANCE );

		for ( ClassInfo rootDescriptor : rootEntityDescriptors ) {
			final AccessType defaultAccessType = determineDefaultAccessTypeForHierarchy( rootDescriptor );
			final RootEntityTypeMetadata root = new RootEntityTypeMetadata( rootDescriptor, defaultAccessType, bindingContext );
			roots.add( root );
			final EntityHierarchySourceImpl hierarchy = new EntityHierarchySourceImpl( root, determineInheritanceType( root ) );
			hierarchies.add( hierarchy );
		}

		// At this point we have built all EntityClass and MappedSuperclass instances.
		// All entities that are considered a root are grouped in the 'roots' collection
		// Additionally we know of any unprocessed MappedSuperclass classes via
		//		'allKnownMappedSuperclassClassNames' - 'processedMappedSuperclassClassNames'

		warnAboutUnusedMappedSuperclasses( roots );

		return hierarchies;
	}

	private static class RootEntityNameComparator implements Comparator<EntityHierarchySourceImpl> {
		/**
		 * Singleton access
		 */
		public static final RootEntityNameComparator INSTANCE = new RootEntityNameComparator();

		@Override
		public int compare(EntityHierarchySourceImpl o1, EntityHierarchySourceImpl o2) {
			return o1.getRoot().getEntityName().compareTo( o2.getRoot().getEntityName() );
		}
	}

	/**
	 * Collect ClassInfo for all "root entities"
	 * <p/>
	 * At the same time, populates allKnownMappedSuperclassClassNames based on all
	 * encountered MappedSuperclass descriptors.
	 *
	 * @return JavaTypeDescriptor for all @Entity and @MappedSuperclass classes.
	 */
	private Set<ClassInfo> findHierarchyRootDescriptors() {
		final Set<ClassInfo> collectedDescriptors = new HashSet<ClassInfo>();

		for ( ClassInfo classInfo : bindingContext.getJandexIndex().getKnownClasses() ) {
			final Map<DotName,AnnotationInstance> classAnnotationMap = resolveClassAnnotationMap( classInfo );

			if ( classAnnotationMap.containsKey( JpaDotNames.MAPPED_SUPERCLASS ) ) {
				allKnownMappedSuperclassClassNames.add( classInfo.name() );
				continue;
			}

			if ( !classAnnotationMap.containsKey( JpaDotNames.ENTITY ) ) {
				continue;
			}

			if ( isRoot( classInfo ) ) {
				collectedDescriptors.add( classInfo );
			}
		}

		return collectedDescriptors;
	}

	private Map<DotName, AnnotationInstance> resolveClassAnnotationMap(ClassInfo classInfo) {
		return bindingContext.getTypeAnnotationInstances( classInfo.name() );
	}

	private boolean isRoot(ClassInfo classInfo) {
		// perform a series of opt-out checks against the super-type hierarchy

		// an entity is considered a root of the hierarchy if:
		// 		1) it has no super-types
		//		2) its super types contain no entities (MappedSuperclasses are allowed)

		if ( classInfo.superName() == null ) {
			return true;
		}

		ClassInfo current = bindingContext.getJandexIndex().getClassByName( classInfo.superName() );
		while ( current != null ) {
			final Map<DotName, AnnotationInstance> classAnnotationMap = resolveClassAnnotationMap( current );
			if ( classAnnotationMap.containsKey( JpaDotNames.ENTITY ) ) {
				return false;
			}

			if ( current.superName() == null ) {
				break;
			}

			current = bindingContext.getJandexIndex().getClassByName( current.superName() );
		}

		// if we hit no opt-outs we have a root
		return true;
	}

	private AccessType determineDefaultAccessTypeForHierarchy(ClassInfo root) {
		ClassInfo current = root;
		while ( current != null ) {
			final AnnotationInstance accessAnnotationInstance = bindingContext.getTypeAnnotationInstances( current.name() ).get(
					JpaDotNames.ACCESS
			);
			if ( accessAnnotationInstance != null ) {
				return AccessType.valueOf( accessAnnotationInstance.value().asEnum() );
			}

			final Collection<AnnotationInstance> embeddedIdAnnotations = current.annotations().get(
					JpaDotNames.EMBEDDED_ID
			);
			if ( CollectionHelper.isNotEmpty( embeddedIdAnnotations ) ) {
				return determineAccessTypeByIdPlacement( current, embeddedIdAnnotations );
			}

			final Collection<AnnotationInstance> idAnnotations = current.annotations().get(
					JpaDotNames.ID
			);
			if ( CollectionHelper.isNotEmpty( idAnnotations ) ) {
				return determineAccessTypeByIdPlacement( current, idAnnotations );
			}

			if ( current.superName() != null ) {
				current = bindingContext.getJandexIndex().getClassByName( current.superName() );
			}
			else {
				current = null;
			}
		}

		throw makeMappingException(
				"Unable to locate identifier attribute for class hierarchy to determine default AccessType",
				root
		);
	}

	private static MappingException makeMappingException(String message, ClassInfo classInfo) {
		return new MappingException(
				message,
				new Origin( SourceType.ANNOTATION, classInfo.name().toString() )
		);
	}

	private static AccessType determineAccessTypeByIdPlacement(
			ClassInfo descriptor,
			Collection<AnnotationInstance> idAnnotations) {
		AccessType accessType = null;
		for ( AnnotationInstance annotation : idAnnotations ) {
			AccessType tmpAccessType;
			if ( annotation.target() instanceof FieldInfo ) {
				tmpAccessType = AccessType.FIELD;
			}
			else if ( annotation.target() instanceof MethodInfo ) {
				tmpAccessType = AccessType.PROPERTY;
			}
			else {
				throw makeMappingException(
						"Invalid placement of @" + annotation.name().toString() + " annotation.  Target was " +
								annotation.target() + "; expecting field or method",
						descriptor
				);
			}

			if ( accessType == null ) {
				accessType = tmpAccessType;
			}
			else {
				if ( !accessType.equals( tmpAccessType ) ) {
					throw makeMappingException(
							"Inconsistent placement of @" + annotation.name().toString() + " annotation on class",
							descriptor
					);
				}
			}
		}
		return accessType;
	}

	private InheritanceType determineInheritanceType(EntityTypeMetadata root) {
		if ( root.getSubclasses().isEmpty() ) {
			// I make an assumption here that *any* subclasses are a indicator of
			// "persistent inheritance" but that is not strictly true.  Really its any
			// subclasses that are Entity (MappedSuperclass does not count).
			return InheritanceType.NO_INHERITANCE;
		}

		InheritanceType inheritanceType = root.getLocallyDefinedInheritanceType();
		if ( inheritanceType == null ) {
			// if we have more than one entity class the default is DISCRIMINATED/SINGLE_TABLE
			inheritanceType = InheritanceType.DISCRIMINATED;
		}

		// Validate that there is no @Inheritance annotation further down the hierarchy
		ensureNoInheritanceAnnotationsOnSubclasses( root );

		return inheritanceType;
	}

	private void ensureNoInheritanceAnnotationsOnSubclasses(IdentifiableTypeMetadata clazz) {
		for ( ManagedTypeMetadata subclass : clazz.getSubclasses() ) {
			if ( subclass.getLocalBindingContext().getTypeAnnotationInstances( subclass.getClassInfo().name() ).containsKey( JpaDotNames.INHERITANCE ) ) {
				LOG.warnf(
						"@javax.persistence.Inheritance was specified on non-root entity [%s]; ignoring...",
						clazz.getName()
				);
			}
			ensureNoInheritanceAnnotationsOnSubclasses( (IdentifiableTypeMetadata) subclass );
		}
	}

	private void warnAboutUnusedMappedSuperclasses(Set<RootEntityTypeMetadata> roots) {
		for ( RootEntityTypeMetadata root : roots ) {
			walkUp( root );
			walkDown( root );
		}

		// At this point, any left in the allKnownMappedSuperclassClassNames
		// collection are unused...
		for ( DotName mappedSuperclassName : allKnownMappedSuperclassClassNames ) {
			// todo : i18n log message?
			LOG.debugf(
					"Encountered MappedSuperclass [%s] which was unused in any entity hierarchies",
					mappedSuperclassName
			);
		}

		allKnownMappedSuperclassClassNames.clear();
	}

	private void walkUp(IdentifiableTypeMetadata type) {
		if ( type == null ) {
			return;
		}

		if ( MappedSuperclassTypeMetadata.class.isInstance( type ) ) {
			allKnownMappedSuperclassClassNames.remove( type.getClassInfo().name() );
		}

		walkUp( type.getSuperType() );
	}

	private void walkDown(ManagedTypeMetadata type) {
		for ( ManagedTypeMetadata sub : type.getSubclasses() ) {
			if ( MappedSuperclassTypeMetadata.class.isInstance( sub ) ) {
				allKnownMappedSuperclassClassNames.remove( sub.getClassInfo().name() );
			}

			walkDown( sub );
		}
	}
}

