/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.SingularAttribute;

import org.hibernate.query.criteria.JpaCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaUpdate;
import org.hibernate.query.criteria.JpaPath;
import org.hibernate.query.criteria.JpaRoot;
import org.hibernate.query.criteria.internal.path.JpaRootImpl;
import org.hibernate.query.sqm.produce.spi.ParsingContext;
import org.hibernate.query.sqm.tree.expression.SqmExpression;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReference;
import org.hibernate.query.sqm.tree.from.SqmFromElementSpace;
import org.hibernate.query.sqm.tree.from.SqmRoot;
import org.hibernate.query.sqm.tree.predicate.SqmWhereClause;
import org.hibernate.query.sqm.tree.set.SqmAssignment;
import org.hibernate.query.sqm.tree.set.SqmSetClause;

public class CriteriaUpdateImpl<T> extends CommonAbstractCriteriaImpl implements JpaCriteriaUpdate<T> {

	private final SqmWhereClause whereClause;
	private final SqmSetClause setClause;
	private JpaRootImpl<T> root;

	public CriteriaUpdateImpl(JpaCriteriaBuilder builder, ParsingContext parsingContext) {
		super( builder, parsingContext );
		this.whereClause = new SqmWhereClause();
		this.setClause = new SqmSetClause();
	}

	@Override
	@SuppressWarnings("unchecked")
	public JpaRoot<T> from(Class entityClass) {
		JpaRootImpl<T> root = createRoot( entityClass );
		this.root = root;
		return root;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JpaRoot<T> from(EntityType entity) {
		JpaRootImpl<T> root = createRoot( entity );
		this.root = root;
		return root;
	}

	@Override
	@SuppressWarnings("unchecked")
	public JpaRoot<T> getRoot() {
		return root;
	}

	@Override
	public JpaCriteriaUpdate<T> set(
			SingularAttribute attribute, Object value) {
		SqmNavigableReference attributeReference = getAttributeReference( root, attribute.getName() );
		return set( attributeReference, literal( attributeReference, value ) );
	}

	@Override
	public JpaCriteriaUpdate<T> set(
			SingularAttribute attribute, Expression value) {
		SqmNavigableReference attributeReference = getAttributeReference( root, attribute.getName() );
		return set( attribute,  literal( attributeReference, value ) );
	}

	@Override
	public JpaCriteriaUpdate<T> set(Path attribute, Object value) {
		SqmNavigableReference attributeReference = ( (JpaPath<?>) attribute ).getSqmNavigableReference();
		return set( attributeReference, literal( attributeReference, value ) );
	}

	@Override
	public JpaCriteriaUpdate<T> set(Path attribute, Expression value) {
		SqmNavigableReference attributeReference = ( (JpaPath<?>) attribute ).getSqmNavigableReference();
		return set( attributeReference, (SqmExpression) value );
	}

	@Override
	public JpaCriteriaUpdate<T> set(String attributeName, Object value) {
		SqmNavigableReference attributeReference = getAttributeReference( root, attributeName );
		return set( attributeReference, literal( attributeReference, value ) );
	}

	private SqmExpression literal(SqmNavigableReference attributeReference, Object value) {
		if ( value == null ) {
			return (SqmExpression) criteriaBuilder.nullLiteral( attributeReference.getJavaType() );
		} else {
			return (SqmExpression) criteriaBuilder.literal( value );
		}
	}

	private JpaCriteriaUpdate<T> set(SqmNavigableReference stateField, SqmExpression value) {
		setClause.addAssignment( new SqmAssignment( (SqmSingularAttributeReference) stateField, value ) );
		return this;
	}

	@Override
	public JpaCriteriaUpdate<T> where(Expression<Boolean> restriction) {
		setWhere( whereClause, restriction );
		return this;
	}

	@Override
	public JpaCriteriaUpdate<T> where(Predicate... restrictions) {
		setWhere( whereClause, restrictions );
		return this;
	}

	@Override
	protected SqmWhereClause getWhereClause() {
		return whereClause;
	}

	@Override
	protected SqmFromElementSpace getFromElementSpace() {
		return null;
	}
}
