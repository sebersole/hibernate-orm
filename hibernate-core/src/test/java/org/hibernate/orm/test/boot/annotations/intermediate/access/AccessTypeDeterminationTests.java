/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.intermediate.access;

import java.util.Set;

import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.EntityTypeMetadata;
import org.hibernate.orm.test.boot.annotations.intermediate.ModelHelper;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.AccessType;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class AccessTypeDeterminationTests {
	@Test
	void testImplicitAccessType(ServiceRegistryScope scope) {
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies(
				scope.getRegistry(),
				SimpleImplicitEntity.class
		);

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata entity = hierarchy.getRoot();
		assertThat( entity.getAccessType() ).isEqualTo( AccessType.FIELD );
	}

	@Test
	void testExplicitAccessType(ServiceRegistryScope scope) {
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies(
				scope.getRegistry(),
				SimpleExplicitEntity.class
		);

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata entity = hierarchy.getRoot();
		assertThat( entity.getAccessType() ).isEqualTo( AccessType.FIELD );
	}

	@Test
	void testImplicitAccessTypeMappedSuper(ServiceRegistryScope scope) {
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies(
				scope.getRegistry(),
				ImplicitMappedSuper.class,
				ImplicitMappedSuperRoot.class
		);

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata root = hierarchy.getRoot();
		assertThat( root.getAccessType() ).isEqualTo( AccessType.FIELD );
		assertThat( root.getNumberOfAttributes() ).isEqualTo( 1 );
		assertThat( root.getNumberOfSubTypes() ).isEqualTo( 0 );
		assertThat( root.getSuperType() ).isNotNull();
		assertThat( root.getSuperType().getAccessType() ).isEqualTo( AccessType.FIELD );
		assertThat( root.getSuperType().getNumberOfAttributes() ).isEqualTo( 1 );
	}

	@Test
	void testExplicitAccessTypeMappedSuper(ServiceRegistryScope scope) {
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies(
				scope.getRegistry(),
				ExplicitMappedSuper.class,
				ExplicitMappedSuperRoot1.class
		);

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata root = hierarchy.getRoot();
		assertThat( root.getAccessType() ).isEqualTo( AccessType.PROPERTY );
		assertThat( root.getNumberOfAttributes() ).isEqualTo( 1 );
		assertThat( root.getNumberOfSubTypes() ).isEqualTo( 0 );
		assertThat( root.getSuperType() ).isNotNull();
		assertThat( root.getSuperType().getAccessType() ).isEqualTo( AccessType.FIELD );
		assertThat( root.getSuperType().getNumberOfAttributes() ).isEqualTo( 1 );
	}

}
