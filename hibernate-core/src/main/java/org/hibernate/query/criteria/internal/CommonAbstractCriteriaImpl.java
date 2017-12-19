/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;

import org.hibernate.HibernateException;
import org.hibernate.query.criteria.JpaCommonAbstractCriteria;
import org.hibernate.query.criteria.JpaCriteriaBuilder;
import org.hibernate.query.criteria.JpaPredicate;
import org.hibernate.query.criteria.JpaSubquery;
import org.hibernate.query.criteria.internal.path.JpaRootImpl;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.SqmQuerySpec;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.SubQuerySqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.predicate.SqmPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.tree.select.SqmSelection;
import org.hibernate.sql.ast.produce.metamodel.spi.EntityValuedExpressableType;
import org.hibernate.type.spi.BasicType;
import org.hibernate.type.spi.TypeConfiguration;

public abstract class CommonAbstractCriteriaImpl implements JpaCommonAbstractCriteria {

	protected final JpaCriteriaBuilder criteriaBuilder;
	protected final ParsingContext parsingContext;
	protected final QueryContext queryContext;

	public CommonAbstractCriteriaImpl(
			JpaCriteriaBuilder builder,
			ParsingContext parsingContext) {
		this.criteriaBuilder = builder;
		this.parsingContext = parsingContext;
		this.queryContext = new QueryContext() {
			@Override
			public JpaCriteriaBuilder criteriaBuilder() {
				return criteriaBuilder;
			}

			@Override
			public ParsingContext parsingContext() {
				return parsingContext;
			}
		};
	}

	protected abstract SqmFromElementSpace getFromElementSpace();

	protected abstract SqmWhereClause getWhereClause();

	@Override
	public <U> JpaSubquery<U> subquery(Class<U> type) {
		TypeConfiguration typeConfiguration = parsingContext.getSessionFactory().getTypeConfiguration();
		BasicType<U> basicType = typeConfiguration.getBasicTypeRegistry().getBasicType( type );
		return new SubQuerySqmExpression( criteriaBuilder, parsingContext, this, new SqmQuerySpec(), basicType );
	}

	@Override
	public JpaPredicate getRestriction() {
		return (JpaPredicate) getWhereClause().getPredicate();
	}

	protected final <T> JpaRootImpl<T> createRoot(EntityType<T> entityType) {
		return createRootInternal( entityType.getJavaType() );
	}

	protected final <T> JpaRootImpl<T> createRoot(Class<T> entityClass) {
		try {
			return createRootInternal( entityClass );
		} catch (HibernateException ex) {
			throw new IllegalArgumentException( "Not an entity type : " + entityClass.getName(), ex );
		}
	}

	private final <T> JpaRootImpl<T> createRootInternal(Class<T> entityClass) {
		EntityValuedExpressableType<T> entityReference = parsingContext.getSessionFactory()
				.getTypeConfiguration()
				.resolveEntityReference( entityClass );
		String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		return new JpaRootImpl<>( getFromElementSpace(), parsingContext.makeUniqueIdentifier(), alias, entityReference, queryContext );
	}

	protected final SqmNavigableReference getAttributeReference(SqmRoot root, String attributePath) {
		return parsingContext.findOrCreateNavigableBinding(
				root.getNavigableReference(),
				attributePath
		);
	}

	protected final SqmSelection createSelection(Expression expression) {
		return new SqmSelection( (SqmExpression) expression );
	}

	protected final void setWhere(SqmWhereClause whereClause, Expression restriction) {
		whereClause.setPredicate( (SqmPredicate) restriction );
	}

	protected final void setWhere(SqmWhereClause whereClause, Predicate... restrictions) {
		if (restrictions.length == 0) {
			whereClause.setPredicate( null );
			return;
		}

		whereClause.setPredicate( (SqmPredicate) SqmUtils.getAndPredicate( restrictions ) );
	}

}
