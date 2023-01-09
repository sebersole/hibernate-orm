/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.annotations;

import java.lang.reflect.Field;

import org.hibernate.boot.model.annotations.internal.AnnotationUsageImpl;
import org.hibernate.boot.model.annotations.spi.AnnotationDescriptor;
import org.hibernate.boot.model.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.annotations.spi.AnnotationUsage;
import org.hibernate.boot.model.annotations.spi.JpaAnnotations;
import org.hibernate.orm.boot.model.source.annotations.CustomAnnotation;
import org.hibernate.orm.boot.model.source.annotations.CustomMetaAnnotation;
import org.hibernate.orm.boot.model.source.annotations.SimpleColumnEntity;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Column;
import org.assertj.core.api.AssertionsForClassTypes;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author Steve Ebersole
 */
public class AnnotationUsageSmokeTests {
	@Test
	void testColumn() throws NoSuchFieldException {
		final Field nameField = SimpleColumnEntity.class.getDeclaredField( "name" );
		final Column nameColumnAnn = nameField.getAnnotation( Column.class );
		final AnnotationUsageImpl<Column> nameColumnUsage = new AnnotationUsageImpl<>( nameColumnAnn, JpaAnnotations.COLUMN, null );

		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "name" ).getStringValue() ).isEqualTo( "description" );
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "table" ).getStringValue() ).isEqualTo( "" );
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "nullable" ).getBooleanValue() ).isFalse();
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "unique" ).getBooleanValue() ).isTrue();
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "insertable" ).getBooleanValue() ).isFalse();
		AssertionsForClassTypes.assertThat( nameColumnUsage.getAttributeValue( "updatable" ).getBooleanValue() ).isTrue();
	}

	@Test
	void testMetaAnnotation() {
		final AnnotationDescriptorRegistry descriptorRegistry = new AnnotationDescriptorRegistry();
		final AnnotationDescriptor<CustomAnnotation> descriptor = descriptorRegistry.getDescriptor( CustomAnnotation.class );
		final AnnotationDescriptor<CustomMetaAnnotation> metaDescriptor = descriptorRegistry.getDescriptor( CustomMetaAnnotation.class );
		assertThat( descriptor ).isNotNull();
		assertThat( metaDescriptor ).isNotNull();

		final AnnotationUsage<CustomMetaAnnotation> metaUsage = descriptor.getUsage( metaDescriptor );
		assertThat( metaUsage ).isNotNull();
	}

}
