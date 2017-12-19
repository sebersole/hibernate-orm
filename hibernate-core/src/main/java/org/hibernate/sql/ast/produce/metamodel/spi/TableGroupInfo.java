/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.spi;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.TableGroupProducer;

/**
 * Access to "source" information about a TableGroup to be built - a set of
 * parameters indicating information to encode into the TableGroup.
 *
 * This information varies depending on the particular source (HQL, loading, etc).
 *
 * Intended use is as a "parameter object" for {@link TableGroupProducer}
 * methods.
 *
 * @author Steve Ebersole
 *
 * @apiNote The point of this contract is to isolate the different sources
 * of SQL AST trees (HQL, NativeQuery, LoadPlan), especially around producing
 * TableGroups and NavigableReferences ({@link NavigableReferenceInfo}).
 */
public interface TableGroupInfo {

	/**
	 * The unique identifier (uid) for the table group generated by Hibernate.
	 */
	String getUniqueIdentifier();

	/**
	 * Access to the "identification variable" (alias) as defined in the source.
	 * Should never be {@code null}; if a particular source reference defines no
	 * identification variable the implementor should use a generated one.
	 */
	String getIdentificationVariable();

	void setIdentificationVariable(String identificationVariable);

	/**
	 * The specific entity subclass to be used (for filtering).
	 */
	EntityDescriptor getIntrinsicSubclassEntityMetadata();
}
