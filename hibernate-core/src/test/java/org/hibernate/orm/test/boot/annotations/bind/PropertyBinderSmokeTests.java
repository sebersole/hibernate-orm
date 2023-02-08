/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.bind;

import java.util.Set;

import org.hibernate.boot.annotations.bind.internal.PropertyBinder;
import org.hibernate.boot.annotations.bind.internal.TypeBinder;
import org.hibernate.boot.annotations.model.spi.EntityHierarchy;
import org.hibernate.boot.annotations.model.spi.EntityTypeMetadata;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.RootClass;
import org.hibernate.orm.test.boot.annotations.intermediate.ModelHelper;
import org.hibernate.orm.test.boot.annotations.source.SimpleColumnEntity;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.boot.annotations.bind.internal.PropertyBinder.buildProperty;

/**
 * Simple tests of {@link PropertyBinder}
 * during development
 *
 * @author Steve Ebersole
 */
@ServiceRegistry
public class PropertyBinderSmokeTests {
	@Test
	void simpleTest(ServiceRegistryScope scope) {
		final Set<EntityHierarchy> entityHierarchies = ModelHelper.buildHierarchies( scope.getRegistry(), SimpleColumnEntity.class );
		final EntityHierarchy hierarchy = entityHierarchies.iterator().next();
		final EntityTypeMetadata entity = hierarchy.getRoot();

		final RootClass persistentClass = (RootClass) TypeBinder.buildPersistentClass( entity );
		assertThat( persistentClass.getTable() ).isNotNull();
		assertThat( persistentClass.getTable().getName() ).isEqualTo( "simple_entities" );

		entity.forEachAttribute( (index, attribute) -> {
			final Property property = buildProperty( attribute, entity, persistentClass.getTable(), persistentClass::findTable );
			final BasicValue valueMapping = (BasicValue) property.getValue();

			if ( "name".equals( property.getName() ) ) {
				assertThat( valueMapping.getColumn() ).isNotNull();
				assertThat( valueMapping.getColumn().getText() ).isEqualTo( "description" );
				assertThat( valueMapping.getTable() ).isEqualTo( persistentClass.getTable() );
			}
			else if ( "name2".equals( property.getName() ) ) {
				assertThat( valueMapping.getColumn() ).isNotNull();
				assertThat( valueMapping.getColumn().getText() ).isEqualTo( "name2" );
				assertThat( valueMapping.getTable() ).isNotSameAs( persistentClass.getTable() );
				assertThat( valueMapping.getTable().getName() ).isEqualTo( "another_table" );
			}
		} );
	}
}
