/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.spi;

import java.util.List;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.internal.util.collections.Stack;
import org.hibernate.sql.ast.produce.metamodel.spi.SqlAliasBaseGenerator;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.ast.tree.spi.from.TableSpace;

/**
 * @author Steve Ebersole
 */
public interface DomainResultCreationState {
	SqlExpressionResolver getSqlExpressionResolver();

	Stack<ColumnReferenceQualifier> getColumnReferenceQualifierStack();

	Stack<NavigableReference> getNavigableReferenceStack();

	default SqlAliasBaseGenerator getSqlAliasBaseGenerator() {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	boolean fetchAllAttributes();

	default List<Fetch> visitFetches(FetchParent fetchParent) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	TableSpace getCurrentTableSpace();

	// todo (6.0) : what else do we need to properly allow Fetch creation the ability to create/
	//		actually, this (^^) is not true
	//
	// fetches ought to include basic attributes too.  Handling "all attributes fetched"
	//		can be handled in 2 logical places:
	//			1) DomainResultCreationState#visitFetches - creates the sub-fetch graph
	//			2) The `Initializer` registered by each
}
