/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.source;

import java.lang.reflect.Field;

import org.hibernate.boot.annotations.source.internal.AnnotationProcessingContextImpl;
import org.hibernate.boot.annotations.source.internal.AnnotationUsageImpl;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;

import org.hibernate.testing.boot.MetadataBuildingContextTestingImpl;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.ServiceRegistryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import org.assertj.core.api.AssertionsForClassTypes;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Steve Ebersole
 */
@ServiceRegistry
public class AnnotationUsageSmokeTests {
	@Test
	void testColumn(ServiceRegistryScope scope) throws NoSuchFieldException {
		final MetadataBuildingContextTestingImpl buildingContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final AnnotationProcessingContextImpl processingContext = new AnnotationProcessingContextImpl( buildingContext );

		final Field nameField = SimpleColumnEntity.class.getDeclaredField( "name" );
		final Column nameColumnAnn = nameField.getAnnotation( Column.class );
		final AnnotationUsageImpl<Column> nameColumnUsage = new AnnotationUsageImpl<>( nameColumnAnn, JpaAnnotations.COLUMN, null, processingContext );

		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "name" ).asString() ).isEqualTo( "description" );
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "table" ).asString() ).isEqualTo( "" );
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "nullable" ).asBoolean() ).isFalse();
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "unique" ).asBoolean() ).isTrue();
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "insertable" ).asBoolean() ).isFalse();
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "updatable" ).asBoolean() ).isTrue();
	}

	@Test
	void testMetaAnnotation(ServiceRegistryScope scope) {
		final MetadataBuildingContextTestingImpl buildingContext = new MetadataBuildingContextTestingImpl( scope.getRegistry() );
		final AnnotationProcessingContextImpl processingContext = new AnnotationProcessingContextImpl( buildingContext );
		final AnnotationDescriptorRegistry descriptorRegistry = processingContext.getAnnotationDescriptorRegistry();

		final AnnotationDescriptor<CustomAnnotation> descriptor = descriptorRegistry.getDescriptor( CustomAnnotation.class );
		final AnnotationDescriptor<CustomMetaAnnotation> metaDescriptor = descriptorRegistry.getDescriptor( CustomMetaAnnotation.class );
		assertThat( descriptor ).isNotNull();
		assertThat( metaDescriptor ).isNotNull();

		final AnnotationUsage<CustomMetaAnnotation> metaUsage = descriptor.getAnnotation( metaDescriptor );
		assertThat( metaUsage ).isNotNull();
	}

}
