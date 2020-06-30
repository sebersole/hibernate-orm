/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.from;

import java.util.List;
import java.util.function.Supplier;

import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractColumnReferenceQualifier implements ColumnReferenceQualifier {
	protected abstract TableReference getPrimaryTableReference();

	protected abstract List<TableReferenceJoin> getTableReferenceJoins();

	protected abstract SessionFactoryImplementor getSessionFactory();

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// TableReference handling

	@Override
	public TableReference resolveTableReference(String tableExpression, Supplier<TableReference> creator) {
		final TableReference existing = resolveTableReferenceInternal( tableExpression );
		if ( existing != null ) {
			return existing;
		}

		return creator.get();
	}

	@Override
	public TableReference resolveTableReference(String tableExpression) {
		assert tableExpression != null;

		final TableReference tableReference = resolveTableReferenceInternal( tableExpression );
		if ( tableReference == null ) {
			final StringBuilder buffer = new StringBuilder()
					.append( "Could not resolve TableReference for table `" ).append( tableExpression ).append( "`" )
					.append( System.lineSeparator() )
					.append( "  Existing tables:" )
					.append( System.lineSeparator() )
					.append( "`" ).append( getPrimaryTableReference().getTableExpression() ).append( "`" );

			for ( int i = 0; i < getTableReferenceJoins().size(); i++ ) {
				buffer.append( "," ).append( System.lineSeparator() );

				final TableReferenceJoin tableReferenceJoin = getTableReferenceJoins().get( i );
				final TableReference joinedTableReference = tableReferenceJoin.getJoinedTableReference();
				buffer.append( "`" ).append( joinedTableReference.getTableExpression() ).append( "`" );
			}

			throw new IllegalStateException( buffer.toString() );
		}

		return tableReference;
	}

	@Override
	public TableReference getTableReference(String tableExpression) {
		return resolveTableReferenceInternal( tableExpression );
	}

	protected TableReference resolveTableReferenceInternal(String tableExpression) {
		if ( getPrimaryTableReference().getTableExpression().equals( tableExpression ) ) {
			return getPrimaryTableReference();
		}

		for ( TableReferenceJoin tableJoin : getTableReferenceJoins() ) {
			if ( tableJoin.getJoinedTableReference().getTableExpression().equals( tableExpression ) ) {
				return tableJoin.getJoinedTableReference();
			}
		}

		return null;
	}

}
