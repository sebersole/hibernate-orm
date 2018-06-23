/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.spi;

import java.util.List;
import java.util.Set;

import org.hibernate.sql.ast.tree.spi.expression.ParameterSpec;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.results.spi.ResultSetMappingDescriptor;

/**
 * Access the values defining a native select query
 *
 * @author Steve Ebersole
 */
public interface NativeSelectQueryDefinition<R> {
	String getSqlString();
	List<ParameterSpec> getParameterSpecs();
	ResultSetMappingDescriptor getResultSetMapping();

	Set<String> getAffectedTableNames();

	RowTransformer<R> getRowTransformer();


	// todo (6.0) : drop support for executing callables via NativeQuery
	boolean isCallable();

}
