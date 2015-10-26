/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.spi;

import java.util.List;

import org.hibernate.boot.model.source.internal.annotations.ForeignKeyInformation;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;

/**
 * Additional contract for things which describe foreign keys.
 *
 * @author Steve Ebersole
 */
public interface ForeignKeyContributingSource {
	/**
	 * Retrieve foreign-key information explicitly provided by the user.
	 *
	 * @return
	 */
	ForeignKeyInformation getForeignKeyInformation();

	// todo : really should move isCascadeDeleteEnabled into ForeignKeyInformation as well

	/**
	 * Is "cascade delete" enabled for the foreign key? In other words, if a record in the parent (referenced)
	 * table is deleted, should the corresponding records in the child table automatically be deleted?
	 *
	 * @return true, if the cascade delete is enabled; false, otherwise.
	 */
	boolean isCascadeDeleteEnabled();

	/**
	 * Retrieve the delegate for resolving foreign key target columns.  This corresponds directly to
	 * HBM {@code <property-ref/>} and JPA {@link javax.persistence.JoinColumn} mappings.
	 * <p/>
	 * By default foreign keys target the primary key of the targeted table.  {@code <property-ref/>} and
	 * {@link javax.persistence.JoinColumn} mappings represents ways to instead target non-PK columns.  Implementers
	 * should return {@code null} to indicate targeting primary key columns.
	 *
	 * @return The delegate, or {@code null}
	 */
	JoinColumnResolutionDelegate getForeignKeyTargetColumnResolutionDelegate();

	/**
	 * By default foreign keys target the columns defined as the primary key of the targeted table.  This contract
	 * helps account for cases where other columns should be targeted instead.
	 */
	interface JoinColumnResolutionDelegate {
		/**
		 * Resolve the (other, non-PK) columns which should targeted by the foreign key.
		 *
		 * @param context The context for resolving those columns.
		 *
		 * @return The resolved target columns.
		 */
		List<? extends Selectable> getJoinColumns(JoinColumnResolutionContext context);

		Table getReferencedTable(JoinColumnResolutionContext context);

		/**
		 * Retrieves the explicitly named attribute that maps to the non-PK foreign-key target columns.
		 *
		 * @return The explicitly named referenced attribute, or {@code null}.  This most likely always {@code null}
		 * 		from annotations cases.
		 */
		String getReferencedAttributeName();
	}

	/**
	 * Means to allow the {@link JoinColumnResolutionDelegate} access to the relational values it needs.
	 */
	interface JoinColumnResolutionContext {
		/**
		 * Given an attribute name, resolve the columns.  This is used in the HBM {@code property-ref/>} case.
		 *
		 * @param attributeName The name of the referenced property.
		 *
		 * @return The corresponding referenced columns
		 */
		List<? extends Selectable> resolveRelationalValuesForAttribute(String attributeName);

		Table resolveTableForAttribute(String attributeName);

		/**
		 * Resolve a column reference given the logical names of both the table and the column.  Used in the
		 * {@link javax.persistence.JoinColumn} case
		 *
		 * @param logicalColumnName The logical column name.
		 * @param logicalTableName The logical table name.
		 * @param logicalSchemaName The logical schema name.
		 * @param logicalCatalogName The logical catalog name.
		 *
		 * @return The column.
		 */
		Column resolveColumn(
				String logicalColumnName,
				String logicalTableName,
				String logicalSchemaName,
				String logicalCatalogName);

		/**
		 * Resolve a table reference given the logical names of the table and the column.  Used in the
		 * {@link javax.persistence.JoinColumn} case
		 *
		 * @param logicalTableName The logical table name.
		 * @param logicalSchemaName The logical schema name.
		 * @param logicalCatalogName The logical catalog name.
		 *
		 * @return The column.
		 */
		Table resolveTable(
				String logicalTableName,
				String logicalSchemaName,
				String logicalCatalogName);
	}
}
