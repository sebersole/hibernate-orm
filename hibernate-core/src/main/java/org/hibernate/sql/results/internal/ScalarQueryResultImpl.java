/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.results.spi.InitializerCollector;
import org.hibernate.sql.results.spi.QueryResultAssembler;
import org.hibernate.sql.results.spi.ScalarQueryResult;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;

/**
 * @author Steve Ebersole
 */
public class ScalarQueryResultImpl implements ScalarQueryResult {
	private final String resultVariable;
	private final JdbcValueMapper jdbcValueMapper;

	private final QueryResultAssembler assembler;

	public ScalarQueryResultImpl(
			String resultVariable,
			SqlSelection sqlSelection,
			BasicValueConverter valueConverter) {
		this.resultVariable = resultVariable;
		this.jdbcValueMapper = sqlSelection.getJdbcValueMapper();

		this.assembler = new ScalarQueryResultAssembler( sqlSelection, valueConverter );
	}

	@Override
	public String getResultVariable() {
		return resultVariable;
	}

	@Override
	public BasicJavaDescriptor getJavaTypeDescriptor() {
		return jdbcValueMapper.getJavaTypeDescriptor();
	}

	@Override
	public void registerInitializers(InitializerCollector collector) {
		// nothing to do
	}

	@Override
	public QueryResultAssembler getResultAssembler() {
		return assembler;
	}
}
