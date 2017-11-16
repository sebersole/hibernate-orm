/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.query.criteria.JpaCollectionJoin;
import org.hibernate.query.criteria.JpaExpression;
import org.hibernate.query.criteria.JpaFetch;
import org.hibernate.query.criteria.JpaFrom;
import org.hibernate.query.criteria.JpaJoin;
import org.hibernate.query.criteria.JpaListJoin;
import org.hibernate.query.criteria.JpaMapJoin;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.JpaSelection;
import org.hibernate.query.criteria.JpaSetJoin;
import org.hibernate.query.criteria.internal.SqmUtils;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.SqmJoinType;
import org.hibernate.query.sqm.tree.expression.domain.SqmAttributeReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableContainerReference;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.query.sqm.tree.from.SqmJoin;

/**
 * Implementor of JpaRoot.
 *
 * @author Christian Beikov
 */
public interface JpaFromImplementor<Z, X> extends JpaFrom<Z, X>, JpaPathImplementor<X>, SqmFrom {

	@Override
	@SuppressWarnings("unchecked")
	default Bindable<X> getModel() {
		return getIntrinsicSubclassEntityMetadata();
	}

	@Override
	default JpaSelection<X> alias(String name) {
		setIdentificationVariable(name);
		return this;
	}

	@Override
	default String getAlias() {
		return getIdentificationVariable();
	}

	@Override
	default <Y> JpaPath<Y> get(SingularAttribute<? super X, Y> attribute) {
		// TODO: implement
		return null;
	}

	@Override
	default <E, C extends Collection<E>> JpaExpression<C> get(PluralAttribute<X, C, E> collection) {
		// TODO: implement
		return null;
	}

	@Override
	default <K, V, M extends Map<K, V>> JpaExpression<M> get(MapAttribute<X, K, V> map) {
		// TODO: implement
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	default <Y> JpaPath<Y> get(String attributeName) {
		Attribute<X, Y> attribute = getIntrinsicSubclassEntityMetadata()
				.getAttribute( attributeName );
		// TODO: implement
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	default Set<JpaJoin<X, ?>> getJpaJoins() {
		List<SqmJoin> sqmJoins = getContainingSpace().getJoins();
		Set<JpaJoin<X, ?>> joins = new HashSet<>( sqmJoins.size() );
		for ( SqmJoin sqmJoin : sqmJoins ) {
			if ( sqmJoin instanceof SqmAttributeJoin && ( (SqmAttributeJoin) sqmJoin ).getLhs() == this
					&& !( (SqmAttributeJoin) sqmJoin ).isFetched() ) {
				joins.add( (JpaJoin<X, ?>) sqmJoin );
			}
		}
		return joins;
	}

	@Override
	@SuppressWarnings("unchecked")
	default Set<Join<X, ?>> getJoins() {
		return (Set<Join<X, ?>>) (Set<?>) getJpaJoins();
	}

	JpaFrom<Z, X> getCorrelationParent();

	@Override
	@SuppressWarnings("unchecked")
	default Set<JpaFetch<X, ?>> getJpaFetches() {
		List<SqmJoin> sqmJoins = getContainingSpace().getJoins();
		Set<JpaFetch<X, ?>> joins = new HashSet<>( sqmJoins.size() );
		for ( SqmJoin sqmJoin : sqmJoins ) {
			if ( sqmJoin instanceof SqmAttributeJoin && ( (SqmAttributeJoin) sqmJoin ).getLhs() == this
					&& ( (SqmAttributeJoin) sqmJoin ).isFetched() ) {
				joins.add( (JpaFetch<X, ?>) sqmJoin );
			}
		}
		return joins;
	}

	@Override
	default <Y> JpaFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute) {
		return fetch( attribute, JoinType.INNER );
	}

	@Override
	default <Y> JpaFetch<X, Y> fetch(SingularAttribute<? super X, Y> attribute, JoinType jt) {
		JpaJoin<X, Y> join = join( attribute, jt );
		join.setFetch( true );
		return (JpaFetch<X, Y>) join;
	}

	@Override
	default <Y> JpaFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute) {
		return fetch( attribute, JoinType.INNER );
	}

	@Override
	default <Y> JpaFetch<X, Y> fetch(PluralAttribute<? super X, ?, Y> attribute, JoinType jt) {
		JpaJoin<X, Y> join;
		PluralAttribute.CollectionType collectionType = attribute.getCollectionType();
		switch ( collectionType ) {
			case COLLECTION: join = join( (CollectionAttribute) attribute, jt );
				break;
			case LIST: join = join( (ListAttribute) attribute, jt );
				break;
			case SET: join = join( (SetAttribute) attribute, jt );
				break;
			case MAP: join = join( (MapAttribute) attribute, jt );
				break;
			default:
				throw new IllegalArgumentException( "Illegal collection type:" + collectionType );
		}
		join.setFetch( true );
		return (JpaFetch<X, Y>) join;
	}

