/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.spi.TableSource;
import org.hibernate.internal.util.compare.EqualsHelper;

import org.jboss.jandex.AnnotationInstance;

/**
 * @author Steve Ebersole
 * @author Hardy Ferentschik
 */
public class TableSourceImpl implements TableSource {
	private final String schema;
	private final String catalog;
	private final String tableName;
	private final String rowId;

	static TableSourceImpl build(AnnotationInstance tableAnnotation, EntityBindingContext bindingContext) {
		// NOTE : ROWID currently not supported outside case of entity primary table
		return build( tableAnnotation, null, bindingContext );
	}

	static TableSourceImpl build(AnnotationInstance tableAnnotation, String rowId, EntityBindingContext bindingContext) {
		if ( tableAnnotation == null ) {
			return new TableSourceImpl( null, null, null, rowId );
		}

		return new TableSourceImpl(
				bindingContext.getTypedValueExtractor( String.class ).extract(
						tableAnnotation,
						"schema"
				),
				bindingContext.getTypedValueExtractor( String.class ).extract(
						tableAnnotation,
						"catalog"
				),
				bindingContext.getTypedValueExtractor( String.class ).extract(
						tableAnnotation,
						"name"
				),
				rowId
		);
	}

	private TableSourceImpl(String catalog, String schema, String tableName, String rowId) {
		this.catalog = catalog;
		this.schema = schema;
		this.tableName = tableName;
		this.rowId = rowId;
	}

	@Override
	public String getExplicitCatalogName() {
		return catalog;
	}

	@Override
	public String getExplicitSchemaName() {
		return schema;
	}

	public String getExplicitTableName() {
		return tableName;
	}

	public String getRowId() {
		return rowId;
	}

	@Override
	public String getComment() {
		return null;
	}

	@Override
	public String getCheckConstraint() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		final TableSourceImpl that = ( TableSourceImpl ) o;
		return EqualsHelper.equals( this.catalog, that.catalog )
				&& EqualsHelper.equals( this.schema, that.schema )
				&& EqualsHelper.equals( this.tableName, that.tableName );
	}

	@Override
	public int hashCode() {
		int result = schema != null ? schema.hashCode() : 0;
		result = 31 * result + ( catalog != null ? catalog.hashCode() : 0 );
		result = 31 * result + ( tableName != null ? tableName.hashCode() : 0 );
		return result;
	}
}


