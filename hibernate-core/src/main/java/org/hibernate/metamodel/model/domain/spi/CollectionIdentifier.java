/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

package org.hibernate.metamodel.model.domain.spi;

import org.hibernate.id.IdentifierGenerator;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.sql.ast.produce.spi.SqlExpressionResolver;
import org.hibernate.sql.results.internal.domain.basic.BasicResultImpl;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultCreationContext;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.DomainResultProducer;
import org.hibernate.type.spi.BasicType;

/**
 * @author Steve Ebersole
 */
public class CollectionIdentifier implements DomainResultProducer {
	private final BasicType type;
	private final Column column;

	private final IdentifierGenerator generator;

	public CollectionIdentifier(BasicType type, Column column, IdentifierGenerator generator) {
		this.type = type;
		this.column = column;
		this.generator = generator;
	}


	public IdentifierGenerator getGenerator() {
		return generator;
	}

	public BasicType getBasicType() {
		return type;
	}

	// todo (6.0) : where is the identifier column kept track of?  seems like it should be here


	@Override
	public DomainResult createDomainResult(
			String resultVariable,
			DomainResultCreationState creationState,
			DomainResultCreationContext creationContext) {
		final SqlExpressionResolver sqlExpressionResolver = creationState.getSqlExpressionResolver();

		return new BasicResultImpl(
				resultVariable,
				sqlExpressionResolver.resolveSqlSelection(
						sqlExpressionResolver.resolveSqlExpression(
								creationState.getColumnReferenceQualifierStack().getCurrent(),
								column
						),
						getBasicType().getJavaTypeDescriptor(),
						creationContext.getSessionFactory().getTypeConfiguration()
				),
				column.getExpressableType()
		);
	}
}
