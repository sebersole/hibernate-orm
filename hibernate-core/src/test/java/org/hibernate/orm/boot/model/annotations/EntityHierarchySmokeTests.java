/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.model.annotations;

import java.util.Set;

import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.EntityTypeMetadata;
import org.hibernate.boot.annotations.source.spi.ClassDetails;

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
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies( scope.getRegistry(), SimpleColumnEntity.class );

		assertThat( entityHierarchies ).hasSize( 1 );

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		assertThat( hierarchy.getInheritanceType() ).isEqualTo( SINGLE_TABLE );

		final EntityTypeMetadata entityTypeMetadata = hierarchy.getRoot();
		final ClassDetails classDetails = entityTypeMetadata.getManagedClass();
		assertThat( classDetails.getClassName() ).isEqualTo( SimpleColumnEntity.class.getName() );
		assertThat( classDetails.getName() ).isEqualTo( classDetails.getClassName() );
		assertThat( entityTypeMetadata.getEntityName() ).isEqualTo( classDetails.getClassName() );
		assertThat( entityTypeMetadata.getJpaEntityName() ).isEqualTo( "SimpleColumnEntity" );

		assertThat( entityTypeMetadata.getSuperType() ).isNull();
		assertThat( entityTypeMetadata.hasSubTypes() ).isFalse();

		assertThat( entityTypeMetadata.getAttributes() ).hasSize( 3 );
	}

	@Test
	void testJoinedInheritance(ServiceRegistryScope scope) {
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies(
				scope.getRegistry(),
				JoinedRoot.class,
				JoinedLeaf.class
		);

		assertThat( entityHierarchies ).hasSize( 1 );

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		assertThat( hierarchy.getInheritanceType() ).isEqualTo( JOINED );

		assertThat( hierarchy.getRoot().getEntityName() ).isEqualTo( JoinedRoot.class.getName() );
		assertThat( hierarchy.getRoot().getSuperType() ).isNull();
		assertThat( hierarchy.getRoot().hasSubTypes() ).isTrue();
		final EntityTypeMetadata subType = (EntityTypeMetadata) hierarchy.getRoot().getSubTypes().iterator().next();
		assertThat( subType.getEntityName() ).isEqualTo( JoinedLeaf.class.getName() );
		assertThat( subType.getSuperType() ).isSameAs( hierarchy.getRoot() );
	}

}
