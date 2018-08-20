/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.sql.ast.tree.spi.expression;

import java.util.Locale;
import java.util.Objects;

import org.hibernate.dialect.Dialect;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.SqlExpressableType;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.SqlExpressable;
import org.hibernate.sql.ast.tree.spi.from.TableReference;
import org.hibernate.sql.results.internal.SqlSelectionImpl;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class ColumnReference implements Expression {
	private final ColumnReferenceQualifier qualifier;
	private final Column column;

	public ColumnReference(ColumnReferenceQualifier qualifier, Column column) {
		assert qualifier != null;

		this.qualifier = qualifier;
		this.column = column;
	}

	public ColumnReference(Column column) {
		this.qualifier = null;
		this.column = column;
	}

	@Override
	public SqlSelection createSqlSelection(
			int jdbcPosition,
			int valuesArrayPosition,
			BasicJavaDescriptor javaTypeDescriptor,
			TypeConfiguration typeConfiguration) {
		final JdbcValueExtractor jdbcValueExtractor = getColumn().getSqlTypeDescriptor().getSqlExpressableType(
				column.getSqlTypeDescriptor().getJdbcRecommendedJavaTypeMapping( typeConfiguration ),
				typeConfiguration
		).getJdbcValueExtractor();
		return new SqlSelectionImpl(
				jdbcPosition,
				valuesArrayPosition,
				this,
				jdbcValueExtractor
		);
	}

	public ColumnReferenceQualifier getQualifier() {
		return qualifier;
	}

	public Column getColumn() {
		return column;
	}

	@Override
	public void accept(SqlAstWalker  interpreter) {
		interpreter.visitColumnReference( this );
	}

	@Override
	public SqlExpressableType getType() {
		// n/a
		return null;
	}

	@Override
	public SqlExpressable getExpressable() {
		return getColumn();
	}

	public String renderSqlFragment(Dialect dialect) {
		if ( qualifier == null ) {
			return column.getExpression();
		}
		final TableReference tableReference = qualifier.locateTableReference( column.getSourceTable() );
		return column.render( tableReference.getIdentificationVariable(), dialect );
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		final ColumnReference that = (ColumnReference) o;
		return Objects.equals( qualifier, that.qualifier )
				&& getColumn().equals( that.getColumn() );
	}

	@Override
	public int hashCode() {
		int result = getColumn().hashCode();
		if ( qualifier != null ) {
			result = 31 * result + qualifier.hashCode();
		}
		return result;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"%s(%s.%s)",
				getClass().getSimpleName(),
				qualifier,
				column.getExpression()
		);
	}
}
