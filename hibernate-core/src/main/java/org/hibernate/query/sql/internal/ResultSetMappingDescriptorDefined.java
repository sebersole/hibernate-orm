/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.internal;

import java.util.List;
import java.util.Set;

import org.hibernate.sql.results.internal.StandardResultSetMapping;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.ResultSetMapping;
import org.hibernate.sql.results.spi.ResultSetMappingDescriptor;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * ResultSetMapping for handling selections for a {@link org.hibernate.query.NativeQuery}
 * which (partially) defines its result mappings.  At the very least we will need
 * to resolve the `columnAlias` to its ResultSet index.  For scalar results
 * ({@link javax.persistence.ColumnResult}) we may additionally need to resolve
 * its "type" for reading.
 *
 * Specifically needs to
 * @author Steve Ebersole
 */
public class ResultSetMappingDescriptorDefined implements ResultSetMappingDescriptor {
	private final ResultSetMapping resolvedMapping;

	public ResultSetMappingDescriptorDefined(Set<SqlSelection> selections, List<QueryResult> queryResults) {
		resolvedMapping  = new StandardResultSetMapping( selections, queryResults );
	}

	@Override
	public ResultSetMapping resolve(
			JdbcValuesMetadata jdbcResultsMetadata,
			ResolutionContext resolutionContext) {
		for ( SqlSelection sqlSelection : resolvedMapping.getSqlSelections() ) {
			sqlSelection.prepare( jdbcResultsMetadata, resolutionContext );
		}

		return resolvedMapping;
	}
}
