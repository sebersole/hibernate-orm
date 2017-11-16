/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import javax.persistence.Tuple;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.criteria.JpaCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaDelete;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.hibernate.query.criteria.JpaCriteriaUpdate;
import org.hibernate.query.criteria.JpaIn;
import org.hibernate.query.criteria.internal.expression.JpaCollectionSizeExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralBigDecimalExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralBigIntegerExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralCharacterExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralDoubleExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralFalseExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralFloatExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralIntegerExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralLongExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralStringExpression;
import org.hibernate.query.criteria.internal.expression.JpaLiteralTrueExpression;
import org.hibernate.query.criteria.internal.expression.JpaExpressionImplementor;
import org.hibernate.query.criteria.internal.expression.JpaLiteralNullExpression;
import org.hibernate.query.criteria.internal.expression.function.JpaAvgFunctionExpression;
import org.hibernate.query.criteria.internal.expression.function.JpaSumFunctionExpression;
import org.hibernate.query.criteria.internal.path.JpaPathImplementor;
import org.hibernate.query.criteria.internal.predicate.JpaAndPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaBetweenPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaEmptinessPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaInImplementor;
import org.hibernate.query.criteria.internal.predicate.JpaInListPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaLikePredicate;
import org.hibernate.query.criteria.internal.predicate.JpaMemberOfPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaNullnessPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaOrPredicate;
import org.hibernate.query.criteria.internal.predicate.JpaPredicateImplementor;
import org.hibernate.query.criteria.internal.predicate.JpaRelationalPredicate;
import org.hibernate.query.sqm.QueryException;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;
import org.hibernate.query.sqm.tree.predicate.RelationalPredicateOperator;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.produce.metamodel.spi.ExpressableType;

/**
 *
 * @author Christian Beikov
 */
public class CriteriaBuilderImpl implements JpaCriteriaBuilder {

	private final SessionFactoryImplementor sessionFactory;

	public CriteriaBuilderImpl(SessionFactoryImplementor sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public JpaPredicateImplementor wrap(Expression<Boolean> expression) {
		return isTrue( expression );
	}
//
//	public <M extends Map<?,?>> JpaPredicateImplementor isMapEmpty(Expression<M> mapExpression);
//
//	public <M extends Map<?,?>> JpaPredicateImplementor isMapNotEmpty(Expression<M> mapExpression);
//
//	public <M extends Map<?,?>> JpaExpressionImplementor<Integer> mapSize(Expression<M> mapExpression);
//
//	public <M extends Map<?,?>> JpaExpressionImplementor<Integer> mapSize(M map);

	public <T> BasicValuedExpressableType<T> getBasicExpressableType(Class<T> clazz) {
		return sessionFactory.getTypeConfiguration().getBasicTypeRegistry().getBasicType( clazz );
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Co-variant overrides

	@Override
	public JpaCriteriaQuery<Object> createQuery() {
		return new CriteriaQueryImpl<>( this, new ParsingContext( sessionFactory ), Object.class );
	}

	@Override
	public <T> JpaCriteriaQuery<T> createQuery(Class<T> resultClass) {
		return new CriteriaQueryImpl<>( this, new ParsingContext( sessionFactory ), resultClass );
	}

	@Override
	public JpaCriteriaQuery<Tuple> createTupleQuery() {
		return new CriteriaQueryImpl<>( this, new ParsingContext( sessionFactory ), Tuple.class );
	}

	@Override
	public <T> JpaCriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
		return new CriteriaUpdateImpl<>( this, new ParsingContext( sessionFactory ) );
	}

	@Override
	public <T> JpaCriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
		return new CriteriaDeleteImpl<>( this, new ParsingContext( sessionFactory ) );
	}
//
//	@Override
//	public <Y> JpaCompoundSelection<Y> construct(Class<Y> resultClass, Selection<?>[] selections);
//
//	@Override
//	public JpaCompoundSelection<Tuple> tuple(Selection<?>[] selections);
//
//	@Override
//	public JpaCompoundSelection<Object[]> array(Selection<?>[] selections);
//
	@Override
	public <N extends Number> JpaExpressionImplementor<Double> avg(Expression<N> x) {
		JpaExpressionImplementor expr = (JpaExpressionImplementor) x;
		return new JpaAvgFunctionExpression<>(
				expr,
				sessionFactory.getTypeConfiguration()
					.getBasicTypeRegistry()
					.getBasicType( Double.class ),
				expr.context()
		);
	}

