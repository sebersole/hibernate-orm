/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.model.annotations.hierarchy;

import java.util.Set;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.java.JavaReflectionManager;
import org.hibernate.boot.annotations.source.internal.AnnotationProcessingContextImpl;
import org.hibernate.boot.annotations.source.internal.hcann.ManagedClassImpl;
import org.hibernate.boot.annotations.source.spi.ManagedClass;
import org.hibernate.boot.annotations.source.spi.ManagedClassRegistry;
import org.hibernate.boot.annotations.spi.RootAnnotationBindingContext;
import org.hibernate.boot.annotations.type.internal.EntityHierarchyBuilder;
import org.hibernate.boot.annotations.type.spi.EntityHierarchy;
import org.hibernate.boot.annotations.type.spi.EntityTypeMetadata;
import org.hibernate.orm.boot.model.annotations.SimpleColumnEntity;

import org.hibernate.testing.boot.MetadataBuildingContextTestingImpl;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import static jakarta.persistence.InheritanceType.JOINED;
import static jakarta.persistence.InheritanceType.SINGLE_TABLE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple unit tests for {@link EntityHierarchy}
 *
 * @author Steve Ebersole
 */
@ServiceRegistry
public class EntityHierarchySmokeTests {
	@Test
	void testNoInheritance(ServiceRegistryScope scope) {
		final JavaReflectionManager hcannReflectionManager = new JavaReflectionManager();
		final AnnotationProcessingContextImpl processingContext = new AnnotationProcessingContextImpl();

		final XClass xClass = hcannReflectionManager.toXClass( SimpleColumnEntity.class );
		final ManagedClassImpl entityClass = new ManagedClassImpl( xClass, processingContext );

		final ManagedClassRegistry managedClassRegistry = processingContext.getManagedClassRegistry();
		final ManagedClass registered = managedClassRegistry.getManagedClass( entityClass.getName() );
		assertThat( registered ).isSameAs( entityClass );

		final MetadataBuildingContextTestingImpl rootContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final RootAnnotationBindingContext rootAnnotationContext = new RootAnnotationBindingContext( processingContext, rootContext );
		final Set<EntityHierarchy> entityHierarchies = EntityHierarchyBuilder.createEntityHierarchies( rootAnnotationContext );

		assertThat( entityHierarchies ).hasSize( 1 );

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		assertThat( hierarchy ).isNotNull();
		assertThat( hierarchy.getInheritanceType() ).isEqualTo( SINGLE_TABLE );

		final EntityTypeMetadata entityTypeMetadata = hierarchy.getRoot();
		final ManagedClass managedClass = entityTypeMetadata.getManagedClass();
		assertThat( managedClass.getClassName() ).isEqualTo( SimpleColumnEntity.class.getName() );
		assertThat( managedClass.getName() ).isEqualTo( managedClass.getClassName() );
		assertThat( entityTypeMetadata.getEntityName() ).isEqualTo( managedClass.getClassName() );
		assertThat( entityTypeMetadata.getJpaEntityName() ).isEqualTo( "SimpleColumnEntity" );

		assertThat( entityTypeMetadata.hasSubTypes() ).isFalse();
		assertThat( entityTypeMetadata.getSuperType() ).isNull();
	}

	@Test
	void testJoinedInheritance(ServiceRegistryScope scope) {
		final JavaReflectionManager hcannReflectionManager = new JavaReflectionManager();
		final AnnotationProcessingContextImpl processingContext = new AnnotationProcessingContextImpl();

		final XClass leafXClass = hcannReflectionManager.toXClass( JoinedLeaf.class );
		new ManagedClassImpl( leafXClass, processingContext );

		final ManagedClass root = processingContext
				.getManagedClassRegistry()
				.findManagedClass( JoinedRoot.class.getName() );
		assertThat( root ).isNotNull();
		assertThat( root.getClassName() ).isEqualTo( JoinedRoot.class.getName() );

		final MetadataBuildingContextTestingImpl rootContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final RootAnnotationBindingContext rootAnnotationContext = new RootAnnotationBindingContext( processingContext, rootContext );
		final Set<EntityHierarchy> entityHierarchies = EntityHierarchyBuilder.createEntityHierarchies( rootAnnotationContext );

		assertThat( entityHierarchies ).hasSize( 1 );

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		assertThat( hierarchy.getInheritanceType() ).isEqualTo( JOINED );
		assertThat( hierarchy.getRoot().getEntityName() ).isEqualTo( JoinedRoot.class.getName() );
		assertThat( hierarchy.getRoot().hasSubTypes() ).isTrue();
		final EntityTypeMetadata subType = (EntityTypeMetadata) hierarchy.getRoot().getSubTypes().iterator().next();
		assertThat( subType.getEntityName() ).isEqualTo( JoinedLeaf.class.getName() );
		assertThat( subType.getSuperType() ).isSameAs( hierarchy.getRoot() );
	}

}
