/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.model.source.annotations;

import java.util.Arrays;
import java.util.List;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.java.JavaReflectionManager;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.spi.AnnotationUsage;
import org.hibernate.boot.annotations.spi.JpaAnnotations;
import org.hibernate.boot.model.source.annotations.internal.hcann.ManagedClassImpl;
import org.hibernate.boot.model.source.annotations.spi.FieldSource;
import org.hibernate.boot.model.source.annotations.spi.MethodSource;
import org.hibernate.internal.util.MutableInteger;

import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import jakarta.persistence.NamedQuery;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class CommonAnnotationSmokeTests {
	@Test
	void basicAssertions() {
		final AnnotationDescriptorRegistry descriptorRegistry = new AnnotationDescriptorRegistry();

		final ManagedClassImpl entityClass = buildManagedClass( descriptorRegistry );
		assertThat( entityClass.getFields() ).hasSize( 3 );
		assertThat( entityClass.getFields().stream().map( FieldSource::getName ) )
				.containsAll( Arrays.asList( "id", "name", "name2" ) );

		assertThat( entityClass.getMethods() ).hasSize( 3 );
		assertThat( entityClass.getMethods().stream().map( MethodSource::getName ) )
				.containsAll( Arrays.asList( "getId", "getName", "setName" ) );

		verifyNameMapping( findNamedField( entityClass.getFields(), "name" ) );
		verifyIdMapping( findNamedField( entityClass.getFields(), "id" ) );

		final AnnotationUsage<CustomAnnotation> customAnnotation = entityClass.getUsage( descriptorRegistry.getDescriptor( CustomAnnotation.class ) );
		assertThat( customAnnotation ).isNotNull();
	}

	private ManagedClassImpl buildManagedClass(AnnotationDescriptorRegistry descriptorRegistry) {
		final JavaReflectionManager hcannReflectionManager = new JavaReflectionManager();
		final XClass xClass = hcannReflectionManager.toXClass( SimpleColumnEntity.class );

		return new ManagedClassImpl( xClass, descriptorRegistry );
	}

	@Test
	void testRepeatableAnnotationHandling() {
		final AnnotationDescriptorRegistry descriptorRegistry = new AnnotationDescriptorRegistry();
		final ManagedClassImpl entityClass = buildManagedClass( descriptorRegistry );

		final List<AnnotationUsage<NamedQuery>> usages = entityClass.getUsages( JpaAnnotations.NAMED_QUERY );
		assertThat( usages ).hasSize( 2 );

		final AnnotationUsage<NamedQuery> abc = entityClass.getNamedUsage( JpaAnnotations.NAMED_QUERY, "abc" );
		assertThat( abc.getAttributeValue( "query" ).getStringValue() ).isEqualTo( "select me" );

		final AnnotationUsage<NamedQuery> xyz = entityClass.getNamedUsage( JpaAnnotations.NAMED_QUERY, "xyz" );
		assertThat( xyz.getAttributeValue( "query" ).getStringValue() ).isEqualTo( "select you" );

		// just to test
		final AnnotationUsage<NamedQuery> xyzReverse = entityClass.getNamedUsage( JpaAnnotations.NAMED_QUERY, "select you", "query" );
		assertThat( xyzReverse.getAttributeValue( "name" ).getStringValue() ).isEqualTo( "xyz" );

		final MutableInteger expectedIndexRef = new MutableInteger();
		entityClass.forEachUsage( JpaAnnotations.NAMED_QUERY, (index, usage) -> {
			assertThat( index ).isEqualTo( expectedIndexRef.getAndIncrement() );

		} );
		assertThat( expectedIndexRef.get() ).isEqualTo( 2 );
	}

	private FieldSource findNamedField(List<FieldSource> fields, String name) {
		for ( FieldSource field : fields ) {
			if ( field.getName().equals( name ) ) {
				return field;
			}
		}
		throw new RuntimeException();
	}

	private void verifyNameMapping(FieldSource nameAttributeSource) {
		final AnnotationUsage<Column> column = nameAttributeSource.getUsage( JpaAnnotations.COLUMN );
		assertThat( column.getAttributeValue( "name" ).getStringValue() ).isEqualTo( "description" );
		assertThat( column.getAttributeValue( "table" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "nullable" ).getBooleanValue() ).isFalse();
		assertThat( column.getAttributeValue( "unique" ).getBooleanValue() ).isTrue();
		assertThat( column.getAttributeValue( "insertable" ).getBooleanValue() ).isFalse();
		assertThat( column.getAttributeValue( "updatable" ).getBooleanValue() ).isTrue();
	}

	private void verifyIdMapping(FieldSource idSource) {
		final AnnotationUsage<Column> column = idSource.getUsage( JpaAnnotations.COLUMN );
		assertThat( column.getAttributeValue( "name" ).getStringValue() ).isEqualTo( "id" );
		assertThat( column.getAttributeValue( "table" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "nullable" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "unique" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "insertable" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "updatable" ).isDefaultValue() ).isTrue();
	}

}
