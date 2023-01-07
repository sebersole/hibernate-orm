/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.model.source.annotations;

import java.util.List;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.annotations.common.reflection.java.JavaReflectionManager;
import org.hibernate.boot.model.TruthValue;
import org.hibernate.boot.model.source.annotations.internal.ColumnSourceImpl;
import org.hibernate.boot.model.source.annotations.internal.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.annotations.internal.XClassAttributeSource;
import org.hibernate.boot.model.source.annotations.spi.JpaAnnotations;

import org.hibernate.testing.boot.MetadataBuildingContextTestingImpl;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import org.assertj.core.api.AssertionsForInterfaceTypes;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class CommonAnnotationSmokeTests {
	@Test
	void testBasic(ServiceRegistryScope scope) {
		final MetadataBuildingContextTestingImpl baseContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final RootAnnotationBindingContext rootContext = new RootAnnotationBindingContext( baseContext );

		final JavaReflectionManager hcannReflectionManager = new JavaReflectionManager();
		final XClass xClass = hcannReflectionManager.toXClass( SimpleColumnEntity.class );
		final List<XProperty> properties = xClass.getDeclaredProperties( "field" );

		final XProperty name = extractProperty( properties, "name" );
		final XClassAttributeSource nameAttributeSource = new XClassAttributeSource( name, rootContext );

		final ColumnSourceImpl nameColumnSource = new ColumnSourceImpl( nameAttributeSource.getUsage( JpaAnnotations.COLUMN ) );
		verifyBaseValues( nameColumnSource );
	}

	private XProperty extractProperty(List<XProperty> properties, String name) {
		for ( int i = 0; i < properties.size(); i++ ) {
			if ( name.equals( properties.get( i ).getName() ) ) {
				return properties.get( i );
			}
		}

		throw new RuntimeException();
	}

	private void verifyBaseValues(ColumnSourceImpl columnSource) {
		assertThat( columnSource.getName() ).isEqualTo( "description" );
		assertThat( columnSource.getContainingTableName() ).isNull();
		AssertionsForInterfaceTypes.assertThat( columnSource.isNullable() ).isEqualTo( TruthValue.FALSE );
		assertThat( columnSource.isUnique() ).isTrue();
		assertThat( columnSource.isInsertable() ).isFalse();
		assertThat( columnSource.isUpdateable() ).isTrue();
	}

}