	@Override
	public <N extends Number> JpaExpressionImplementor<N> sum(Expression<N> x) {
		JpaExpressionImplementor expr = (JpaExpressionImplementor) x;
		BasicValuedExpressableType expressionType = (BasicValuedExpressableType) expr.getExpressableType();
		return new JpaSumFunctionExpression<>(
				expr,
				sessionFactory.getTypeConfiguration()
						.resolveSumFunctionType( expressionType ),
				expr.context()
		);
	}
//
//	@Override
//	public JpaExpressionImplementor<Long> sumAsLong(Expression<Integer> x);
//
//	@Override
//	public JpaExpressionImplementor<Double> sumAsDouble(Expression<Float> x);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> max(Expression<N> x);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> min(Expression<N> x);
//
//	@Override
//	public <X extends Comparable<? super X>> JpaExpressionImplementor<X> greatest(Expression<X> x);
//
//	@Override
//	public <X extends Comparable<? super X>> JpaExpressionImplementor<X> least(Expression<X> x);
//
//	@Override
//	public JpaExpressionImplementor<Long> count(Expression<?> x);
//
//	@Override
//	public JpaExpressionImplementor<Long> countDistinct(Expression<?> x);
//
//	@Override
//	public JpaPredicateImplementor exists(Subquery<?> subquery);
//
//	@Override
//	public <Y> JpaExpressionImplementor<Y> all(Subquery<Y> subquery);
//
//	@Override
//	public <Y> JpaExpressionImplementor<Y> some(Subquery<Y> subquery);
//
//	@Override
//	public <Y> JpaExpressionImplementor<Y> any(Subquery<Y> subquery);
//
	@Override
	public JpaPredicateImplementor and(Expression<Boolean> x, Expression<Boolean> y) {
		return new JpaAndPredicate( isTrue( x ), isTrue( y ), ((JpaExpressionImplementor) x).context() );
	}

	@Override
	public JpaPredicateImplementor and(Predicate... restrictions) {
		return SqmUtils.getAndPredicate( restrictions );
	}

	@Override
	public JpaPredicateImplementor or(Expression<Boolean> x, Expression<Boolean> y) {
		return new JpaOrPredicate( isTrue( x ), isTrue( y ), ((JpaExpressionImplementor) x).context() );
	}

	@Override
	public JpaPredicateImplementor or(Predicate... restrictions) {
		return SqmUtils.getOrPredicate( restrictions );
	}

	@Override
	public JpaPredicateImplementor not(Expression<Boolean> restriction) {
		if ( restriction instanceof JpaPredicateImplementor ) {
			return (JpaPredicateImplementor) ( (JpaPredicateImplementor) restriction ).not();
		} else {
			return isFalse( restriction );
		}
	}

	@Override
	public JpaPredicateImplementor conjunction() {
		// TODO: need a default context?
		final QueryContext context = null;
		return (JpaPredicateImplementor) new JpaLiteralTrueExpression(
				getBasicExpressableType( Boolean.class ),
				context
		);
	}

	@Override
	public JpaPredicateImplementor disjunction() {
		// TODO: need a default context?
		final QueryContext context = null;
		return (JpaPredicateImplementor) new JpaLiteralFalseExpression(
				getBasicExpressableType( Boolean.class ),
				context
		);
	}

	@Override
	public JpaPredicateImplementor isTrue(Expression<Boolean> x) {
		if ( x instanceof JpaPredicateImplementor ) {
			return (JpaPredicateImplementor) x;
		}

		return equal( x, Boolean.TRUE );
	}

	@Override
	public JpaPredicateImplementor isFalse(Expression<Boolean> x) {
		if ( x instanceof JpaPredicateImplementor ) {
			return (JpaPredicateImplementor) ((JpaPredicateImplementor) x).not();
		}

		return equal( x, Boolean.FALSE );
	}

	@Override
	public JpaPredicateImplementor isNull(Expression<?> x) {
		JpaExpressionImplementor<?> expr = (JpaExpressionImplementor<?>) x;
		return new JpaNullnessPredicate( (SqmExpression) x, expr.context() );
	}

	@Override
	public JpaPredicateImplementor isNotNull(Expression<?> x) {
		JpaExpressionImplementor<?> expr = (JpaExpressionImplementor<?>) x;
		return new JpaNullnessPredicate( (SqmExpression) x, true, expr.context() );
	}

