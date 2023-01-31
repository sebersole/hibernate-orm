/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.model.annotations;

import java.util.Arrays;
import java.util.List;

import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.java.JavaReflectionManager;
import org.hibernate.boot.annotations.source.internal.AnnotationProcessingContextImpl;
import org.hibernate.boot.annotations.source.internal.hcann.ClassDetailsImpl;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.FieldDetails;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.MethodDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.MutableInteger;

import org.hibernate.testing.boot.MetadataBuildingContextTestingImpl;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
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
	void basicAssertions(ServiceRegistryScope scope) {
		final MetadataBuildingContextTestingImpl buildingContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final AnnotationProcessingContextImpl processingContext = new AnnotationProcessingContextImpl( buildingContext );

		final ClassDetailsImpl entityClass = buildManagedClass( processingContext );

		assertThat( entityClass.getFields() ).hasSize( 3 );
		assertThat( entityClass.getFields().stream().map( FieldDetails::getName ) )
				.containsAll( Arrays.asList( "id", "name", "name2" ) );

		assertThat( entityClass.getMethods() ).hasSize( 3 );
		assertThat( entityClass.getMethods().stream().map( MethodDetails::getName ) )
				.containsAll( Arrays.asList( "getId", "getName", "setName" ) );

		verifyNameMapping( findNamedField( entityClass.getFields(), "name" ) );
		verifyIdMapping( findNamedField( entityClass.getFields(), "id" ) );

		final AnnotationDescriptorRegistry descriptorRegistry = processingContext.getAnnotationDescriptorRegistry();
		final AnnotationUsage<CustomAnnotation> customAnnotation = entityClass.getAnnotation( descriptorRegistry.getDescriptor( CustomAnnotation.class ) );
		assertThat( customAnnotation ).isNotNull();
	}

	private ClassDetailsImpl buildManagedClass(AnnotationProcessingContext processingContext) {
		final JavaReflectionManager hcannReflectionManager = new JavaReflectionManager();
		final XClass xClass = hcannReflectionManager.toXClass( SimpleColumnEntity.class );

		return new ClassDetailsImpl( xClass, processingContext );
	}

	@Test
	void testRepeatableAnnotationHandling(ServiceRegistryScope scope) {
		final MetadataBuildingContextTestingImpl buildingContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final AnnotationProcessingContextImpl processingContext = new AnnotationProcessingContextImpl( buildingContext );

		final ClassDetailsImpl entityClass = buildManagedClass( processingContext );

		final List<AnnotationUsage<NamedQuery>> usages = entityClass.getAnnotations( JpaAnnotations.NAMED_QUERY );
		assertThat( usages ).hasSize( 2 );

		final AnnotationUsage<NamedQuery> abc = entityClass.getNamedAnnotation( JpaAnnotations.NAMED_QUERY, "abc" );
		assertThat( abc.getAttributeValue( "query" ).asString() ).isEqualTo( "select me" );

		final AnnotationUsage<NamedQuery> xyz = entityClass.getNamedAnnotation( JpaAnnotations.NAMED_QUERY, "xyz" );
		assertThat( xyz.getAttributeValue( "query" ).asString() ).isEqualTo( "select you" );

		// just to test
		final AnnotationUsage<NamedQuery> xyzReverse = entityClass.getNamedAnnotation( JpaAnnotations.NAMED_QUERY, "select you", "query" );
		assertThat( xyzReverse.getAttributeValue( "name" ).asString() ).isEqualTo( "xyz" );

		final MutableInteger expectedIndexRef = new MutableInteger();
		entityClass.forEachAnnotation( JpaAnnotations.NAMED_QUERY, (usage) -> {
			expectedIndexRef.getAndIncrement();
		} );
		assertThat( expectedIndexRef.get() ).isEqualTo( 2 );
	}

	private FieldDetails findNamedField(List<FieldDetails> fields, String name) {
		for ( FieldDetails field : fields ) {
			if ( field.getName().equals( name ) ) {
				return field;
			}
		}
		throw new RuntimeException();
	}

	private void verifyNameMapping(FieldDetails nameAttributeSource) {
		final AnnotationUsage<Column> column = nameAttributeSource.getAnnotation( JpaAnnotations.COLUMN );
		assertThat( column.getAttributeValue( "name" ).asString() ).isEqualTo( "description" );
		assertThat( column.getAttributeValue( "table" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "nullable" ).asBoolean() ).isFalse();
		assertThat( column.getAttributeValue( "unique" ).asBoolean() ).isTrue();
		assertThat( column.getAttributeValue( "insertable" ).asBoolean() ).isFalse();
		assertThat( column.getAttributeValue( "updatable" ).asBoolean() ).isTrue();
	}

	private void verifyIdMapping(FieldDetails idSource) {
		final AnnotationUsage<Column> column = idSource.getAnnotation( JpaAnnotations.COLUMN );
		assertThat( column.getAttributeValue( "name" ).asString() ).isEqualTo( "id" );
		assertThat( column.getAttributeValue( "table" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "nullable" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "unique" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "insertable" ).isDefaultValue() ).isTrue();
		assertThat( column.getAttributeValue( "updatable" ).isDefaultValue() ).isTrue();
	}

}
