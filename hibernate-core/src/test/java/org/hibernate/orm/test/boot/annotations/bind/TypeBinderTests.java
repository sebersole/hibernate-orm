/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.bind;

import java.util.Set;

import org.hibernate.boot.annotations.bind.internal.TypeBinder;
import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.EntityTypeMetadata;
import org.hibernate.mapping.RootClass;
import org.hibernate.orm.test.boot.annotations.intermediate.ModelHelper;
import org.hibernate.orm.test.boot.annotations.source.SimpleColumnEntity;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.cache.spi.access.AccessType.READ_ONLY;

/**
 * Tests for {@link TypeBinder}
 *
 * @author Steve Ebersole
 */
@ServiceRegistry
public class TypeBinderTests {
	@Test
	void simpleEntityTest(ServiceRegistryScope scope) {
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies( scope.getRegistry(), SimpleColumnEntity.class );
		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata entity = hierarchy.getRoot();

		final RootClass persistentClass = (RootClass) TypeBinder.buildPersistentClass( entity );
		assertThat( persistentClass ).isNotNull();
		assertThat( persistentClass.getEntityName() ).isEqualTo( SimpleColumnEntity.class.getName() );
		assertThat( persistentClass.getJpaEntityName() ).isEqualTo( "SimpleColumnEntity" );
		assertThat( persistentClass.getClassName() ).isEqualTo( persistentClass.getEntityName() );
		assertThat( persistentClass.isCached() ).isTrue();
		assertThat( persistentClass.getCacheConcurrencyStrategy() ).isEqualToIgnoringCase( READ_ONLY.getExternalName() );
		assertThat( persistentClass.getCacheRegionName() ).isEqualTo( "custom-region" );

		assertThat( persistentClass.getTable() ).isNotNull();
		assertThat( persistentClass.getTable().getName() ).isEqualTo( "simple_entities" );
	}

	@Test
	void basicEntityTest(ServiceRegistryScope scope) {
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies( scope.getRegistry(), BasicEntity.class );
		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata entity = hierarchy.getRoot();

		final RootClass persistentClass = (RootClass) TypeBinder.buildPersistentClass( entity );
		assertThat( persistentClass ).isNotNull();
		assertThat( persistentClass.getEntityName() ).isEqualTo( BasicEntity.class.getName() );
		assertThat( persistentClass.getJpaEntityName() ).isEqualTo( BasicEntity.class.getSimpleName() );
		assertThat( persistentClass.getClassName() ).isEqualTo( persistentClass.getEntityName() );
		assertThat( persistentClass.isCached() ).isFalse();

		assertThat( persistentClass.getTable() ).isNotNull();
		assertThat( persistentClass.getTable().getName() ).isEqualTo( "BasicEntity" );
	}
}
