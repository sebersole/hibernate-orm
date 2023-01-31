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

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

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
				ImplicitAccessTypeEntity.class
		);

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata entity = hierarchy.getRoot();
		assertThat( entity.getAccessType() ).isEqualTo( AccessType.FIELD );
	}

	@Test
	void testImplicitAccessTypeMappedSuper(ServiceRegistryScope scope) {
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies(
				scope.getRegistry(),
				ImplicitAccessTypeMappedSuper.class,
				ImplicitAccessTypeMappedSuperSub.class
		);

		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata root = hierarchy.getRoot();
		assertThat( root.getAccessType() ).isEqualTo( AccessType.FIELD );
		assertThat( root.getNumberOfSubTypes() ).isEqualTo( 0 );
		assertThat( root.getSuperType() ).isNotNull();
		assertThat( root.getSuperType().getAccessType() ).isEqualTo( AccessType.FIELD );

	}

	@Entity( name = "ImplicitAccessTypeEntity" )
	public static class ImplicitAccessTypeEntity {
	    @Id
	    private Integer id;
	    @Basic
		private String name;

		protected ImplicitAccessTypeEntity() {
			// for use by Hibernate
		}

		public ImplicitAccessTypeEntity(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	@MappedSuperclass
	public static class ImplicitAccessTypeMappedSuper {
		@Id
		protected Integer id;
	}

	@Entity( name = "ImplicitAccessTypeMappedSuperSub" )
	public static class ImplicitAccessTypeMappedSuperSub extends ImplicitAccessTypeMappedSuper {
	    @Basic
		private String name;

		protected ImplicitAccessTypeMappedSuperSub() {
			// for use by Hibernate
		}

		public ImplicitAccessTypeMappedSuperSub(Integer id, String name) {
			this.id = id;
			this.name = name;
		}

		public Integer getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
}
