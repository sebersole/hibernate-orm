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
import org.hibernate.boot.model.annotations.internal.AnnotationProcessingContextImpl;
import org.hibernate.boot.model.annotations.spi.AnnotationUsage;
import org.hibernate.boot.model.annotations.spi.JpaAnnotations;
import org.hibernate.boot.model.source.annotations.internal.AttributeSourceImpl;
import org.hibernate.boot.model.source.annotations.internal.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.annotations.spi.AttributeSource;

import org.hibernate.testing.boot.MetadataBuildingContextTestingImpl;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class CommonAnnotationSmokeTests {
	@Test
	void testBasic(ServiceRegistryScope scope) {
		final AnnotationProcessingContextImpl annotationProcessingContext = new AnnotationProcessingContextImpl();
		final MetadataBuildingContextTestingImpl baseContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final RootAnnotationBindingContext rootContext = new RootAnnotationBindingContext( annotationProcessingContext, baseContext );

		final JavaReflectionManager hcannReflectionManager = new JavaReflectionManager();
		final XClass xClass = hcannReflectionManager.toXClass( SimpleColumnEntity.class );
		final List<XProperty> properties = xClass.getDeclaredProperties( "field" );
		final XProperty name = extractProperty( properties, "name" );
		final AttributeSourceImpl nameAttributeSource = makeAttributeSource( name, rootContext );
		verifyBaseValues( nameAttributeSource );
	}

	private AttributeSourceImpl makeAttributeSource(XProperty name, RootAnnotationBindingContext rootContext) {
		final AttributeSourceImpl nameAttributeSource = new AttributeSourceImpl( "name", rootContext );
		nameAttributeSource.apply( name.getAnnotations() );
		return nameAttributeSource;
	}

	private XProperty extractProperty(List<XProperty> properties, String name) {
		for ( int i = 0; i < properties.size(); i++ ) {
			if ( name.equals( properties.get( i ).getName() ) ) {
				return properties.get( i );
			}
		}

		throw new RuntimeException();
	}

	private void verifyBaseValues(AttributeSource nameAttributeSource) {
		final AnnotationUsage<Column> column = nameAttributeSource.getUsage( JpaAnnotations.COLUMN );
		assertThat( column.getAttributeValue( "name" ).getStringValue() ).isEqualTo( "description" );
		assertThat( column.getAttributeValue( "table" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "nullable" ).getBooleanValue() ).isFalse();
		assertThat( column.getAttributeValue( "unique" ).getBooleanValue() ).isTrue();
		assertThat( column.getAttributeValue( "insertable" ).getBooleanValue() ).isFalse();
		assertThat( column.getAttributeValue( "updatable" ).getBooleanValue() ).isTrue();
	}

}
