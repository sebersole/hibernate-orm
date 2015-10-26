/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

/**
 * Contains the information about a single {@link javax.persistence.AttributeOverride}. Instances of this class
 * are creating during annotation processing and then applied onto the persistence attributes.
 *
 * @author Hardy Ferentschik
 * @todo Take care of prefixes of the form 'element', 'key' and 'value'. Add another type enum to handle this. (HF)
 */
public class AttributeOverride extends AbstractOverrideDefinition{
	private final Column column;
	private final AnnotationInstance columnAnnotation;

	public AttributeOverride(
			String prefix,
			AnnotationInstance attributeOverrideAnnotation,
			EntityBindingContext bindingContext) {
		super( prefix, attributeOverrideAnnotation, bindingContext );

		this.columnAnnotation = bindingContext.getTypedValueExtractor( AnnotationInstance.class ).extract(
				attributeOverrideAnnotation,
				"column"
		);
		this.column = new Column( columnAnnotation );
	}

	@Override
	protected DotName getTargetAnnotation() {
		return JpaDotNames.ATTRIBUTE_OVERRIDE;
	}

	public Column getImpliedColumn() {
		return column;
	}

	public AnnotationInstance getOverriddenColumnInfo() {
		return columnAnnotation;
	}

	@Override
	public void apply(AbstractPersistentAttribute persistentAttribute) {
		int columnSize = persistentAttribute.getColumnValues().size();
		switch ( columnSize ){
			case 0:
				persistentAttribute.getColumnValues().add( column );
				break;
			case 1:
				persistentAttribute.getColumnValues().get( 0 ).applyColumnValues( columnAnnotation );
				break;
			default:
				//TODO throw exception??
		}
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof AttributeOverride ) ) {
			return false;
		}
		if ( !super.equals( o ) ) {
			return false;
		}

		AttributeOverride that = (AttributeOverride) o;

		if ( column != null ? !column.equals( that.column ) : that.column != null ) {
			return false;
		}
		if ( columnAnnotation != null ? !columnAnnotation.equals( that.columnAnnotation ) : that.columnAnnotation != null ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + ( column != null ? column.hashCode() : 0 );
		result = 31 * result + ( columnAnnotation != null ? columnAnnotation.hashCode() : 0 );
		return result;
	}
}