	@Override
	default <X1, Y> JpaFetch<X1, Y> fetch(String attributeName) {
		return fetch( attributeName, JoinType.INNER );
	}

	@Override
	@SuppressWarnings("unchecked")
	default <X1, Y> JpaFetch<X1, Y> fetch(
			String attributeName, JoinType jt) {
		Attribute<X, Y> attribute = getIntrinsicSubclassEntityMetadata()
				.getAttribute( attributeName );
		if ( attribute.isCollection() ) {
			return fetch( (PluralAttribute) attribute, jt );
		} else {
			return fetch( (SingularAttribute) attribute, jt );
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	default Set<Fetch<X, ?>> getFetches() {
		return (Set<Fetch<X, ?>>) (Set<?>) getJpaFetches();
	}

	@Override
	default <Y> JpaJoin<X, Y> join(SingularAttribute<? super X, Y> attribute) {
		return join( attribute, JoinType.INNER );
	}

	@Override
	default <Y> JpaJoin<X, Y> join(SingularAttribute<? super X, Y> attribute, JoinType jt) {
		ParsingContext parsingContext = context().parsingContext();
		SqmAttributeReference binding = (SqmAttributeReference) parsingContext.findOrCreateNavigableBinding(
				(SqmNavigableContainerReference) getNavigableReference(),
				attribute.getName()
		);
		String uid = parsingContext.makeUniqueIdentifier();
		String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		EntityDescriptor subclassDescriptor = binding.getIntrinsicSubclassEntityMetadata();
		SqmJoinType joinType = SqmUtils.getSqmJoinType( jt );
		@SuppressWarnings("unchecked")
		JpaJoin<X, Y> join = new JpaJoinImpl(
				this,
				binding,
				uid,
				alias,
				subclassDescriptor,
				joinType,
				false,
				context()
		);
		getContainingSpace().addJoin( (SqmJoin) join );
		return join;
	}

	@Override
	default <Y> JpaCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection) {
		return join( collection, JoinType.INNER );
	}

	@Override
	default <Y> JpaCollectionJoin<X, Y> join(CollectionAttribute<? super X, Y> collection, JoinType jt) {
		ParsingContext parsingContext = context().parsingContext();
		SqmAttributeReference binding = (SqmAttributeReference) parsingContext.findOrCreateNavigableBinding(
				(SqmNavigableContainerReference) getNavigableReference(),
				collection.getName()
		);
		String uid = parsingContext.makeUniqueIdentifier();
		String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		EntityDescriptor subclassDescriptor = binding.getIntrinsicSubclassEntityMetadata();
		SqmJoinType joinType = SqmUtils.getSqmJoinType( jt );
		@SuppressWarnings("unchecked")
		JpaCollectionJoin<X, Y> join = new JpaCollectionJoinImpl(
				this,
				binding,
				uid,
				alias,
				subclassDescriptor,
				joinType,
				false,
				context()
		);
		getContainingSpace().addJoin( (SqmJoin) join );
		return join;
	}

	@Override
	default <Y> JpaSetJoin<X, Y> join(SetAttribute<? super X, Y> set) {
		return join( set, JoinType.INNER );
	}

	@Override
	default <Y> JpaSetJoin<X, Y> join(SetAttribute<? super X, Y> set, JoinType jt) {
		ParsingContext parsingContext = context().parsingContext();
		SqmAttributeReference binding = (SqmAttributeReference) parsingContext.findOrCreateNavigableBinding(
				(SqmNavigableContainerReference) getNavigableReference(),
				set.getName()
		);
		String uid = parsingContext.makeUniqueIdentifier();
		String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		EntityDescriptor subclassDescriptor = binding.getIntrinsicSubclassEntityMetadata();
		SqmJoinType joinType = SqmUtils.getSqmJoinType( jt );
		@SuppressWarnings("unchecked")
		JpaSetJoin<X, Y> join = new JpaSetJoinImpl(
				this,
				binding,
				uid,
				alias,
				subclassDescriptor,
				joinType,
				false,
				context()
		);
		getContainingSpace().addJoin( (SqmJoin) join );
		return join;
	}

	@Override
	default <Y> JpaListJoin<X, Y> join(ListAttribute<? super X, Y> list) {
		return join( list, JoinType.INNER );
	}

	@Override
	default <Y> JpaListJoin<X, Y> join(ListAttribute<? super X, Y> list, JoinType jt) {
		ParsingContext parsingContext = context().parsingContext();
		SqmAttributeReference binding = (SqmAttributeReference) parsingContext.findOrCreateNavigableBinding(
				(SqmNavigableContainerReference) getNavigableReference(),
				list.getName()
		);
		String uid = parsingContext.makeUniqueIdentifier();
		String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		EntityDescriptor subclassDescriptor = binding.getIntrinsicSubclassEntityMetadata();
		SqmJoinType joinType = SqmUtils.getSqmJoinType( jt );
		@SuppressWarnings("unchecked")
		JpaListJoin<X, Y> join = new JpaListJoinImpl(
				this,
				binding,
				uid,
				alias,
				subclassDescriptor,
				joinType,
				false,
				context()
		);
		getContainingSpace().addJoin( (SqmJoin) join );
		return join;
	}

	@Override
	default <K, V> JpaMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map) {
		return join( map, JoinType.INNER );
	}

