/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.RowTransformer;
import org.hibernate.sql.results.internal.RowReaderStandardImpl;
import org.hibernate.sql.results.internal.values.JdbcValues;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultAssembler;
import org.hibernate.sql.results.spi.RowReader;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static <R> RowReader<R> createRowReader(
			ExecutionContext executionContext,
			RowTransformer<R> rowTransformer,
			JdbcValues jdbcValues) {
		final List<QueryResultAssembler> returnAssemblers = new ArrayList<>();
		final List<Initializer> initializers = new ArrayList<>();
		for ( QueryResult queryResult : jdbcValues.getResultSetMapping().getQueryResults() ) {
			queryResult.registerInitializers( initializers::add );
			returnAssemblers.add( queryResult.getResultAssembler() );
		}

		return new RowReaderStandardImpl<>(
				returnAssemblers,
				initializers,
				rowTransformer,
				executionContext.getCallback()
		);
	}
}