	private JpaPredicateImplementor relational(RelationalPredicateOperator operator, Expression<?> x, Expression<?> y) {
		JpaExpressionImplementor<?> lhs = (JpaExpressionImplementor<?>) x;
		JpaExpressionImplementor<?> rhs = (JpaExpressionImplementor<?>) y;
		return new JpaRelationalPredicate(
				operator,
				lhs,
				rhs,
				lhs.context()
		);
	}

	private JpaPredicateImplementor relational(RelationalPredicateOperator operator, Expression<?> x, Object y) {
		JpaExpressionImplementor<?> lhs = (JpaExpressionImplementor<?>) x;
		return new JpaRelationalPredicate(
				operator,
				lhs,
				literal( y ),
				lhs.context()
		);
	}

	@Override
	public JpaPredicateImplementor equal(Expression<?> x, Expression<?> y) {
		return relational( RelationalPredicateOperator.EQUAL, x, y );
	}

	@Override
	public JpaPredicateImplementor equal(Expression<?> x, Object y) {
		return relational( RelationalPredicateOperator.EQUAL, x, y );
	}

	@Override
	public JpaPredicateImplementor notEqual(Expression<?> x, Expression<?> y) {
		return relational( RelationalPredicateOperator.NOT_EQUAL, x, y );
	}