	@Override
	default <K, V> JpaMapJoin<X, K, V> join(MapAttribute<? super X, K, V> map, JoinType jt) {
		ParsingContext parsingContext = context().parsingContext();
		SqmAttributeReference binding = (SqmAttributeReference) parsingContext.findOrCreateNavigableBinding(
				(SqmNavigableContainerReference) getNavigableReference(),
				map.getName()
		);
		String uid = parsingContext.makeUniqueIdentifier();
		String alias = parsingContext.getImplicitAliasGenerator().buildUniqueImplicitAlias();
		EntityDescriptor subclassDescriptor = binding.getIntrinsicSubclassEntityMetadata();
		SqmJoinType joinType = SqmUtils.getSqmJoinType( jt );
		@SuppressWarnings("unchecked")
		JpaMapJoin<X, K, V> join = new JpaMapJoinImpl(
				this,
				binding,
				uid,
				alias,
				subclassDescriptor,
				joinType,
				false,
				context()
		);
		getContainingSpace().addJoin( (SqmJoin) join );
		return join;
	}


	//String-based:

	@Override
	default <X, Y> JpaJoin<X, Y> join(String attributeName) {
		return join( attributeName, JoinType.INNER );
	}

	@Override
	default <X, Y> JpaCollectionJoin<X, Y> joinCollection(String attributeName) {
		return joinCollection( attributeName, JoinType.INNER );
	}

	@Override
	default <X, Y> JpaSetJoin<X, Y> joinSet(String attributeName) {
		return joinSet( attributeName, JoinType.INNER );
	}

	@Override
	default <X, Y> JpaListJoin<X, Y> joinList(String attributeName) {
		return joinList( attributeName, JoinType.INNER );
	}

	@Override
	default <X, K, V> JpaMapJoin<X, K, V> joinMap(String attributeName) {
		return joinMap( attributeName, JoinType.INNER );
	}

	@Override
	@SuppressWarnings("unchecked")
	default <X, Y> JpaJoin<X, Y> join(String attributeName, JoinType jt) {
		Attribute<X, Y> attribute = getIntrinsicSubclassEntityMetadata().getAttribute( attributeName );
		if ( attribute.isCollection() ) {
			PluralAttribute.CollectionType collectionType = ( (PluralAttribute<?, ?, ?>) attribute ).getCollectionType();
			switch ( collectionType ) {
				case COLLECTION: return join( (CollectionAttribute) attribute, jt );
				case LIST: return join( (ListAttribute) attribute, jt );
				case SET: return join( (SetAttribute) attribute, jt );
				case MAP: return join( (MapAttribute) attribute, jt );
			}
			throw new IllegalArgumentException( "Illegal collection type:" + collectionType );
		} else {
			return join( (SingularAttribute) attribute, jt );
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	default <X, Y> JpaCollectionJoin<X, Y> joinCollection(String attributeName, JoinType jt) {
		CollectionAttribute attribute = getIntrinsicSubclassEntityMetadata()
				.getCollection( attributeName );
		return join( attribute, jt );
	}

	@Override
	@SuppressWarnings("unchecked")
	default <X, Y> JpaSetJoin<X, Y> joinSet(String attributeName, JoinType jt) {
		SetAttribute attribute = getIntrinsicSubclassEntityMetadata()
				.getSet( attributeName );
		return join( attribute, jt );
	}

	@Override
	@SuppressWarnings("unchecked")
	default <X, Y> JpaListJoin<X, Y> joinList(String attributeName, JoinType jt) {
		ListAttribute attribute = getIntrinsicSubclassEntityMetadata()
				.getList( attributeName );
		return join( attribute, jt );
	}

	@Override
	@SuppressWarnings("unchecked")
	default <X, K, V> JpaMapJoin<X, K, V> joinMap(String attributeName, JoinType jt) {
		MapAttribute attribute = getIntrinsicSubclassEntityMetadata()
				.getMap( attributeName );
		return join( attribute, jt );
	}
}
