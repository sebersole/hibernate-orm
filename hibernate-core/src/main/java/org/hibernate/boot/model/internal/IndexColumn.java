/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.internal;

import java.util.Map;

import org.hibernate.annotations.ListIndexBase;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.PropertyData;
import org.hibernate.mapping.Join;
import org.hibernate.models.spi.AnnotationUsage;

import jakarta.persistence.OrderColumn;

import static org.hibernate.internal.util.StringHelper.nullIfEmpty;

/**
 * An {@link jakarta.persistence.OrderColumn} annotation
 *
 * @author inger
 */
public class IndexColumn extends AnnotatedColumn {
	private int base;

	public IndexColumn() {
		setLength( 0L );
		setPrecision( 0 );
		setScale( 0 );
	}

	public static IndexColumn fromAnnotations(
			AnnotationUsage<OrderColumn> orderColumn,
			AnnotationUsage<org.hibernate.annotations.IndexColumn> indexColumn,
			AnnotationUsage<ListIndexBase> listIndexBase,
			PropertyHolder propertyHolder,
			PropertyData inferredData,
			Map<String, Join> secondaryTables,
			MetadataBuildingContext context) {
		final IndexColumn column;
		if ( orderColumn != null ) {
			column = buildColumnFromOrderColumn( orderColumn, propertyHolder, inferredData, secondaryTables, context );
		}
		else if ( indexColumn != null ) {
			column = buildColumnFromIndexColumn( indexColumn, propertyHolder, inferredData, context );
			column.setBase( indexColumn.getInteger( "base" ) );
		}
		else {
			column = new IndexColumn();
			column.setLogicalColumnName( inferredData.getPropertyName() + "_ORDER" ); //JPA default name
			column.setImplicit( true );
//			column.setContext( context );
//			column.setPropertyHolder( propertyHolder );
			createParent( propertyHolder, secondaryTables, column, context );
			column.bind();
		}

		if ( listIndexBase != null ) {
			column.setBase( listIndexBase.getInteger( "value" ) );
		}

		return column;
	}

	private static void createParent(
			PropertyHolder propertyHolder,
			Map<String,Join> secondaryTables,
			IndexColumn column,
			MetadataBuildingContext context) {
		final AnnotatedColumns parent = new AnnotatedColumns();
		parent.setPropertyHolder( propertyHolder );
		parent.setJoins( secondaryTables );
		parent.setBuildingContext( context );
		column.setParent( parent );
	}

	public int getBase() {
		return base;
	}

	public void setBase(int base) {
		this.base = base;
	}

	/**
	 * JPA 2 {@link OrderColumn @OrderColumn} processing.
	 *
	 * @param orderColumn The OrderColumn annotation instance
	 * @param propertyHolder Information about the property
	 * @param inferredData Yeah, right.  Uh...
	 * @param secondaryTables Any secondary tables available.
	 *
	 * @return The index column
	 */
	public static IndexColumn buildColumnFromOrderColumn(
			AnnotationUsage<OrderColumn> orderColumn,
			PropertyHolder propertyHolder,
			PropertyData inferredData,
			Map<String, Join> secondaryTables,
			MetadataBuildingContext context) {
		if ( orderColumn != null ) {
			final String sqlType = nullIfEmpty( orderColumn.getString( "columnDefinition" ) );
			final String explicitName = orderColumn.getString( "name" );
			final String name = explicitName.isEmpty()
					? inferredData.getPropertyName() + "_ORDER"
					: explicitName;
			final IndexColumn column = new IndexColumn();
			column.setLogicalColumnName( name );
			column.setSqlType( sqlType );
			column.setNullable( orderColumn.getBoolean( "nullable" ) );
//			column.setJoins( secondaryTables );
			column.setInsertable( orderColumn.getBoolean( "insertable" ) );
			column.setUpdatable( orderColumn.getBoolean( "updatable" ) );
//			column.setContext( context );
//			column.setPropertyHolder( propertyHolder );
			createParent( propertyHolder, secondaryTables, column, context );
			column.bind();
			return column;
		}
		else {
			final IndexColumn column = new IndexColumn();
			column.setImplicit( true );
//			column.setContext( context );
//			column.setPropertyHolder( propertyHolder );
			createParent( propertyHolder, secondaryTables, column, context );
			column.bind();
			return column;
		}
	}

	/**
	 * Legacy {@link IndexColumn @IndexColumn} processing.
	 *
	 * @param indexColumn The IndexColumn annotation instance
	 * @param propertyHolder Information about the property
	 * @param inferredData Yeah, right.  Uh...
	 *
	 * @return The index column
	 */
	public static IndexColumn buildColumnFromIndexColumn(
			AnnotationUsage<org.hibernate.annotations.IndexColumn> indexColumn,
			PropertyHolder propertyHolder,
			PropertyData inferredData,
			MetadataBuildingContext context) {
		if ( indexColumn != null ) {
			final String explicitName = indexColumn.getString( "name" );
			final String name = explicitName.isEmpty()
					? inferredData.getPropertyName()
					: explicitName;
			final String sqlType = nullIfEmpty( indexColumn.getString( "columnDefinition" ) );
			//TODO move it to a getter based system and remove the constructor
			final IndexColumn column = new IndexColumn();
			column.setLogicalColumnName( name );
			column.setSqlType( sqlType );
			column.setNullable( indexColumn.getBoolean( "nullable" ) );
			column.setBase( indexColumn.getInteger( "base" ) );
//			column.setContext( context );
//			column.setPropertyHolder( propertyHolder );
			createParent( propertyHolder, null, column, context );
			column.bind();
			return column;
		}
		else {
			final IndexColumn column = new IndexColumn();
			column.setImplicit( true );
//			column.setContext( context );
//			column.setPropertyHolder( propertyHolder );
			createParent( propertyHolder, null, column, context );
			column.bind();
			return column;
		}
	}
}