	@Override
	public JpaPredicateImplementor notEqual(Expression<?> x, Object y) {
		return relational( RelationalPredicateOperator.NOT_EQUAL, x, y );
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor greaterThan(Expression<? extends Y> x, Expression<? extends Y> y) {
		return relational( RelationalPredicateOperator.GREATER_THAN, x, y );
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor greaterThan(Expression<? extends Y> x, Y y) {
		return relational( RelationalPredicateOperator.GREATER_THAN, x, y );
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor greaterThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y) {
		return relational( RelationalPredicateOperator.GREATER_THAN_OR_EQUAL, x, y );
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor greaterThanOrEqualTo(Expression<? extends Y> x, Y y) {
		return relational( RelationalPredicateOperator.GREATER_THAN_OR_EQUAL, x, y );
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor lessThan(Expression<? extends Y> x, Expression<? extends Y> y) {
		return relational( RelationalPredicateOperator.LESS_THAN, x, y );
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor lessThan(Expression<? extends Y> x, Y y) {
		return relational( RelationalPredicateOperator.LESS_THAN, x, y );
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor lessThanOrEqualTo(Expression<? extends Y> x, Expression<? extends Y> y) {
		return relational( RelationalPredicateOperator.LESS_THAN_OR_EQUAL, x, y );
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor lessThanOrEqualTo(Expression<? extends Y> x, Y y) {
		return relational( RelationalPredicateOperator.LESS_THAN_OR_EQUAL, x, y );
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor between(Expression<? extends Y> v, Expression<? extends Y> x, Expression<? extends Y> y) {
		JpaExpressionImplementor<?> expr = (JpaExpressionImplementor<?>) v;
		JpaExpressionImplementor<?> lower = (JpaExpressionImplementor<?>) x;
		JpaExpressionImplementor<?> upper = (JpaExpressionImplementor<?>) y;
		return new JpaBetweenPredicate(
				expr,
				lower,
				upper,
				false,
				expr.context()
		);
	}

	@Override
	public <Y extends Comparable<? super Y>> JpaPredicateImplementor between(Expression<? extends Y> v, Y x, Y y) {
		JpaExpressionImplementor<?> expr = (JpaExpressionImplementor<?>) v;
		return new JpaBetweenPredicate(
				expr,
				literal( x ),
				literal( y ),
				false,
				expr.context()
		);
	}

	@Override
	public JpaPredicateImplementor gt(Expression<? extends Number> x, Expression<? extends Number> y) {
		return relational( RelationalPredicateOperator.GREATER_THAN, x, y );
	}

	@Override
	public JpaPredicateImplementor gt(Expression<? extends Number> x, Number y) {
		return relational( RelationalPredicateOperator.GREATER_THAN, x, y );
	}

	@Override
	public JpaPredicateImplementor ge(Expression<? extends Number> x, Expression<? extends Number> y) {
		return relational( RelationalPredicateOperator.GREATER_THAN_OR_EQUAL, x, y );
	}

	@Override
	public JpaPredicateImplementor ge(Expression<? extends Number> x, Number y) {
		return relational( RelationalPredicateOperator.GREATER_THAN_OR_EQUAL, x, y );
	}

	@Override
	public JpaPredicateImplementor lt(Expression<? extends Number> x, Expression<? extends Number> y) {
		return relational( RelationalPredicateOperator.LESS_THAN, x, y );
	}

	@Override
	public JpaPredicateImplementor lt(Expression<? extends Number> x, Number y) {
		return relational( RelationalPredicateOperator.LESS_THAN, x, y );
	}

	@Override
	public JpaPredicateImplementor le(Expression<? extends Number> x, Expression<? extends Number> y) {
		return relational( RelationalPredicateOperator.LESS_THAN_OR_EQUAL, x, y );
	}

	@Override
	public JpaPredicateImplementor le(Expression<? extends Number> x, Number y) {
		return relational( RelationalPredicateOperator.LESS_THAN_OR_EQUAL, x, y );
	}
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> neg(Expression<N> x);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> abs(Expression<N> x);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> sum(Expression<? extends N> x, Expression<? extends N> y);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> sum(Expression<? extends N> x, N y);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> sum(N x, Expression<? extends N> y);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> prod(Expression<? extends N> x, Expression<? extends N> y);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> prod(Expression<? extends N> x, N y);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> prod(N x, Expression<? extends N> y);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> diff(Expression<? extends N> x, Expression<? extends N> y);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> diff(Expression<? extends N> x, N y);
//
//	@Override
//	public <N extends Number> JpaExpressionImplementor<N> diff(N x, Expression<? extends N> y);
//
//	@Override
//	public JpaExpressionImplementor<Number> quot(Expression<? extends Number> x, Expression<? extends Number> y);
//
//	@Override
//	public JpaExpressionImplementor<Number> quot(Expression<? extends Number> x, Number y);
//
//	@Override
//	public JpaExpressionImplementor<Number> quot(Number x, Expression<? extends Number> y);
//
//	@Override
//	public JpaExpressionImplementor<Integer> mod(Expression<Integer> x, Expression<Integer> y);
//
//	@Override
//	public JpaExpressionImplementor<Integer> mod(Expression<Integer> x, Integer y);
//
//	@Override
//	public JpaExpressionImplementor<Integer> mod(Integer x, Expression<Integer> y);
//
//	@Override
//	public JpaExpressionImplementor<Double> sqrt(Expression<? extends Number> x);
//
//	@Override
//	public JpaExpressionImplementor<Long> toLong(Expression<? extends Number> number);
//
//	@Override
//	public JpaExpressionImplementor<Integer> toInteger(Expression<? extends Number> number);
//
//	@Override
//	public JpaExpressionImplementor<Float> toFloat(Expression<? extends Number> number);
//
//	@Override
//	public JpaExpressionImplementor<Double> toDouble(Expression<? extends Number> number);
//
//	@Override
//	public JpaExpressionImplementor<BigDecimal> toBigDecimal(Expression<? extends Number> number);
//
//	@Override
//	public JpaExpressionImplementor<BigInteger> toBigInteger(Expression<? extends Number> number);
//
//	@Override
//	public JpaExpressionImplementor<String> toString(Expression<Character> character);
//
	@Override
	@SuppressWarnings("unchecked")
	public <T> JpaExpressionImplementor<T> literal(T value) {
		if ( value == null ) {
			throw new NullPointerException( "Value passed as `constant value` cannot be null" );
		}
		final Class<T> javaType = (Class<T>) value.getClass();
		// TODO: need a default context?
		final QueryContext context = null;
		if ( Boolean.class.isAssignableFrom( javaType ) ) {
			if ( (Boolean) value ) {
				return (JpaExpressionImplementor<T>) new JpaLiteralTrueExpression(
						getBasicExpressableType( Boolean.class ),
						context
				);
			}
			else {
				return (JpaExpressionImplementor<T>) new JpaLiteralFalseExpression(
						getBasicExpressableType( Boolean.class ),
						context
				);
			}
		}
		else if ( Integer.class.isAssignableFrom( javaType ) ) {
			return (JpaExpressionImplementor<T>) new JpaLiteralIntegerExpression(
					(Integer) value,
					getBasicExpressableType( Integer.class ),
					context
			);
		}
		else if ( Long.class.isAssignableFrom( javaType ) ) {
			return (JpaExpressionImplementor<T>) new JpaLiteralLongExpression(
					(Long) value,
					getBasicExpressableType( Long.class ),
					context
			);
		}
		else if ( Float.class.isAssignableFrom( javaType ) ) {
			return (JpaExpressionImplementor<T>) new JpaLiteralFloatExpression(
					(Float) value,
					getBasicExpressableType( Float.class ),
					context
			);
		}
		else if ( Double.class.isAssignableFrom( javaType ) ) {
			return (JpaExpressionImplementor<T>) new JpaLiteralDoubleExpression(
					(Double) value,
					getBasicExpressableType( Double.class ),
					context
			);
		}
		else if ( BigInteger.class.isAssignableFrom( javaType ) ) {
			return (JpaExpressionImplementor<T>) new JpaLiteralBigIntegerExpression(
					(BigInteger) value,
					getBasicExpressableType( BigInteger.class ),
					context
			);
		}
		else if ( BigDecimal.class.isAssignableFrom( javaType ) ) {
			return (JpaExpressionImplementor<T>) new JpaLiteralBigDecimalExpression(
					(BigDecimal) value,
					getBasicExpressableType( BigDecimal.class ),
					context

			);
		}
		else if ( Character.class.isAssignableFrom( javaType ) ) {
			return (JpaExpressionImplementor<T>) new JpaLiteralCharacterExpression(
					(Character) value,
					getBasicExpressableType( Character.class ),
					context
			);
		}
		else if ( String.class.isAssignableFrom( javaType ) ) {
			return (JpaExpressionImplementor<T>) new JpaLiteralStringExpression(
					(String) value,
					getBasicExpressableType( String.class ),
					context
			);
		}

		throw new QueryException(
				"Unexpected literal expression [value=" + value +
						", javaType=" + javaType.getName() +
						"]; expecting boolean, int, long, float, double, BigInteger, BigDecimal, char, or String"
		);
	}

	@Override
	public <T> JpaExpressionImplementor<T> nullLiteral(Class<T> resultClass) {
		// TODO: Need a default context?
		final QueryContext context = null;
		return new JpaLiteralNullExpression<T>( context, getBasicExpressableType( resultClass ) );
	}
//
//	@Override
//	public <T> JpaParameterExpression<T> parameter(Class<T> paramClass);
//
//	@Override
//	public <T> JpaParameterExpression<T> parameter(Class<T> paramClass, String name);
//
	@Override
	public <C extends Collection<?>> JpaPredicateImplementor isEmpty(Expression<C> collection) {
		JpaPathImplementor<C> expression = (JpaPathImplementor<C>) collection;
		return new JpaEmptinessPredicate( (SqmPluralAttributeReference) expression.getNavigableReference(), expression.context() );
	}

	@Override
	public <C extends Collection<?>> JpaPredicateImplementor isNotEmpty(Expression<C> collection) {
		JpaPathImplementor<C> expression = (JpaPathImplementor<C>) collection;
		return new JpaEmptinessPredicate( (SqmPluralAttributeReference) expression.getNavigableReference(), true, expression.context() );
	}

	@Override
	public <C extends Collection<?>> JpaExpressionImplementor<Integer> size(Expression<C> collection) {
		JpaPathImplementor<C> expression = (JpaPathImplementor<C>) collection;
		return new JpaCollectionSizeExpression(
				(SqmPluralAttributeReference) expression.getNavigableReference(),
				getBasicExpressableType( Integer.class ),
				expression.context()
		);
	}

	@Override
	public <C extends Collection<?>> JpaExpressionImplementor<Integer> size(C collection) {
		int size = collection == null ? 0 : collection.size();
		return literal(size);
	}

	@Override
	public <E, C extends Collection<E>> JpaPredicateImplementor isMember(Expression<E> elem, Expression<C> collection) {
		JpaPathImplementor<C> expression = (JpaPathImplementor<C>) collection;
		return new JpaMemberOfPredicate(
				(JpaExpressionImplementor) elem,
				(SqmPluralAttributeReference) expression.getNavigableReference(),
				expression.context()
		);
	}

	@Override
	public <E, C extends Collection<E>> JpaPredicateImplementor isMember(E elem, Expression<C> collection) {
		JpaPathImplementor<C> expression = (JpaPathImplementor<C>) collection;
		return new JpaMemberOfPredicate(
				literal( elem ),
				(SqmPluralAttributeReference) expression.getNavigableReference(),
				expression.context()
		);
	}

	@Override
	public <E, C extends Collection<E>> JpaPredicateImplementor isNotMember(Expression<E> elem, Expression<C> collection) {
		JpaPathImplementor<C> expression = (JpaPathImplementor<C>) collection;
		return new JpaMemberOfPredicate(
				(JpaExpressionImplementor) elem,
				(SqmPluralAttributeReference) expression.getNavigableReference(),
				true,
				expression.context()
		);
	}

	@Override
	public <E, C extends Collection<E>> JpaPredicateImplementor isNotMember(E elem, Expression<C> collection) {
		JpaPathImplementor<C> expression = (JpaPathImplementor<C>) collection;
		return new JpaMemberOfPredicate(
				literal( elem ),
				(SqmPluralAttributeReference) expression.getNavigableReference(),
				true,
				expression.context()
		);
	}
//
//	@Override
//	public <V, M extends Map<?, V>> JpaExpressionImplementor<Collection<V>> values(M map);
//
//	@Override
//	public <K, M extends Map<K, ?>> JpaExpressionImplementor<Set<K>> keys(M map);
//
	@Override
	public JpaPredicateImplementor like(Expression<String> x, Expression<String> pattern) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				(JpaExpressionImplementor<String>) pattern,
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor like(Expression<String> x, String pattern) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				literal( pattern ),
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor like(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				(JpaExpressionImplementor<String>) pattern,
				(JpaExpressionImplementor<Character>) escapeChar,
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor like(Expression<String> x, Expression<String> pattern, char escapeChar) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				(JpaExpressionImplementor<String>) pattern,
				literal( escapeChar ),
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor like(Expression<String> x, String pattern, Expression<Character> escapeChar) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				literal( pattern ),
				(JpaExpressionImplementor<Character>) escapeChar,
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor like(Expression<String> x, String pattern, char escapeChar) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				literal( pattern ),
				literal( escapeChar ),
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor notLike(Expression<String> x, Expression<String> pattern) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				(JpaExpressionImplementor<String>) pattern,
				true,
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor notLike(Expression<String> x, String pattern) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				literal( pattern ),
				true,
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor notLike(Expression<String> x, Expression<String> pattern, Expression<Character> escapeChar) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				(JpaExpressionImplementor<String>) pattern,
				(JpaExpressionImplementor<Character>) escapeChar,
				true,
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor notLike(Expression<String> x, Expression<String> pattern, char escapeChar) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				(JpaExpressionImplementor<String>) pattern,
				literal( escapeChar ),
				true,
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor notLike(Expression<String> x, String pattern, Expression<Character> escapeChar) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				literal( pattern ),
				(JpaExpressionImplementor<Character>) escapeChar,
				true,
				expression.context()
		);
	}

	@Override
	public JpaPredicateImplementor notLike(Expression<String> x, String pattern, char escapeChar) {
		JpaExpressionImplementor<String> expression = (JpaExpressionImplementor<String>) x;
		return new JpaLikePredicate(
				expression,
				literal( pattern ),
				literal( escapeChar ),
				true,
				expression.context()
		);
	}
//
//	@Override
//	public JpaExpressionImplementor<String> concat(Expression<String> x, Expression<String> y);
//
//	@Override
//	public JpaExpressionImplementor<String> concat(Expression<String> x, String y);
//
//	@Override
//	public JpaExpressionImplementor<String> concat(String x, Expression<String> y);
//
//	@Override
//	public JpaExpressionImplementor<String> substring(Expression<String> x, Expression<Integer> from);
//
//	@Override
//	public JpaExpressionImplementor<String> substring(Expression<String> x, int from);
//
//	@Override
//	public JpaExpressionImplementor<String> substring(Expression<String> x, Expression<Integer> from, Expression<Integer> len);
//
//	@Override
//	public JpaExpressionImplementor<String> substring(Expression<String> x, int from, int len);
//
//	@Override
//	public JpaExpressionImplementor<String> trim(Expression<String> x);
//
//	@Override
//	public JpaExpressionImplementor<String> trim(Trimspec ts, Expression<String> x);
//
//	@Override
//	public JpaExpressionImplementor<String> trim(Expression<Character> t, Expression<String> x);
//
//	@Override
//	public JpaExpressionImplementor<String> trim(Trimspec ts, Expression<Character> t, Expression<String> x);
//
//	@Override
//	public JpaExpressionImplementor<String> trim(char t, Expression<String> x);
//
//	@Override
//	public JpaExpressionImplementor<String> trim(Trimspec ts, char t, Expression<String> x);
//
//	@Override
//	public JpaExpressionImplementor<String> lower(Expression<String> x);
//
//	@Override
//	public JpaExpressionImplementor<String> upper(Expression<String> x);
//
//	@Override
//	public JpaExpressionImplementor<Integer> length(Expression<String> x);
//
//	@Override
//	public JpaExpressionImplementor<Integer> locate(Expression<String> x, Expression<String> pattern);
//
//	@Override
//	public JpaExpressionImplementor<Integer> locate(Expression<String> x, String pattern);
//
//	@Override
//	public JpaExpressionImplementor<Integer> locate(Expression<String> x, Expression<String> pattern, Expression<Integer> from);
//
//	@Override
//	public JpaExpressionImplementor<Integer> locate(Expression<String> x, String pattern, int from);
//
//	@Override
//	public JpaExpressionImplementor<Date> currentDate();
//
//	@Override
//	public JpaExpressionImplementor<Timestamp> currentTimestamp();
//
//	@Override
//	public JpaExpressionImplementor<Time> currentTime();
//
	@Override
	public <T> JpaInImplementor<T> in(Expression<? extends T> expression) {
		JpaExpressionImplementor expr = (JpaExpressionImplementor) expression;
		return new JpaInListPredicate<T>( expr, expr.context() );
	}

	@SuppressWarnings("unchecked")
	public <T> JpaInImplementor<T> in(Expression<? extends T> expression, Expression<? extends T>... values) {
		JpaExpressionImplementor expr = (JpaExpressionImplementor) expression;
		JpaInListPredicate<T> in = new JpaInListPredicate( expr, expr.context() );
		for ( Expression<? extends T> value : values ) {
			in.addExpression( (SqmExpression) value );
		}
		return in;
	}

	@SuppressWarnings("unchecked")
	public <T> JpaInImplementor<T> in(Expression<? extends T> expression, T... values) {
		JpaExpressionImplementor expr = (JpaExpressionImplementor) expression;
		JpaInListPredicate<T> in = new JpaInListPredicate( expr, expr.context() );
		for ( T value : values ) {
			in.addExpression( literal( value ) );
		}
		return in;
	}
//
//	@Override
//	public <Y> JpaCoalesce<Y> coalesce(Expression<? extends Y> x, Expression<? extends Y> y);
//
//	@Override
//	public <Y> JpaExpressionImplementor<Y> coalesce(Expression<? extends Y> x, Y y);
//
//	@Override
//	public <Y> JpaExpressionImplementor<Y> nullif(Expression<Y> x, Expression<?> y);
//
//	@Override
//	public <Y> JpaExpressionImplementor<Y> nullif(Expression<Y> x, Y y);
//
//	@Override
//	public <T> JpaCoalesce<T> coalesce();
//
//	@Override
//	public <C, R> JpaSimpleCase<C, R> selectCase(Expression<? extends C> expression);
//
//	@Override
//	public <R> JpaSearchedCase<R> selectCase();
//
//	@Override
//	public <T> JpaExpressionImplementor<T> function(String name, Class<T> type, Expression<?>[] args);
//
//	@Override
//	public <X, T, V extends T> JpaAttributeJoinImplementor<X, V> treat(Join<X, T> join, Class<V> type);
//
//	@Override
//	public <X, T, E extends T> JpaCollectionJoinImplementor<X, E> treat(CollectionJoin<X, T> join, Class<E> type);
//
//	@Override
//	public <X, T, E extends T> JpaSetJoinImplementor<X, E> treat(SetJoin<X, T> join, Class<E> type);
//
//	@Override
//	public <X, T, E extends T> JpaListJoinImplementor<X, E> treat(ListJoin<X, T> join, Class<E> type);
//
//	@Override
//	public <X, K, T, V extends T> JpaMapJoinImplementor<X, K, V> treat(MapJoin<X, K, T> join, Class<V> type);
//
//	@Override
//	public <X, T extends X> JpaPathImplementor<T> treat(Path<X> path, Class<T> type);
//
//	@Override
//	public <X, T extends X> JpaRoot<T> treat(Root<X> root, Class<T> type);
}
