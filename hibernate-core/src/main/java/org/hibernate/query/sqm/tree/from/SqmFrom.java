/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sqm.tree.from;

import java.util.List;
import java.util.function.Consumer;

import org.hibernate.metamodel.model.domain.EntityDomainType;
import org.hibernate.query.criteria.JpaFrom;
import org.hibernate.query.sqm.SqmPathSource;
import org.hibernate.query.sqm.tree.SqmCopyContext;
import org.hibernate.query.sqm.tree.SqmVisitableNode;
import org.hibernate.query.sqm.tree.domain.SqmBagJoin;
import org.hibernate.query.sqm.tree.domain.SqmListJoin;
import org.hibernate.query.sqm.tree.domain.SqmMapJoin;
import org.hibernate.query.sqm.tree.domain.SqmPath;
import org.hibernate.query.sqm.tree.domain.SqmSetJoin;
import org.hibernate.query.sqm.tree.domain.SqmSingularJoin;
import org.hibernate.query.sqm.tree.domain.SqmTreatedFrom;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.metamodel.CollectionAttribute;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.MapAttribute;
import jakarta.persistence.metamodel.SetAttribute;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * Models a SqmPathSource's inclusion in the {@code FROM} clause.
 *
 * @param <L> The from-element's "left hand side".  It may be the same as {@code R} for roots.
 * @param <R> The from-element's "right hand side".  For joins, this is the target side.
 *
 * @author Steve Ebersole
 */
public interface SqmFrom<L, R> extends SqmVisitableNode, SqmPath<R>, JpaFrom<L, R> {
	/**
	 * The Navigable for an SqmFrom will always be a NavigableContainer
	 *
	 * {@inheritDoc}
	 * @return
	 */
	@Override
	SqmPathSource<R> getReferencedPathSource();

	boolean hasJoins();

	/**
	 * The joins associated with this SqmFrom
	 */
	List<SqmJoin<R,?>> getSqmJoins();

	/**
	 * Add an associated join
	 */
	void addSqmJoin(SqmJoin<R, ?> join);

	/**
	 * Visit all associated joins
	 */
	void visitSqmJoins(Consumer<SqmJoin<R, ?>> consumer);

	@Override
	<S extends R> SqmTreatedFrom<L,R,S> treatAs(Class<S> treatJavaType);

	@Override
	<S extends R> SqmTreatedFrom<L,R,S> treatAs(EntityDomainType<S> treatTarget);

	@Override
	<S extends R> SqmTreatedFrom<L,R,S> treatAs(Class<S> treatJavaType, String alias);

	@Override
	<S extends R> SqmTreatedFrom<L,R,S> treatAs(EntityDomainType<S> treatTarget, String alias);

	@Override
	<S extends R> SqmTreatedFrom<L,R,S> treatAs(Class<S> treatJavaType, String alias, boolean fetch);

	@Override
	<S extends R> SqmTreatedFrom<L,R,S> treatAs(EntityDomainType<S> treatTarget, String alias, boolean fetch);

	boolean hasTreats();

	/**
	 * The treats associated with this SqmFrom
	 */
	List<SqmTreatedFrom<?,?,?>> getSqmTreats();


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// JPA

	@Override
	SqmFrom<L, R> getCorrelationParent();

	@Override
	<Y> SqmEntityJoin<R, Y> join(Class<Y> entityClass);

	@Override
	<Y> SqmEntityJoin<R, Y> join(Class<Y> entityClass, JoinType joinType);

	@Override
	<A> SqmSingularJoin<R, A> join(SingularAttribute<? super R, A> attribute);

	@Override
	<A> SqmSingularJoin<R, A> join(SingularAttribute<? super R, A> attribute, JoinType jt);

	@Override
	<E> SqmBagJoin<R, E> join(CollectionAttribute<? super R, E> attribute);

	@Override
	<E> SqmBagJoin<R, E> join(CollectionAttribute<? super R, E> attribute, JoinType jt);

	@Override
	<E> SqmSetJoin<R, E> join(SetAttribute<? super R, E> set);

	@Override
	<E> SqmSetJoin<R, E> join(SetAttribute<? super R, E> set, JoinType jt);

	@Override
	<E> SqmListJoin<R, E> join(ListAttribute<? super R, E> list);

	@Override
	<E> SqmListJoin<R, E> join(ListAttribute<? super R, E> list, JoinType jt);

	@Override
	<K, V> SqmMapJoin<R, K, V> join(MapAttribute<? super R, K, V> map);

	@Override
	<K, V> SqmMapJoin<R, K, V> join(MapAttribute<? super R, K, V> map, JoinType jt);

	@Override
	<X, Y> SqmAttributeJoin<X, Y> join(String attributeName);

	@Override
	<X, Y> SqmAttributeJoin<X, Y> join(String attributeName, JoinType jt);

	@Override
	<X, Y> SqmBagJoin<X, Y> joinCollection(String attributeName);

	@Override
	<X, Y> SqmBagJoin<X, Y> joinCollection(String attributeName, JoinType jt);

	@Override
	<X, Y> SqmSetJoin<X, Y> joinSet(String attributeName);

	@Override
	<X, Y> SqmSetJoin<X, Y> joinSet(String attributeName, JoinType jt);

	@Override
	<X, Y> SqmListJoin<X, Y> joinList(String attributeName);

	@Override
	<X, Y> SqmListJoin<X, Y> joinList(String attributeName, JoinType jt);

	@Override
	<X, K, V> SqmMapJoin<X, K, V> joinMap(String attributeName);

	@Override
	<X, K, V> SqmMapJoin<X, K, V> joinMap(String attributeName, JoinType jt);

	@Override
	SqmFrom<L, R> copy(SqmCopyContext context);
}
