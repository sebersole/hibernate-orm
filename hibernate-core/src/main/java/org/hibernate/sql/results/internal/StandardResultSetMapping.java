/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.List;
import java.util.Set;

import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.ResultSetMapping;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * @author Steve Ebersole
 */
public class StandardResultSetMapping implements ResultSetMapping {
	private final Set<SqlSelection> sqlSelections;
	private final List<QueryResult> queryResults;

	public StandardResultSetMapping(
			Set<SqlSelection> sqlSelections,
			List<QueryResult> queryResults) {
		this.sqlSelections = sqlSelections;
		this.queryResults = queryResults;
	}

	@Override
	public Set<SqlSelection> getSqlSelections() {
		return sqlSelections;
	}

	@Override
	public List<QueryResult> getQueryResults() {
		return queryResults;
	}
}
