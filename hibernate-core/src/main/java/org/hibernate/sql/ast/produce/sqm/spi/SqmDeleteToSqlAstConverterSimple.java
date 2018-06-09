/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.sqm.spi;

import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.sqm.consume.spi.BaseSqmToSqlAstConverter;
import org.hibernate.query.sqm.tree.SqmDeleteStatement;
import org.hibernate.sql.ast.produce.spi.SqlAstBuildingContext;
import org.hibernate.sql.ast.tree.spi.DeleteStatement;
import org.hibernate.sql.ast.tree.spi.from.TableReference;
import org.hibernate.sql.ast.tree.spi.predicate.Predicate;

/**
 * @author Steve Ebersole
 */
public class SqmDeleteToSqlAstConverterSimple extends BaseSqmToSqlAstConverter {
	public static DeleteStatement interpret(
			SqmDeleteStatement sqmStatement,
			QueryOptions queryOptions,
			SqlAstBuildingContext buildingContext) {
		final SqmDeleteToSqlAstConverterSimple walker = new SqmDeleteToSqlAstConverterSimple(
				buildingContext,
				queryOptions
		);
		walker.visitDeleteStatement( sqmStatement );
		return walker.deleteStatement;
	}

	private DeleteStatement deleteStatement;

	private SqmDeleteToSqlAstConverterSimple(
			SqlAstBuildingContext sqlAstBuildingContext,
			QueryOptions queryOptions) {
		super( sqlAstBuildingContext, queryOptions );

	}

	@Override
	public Object visitDeleteStatement(SqmDeleteStatement sqmStatement) {
		final Predicate restriction;
		if ( sqmStatement.getWhereClause() != null && sqmStatement.getWhereClause().getPredicate() != null ) {
			restriction = (Predicate) sqmStatement.getWhereClause().getPredicate().accept( this );
		}
		else {
			restriction = null;
		}

		deleteStatement = new DeleteStatement(
				new TableReference(
						sqmStatement.getEntityFromElement().getNavigableReference().getEntityDescriptor().getPrimaryTable(),
						null
				),
				restriction
		);
		return deleteStatement;
	}
}
