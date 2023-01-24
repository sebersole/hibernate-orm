/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

import org.hibernate.boot.annotations.model.internal.AbstractOverrideDefinition;
import org.hibernate.boot.annotations.model.internal.ColumnMetadataImpl;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.compare.EqualsHelper;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;

/**
 * Contains the information about a single {@link jakarta.persistence.AttributeOverride}.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class AttributeOverrideMetadata extends AbstractOverrideDefinition {
	private final ColumnMetadata column;
	private final AnnotationUsage<Column> columnAnnotation;

	public AttributeOverrideMetadata(
			String prefix,
			AnnotationUsage<AttributeOverride> attributeOverrideAnnotation,
			AnnotationProcessingContext processingContext) {
		super( prefix, attributeOverrideAnnotation, processingContext );

		this.columnAnnotation = attributeOverrideAnnotation.getAttributeValue( "column" ).getValue();
		this.column = new ColumnMetadataImpl( columnAnnotation );
	}

	@Override
	public void apply(AttributeMetadata persistentAttribute) {
		throw new UnsupportedOperationException( "Not yet implemented" );
//		int columnSize = persistentAttribute.getColumnValues().size();
//		switch ( columnSize ){
//			case 0:
//				persistentAttribute.getColumnValues().add( column );
//				break;
//			case 1:
//				persistentAttribute.getColumnValues().get( 0 ).applyColumnValues( columnAnnotation );
//				break;
//			default:
//				//TODO throw exception??
//		}
	}

	@Override
	protected AnnotationDescriptor<AttributeOverride> getTargetAnnotation() {
		return JpaAnnotations.ATTRIBUTE_OVERRIDE;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof AttributeOverrideMetadata ) ) {
			return false;
		}
		if ( !super.equals( o ) ) {
			return false;
		}

		final AttributeOverrideMetadata that = (AttributeOverrideMetadata) o;
		return EqualsHelper.equals( column, that.column );
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + ( column != null ? column.hashCode() : 0 );
		result = 31 * result + ( columnAnnotation != null ? columnAnnotation.hashCode() : 0 );
		return result;
	}
}


