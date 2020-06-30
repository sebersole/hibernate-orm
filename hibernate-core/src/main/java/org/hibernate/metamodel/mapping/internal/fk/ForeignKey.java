/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.predicate.Predicate;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;

/**
 * Throughout this pa
 * @author Steve Ebersole
 */
public interface ForeignKey {
	String PART_NAME = "{fk}";

	Side getReferringSide();

	Side getTargetSide();

	@Deprecated
	DomainResult createCollectionFetchDomainResult(
			NavigablePath collectionPath,
			TableGroup tableGroup,
			DomainResultCreationState creationState);

	@Deprecated
	DomainResult createDomainResult(
			NavigablePath collectionPath,
			TableGroup tableGroup,
			DomainResultCreationState creationState);

	@Deprecated
	Fetch createReferringKeyFetch(
			NavigablePath associationPath,
			TableGroup tableGroup,
			FetchParent fetchParent,
			DomainResultCreationState creationState);

	@Deprecated
	Fetch createTargetKeyFetch(
			NavigablePath associationPath,
			TableGroup tableGroup,
			FetchParent fetchParent,
			DomainResultCreationState creationState);

	Predicate generateJoinPredicate(
			TableGroup lhs,
			TableGroup tableGroup,
			SqlAstJoinType sqlAstJoinType,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext);

	Predicate generateJoinPredicate(
			TableReference lhs,
			TableReference rhs,
			SqlAstJoinType sqlAstJoinType,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext);

	/**
	 * The name of the table that defines the referring side of the foreign-key
	 *
	 * @apiNote Informational only
	 */
	String getReferringTableExpression();

	/**
	 * The name of the table that defines the target side of the foreign-key
	 *
	 * @apiNote Informational only
	 */
	String getTargetTableExpression();

	void visitReferringColumns(ColumnConsumer consumer);

	void visitTargetColumns(ColumnConsumer consumer);


	boolean areTargetColumnNamesEqualsTo(String[] columnNames);
}
