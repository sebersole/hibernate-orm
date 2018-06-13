/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.tree.spi.expression.QueryLiteral;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.sql.results.spi.SqlSelectionReader;

/**
 * @author Steve Ebersole
 */
public class EmptySqlSelection implements SqlSelection {
	private final int position;

	public EmptySqlSelection(int position) {
		this.position = position;
	}

	@Override
	public SqlSelectionReader getSqlSelectionReader() {
		return EmptySqlSelectionReader.INSTANCE;
	}

	@Override
	public int getValuesArrayPosition() {
		return position;
	}

	@Override
	public void accept(SqlAstWalker interpreter) {
		// todo (6.0) : see the note on `BaseSemanticQueryWalker#visitSelectClause`
		interpreter.visitQueryLiteral(
				new QueryLiteral(
						null,
						null,
						true
				)
		);
	}
}
