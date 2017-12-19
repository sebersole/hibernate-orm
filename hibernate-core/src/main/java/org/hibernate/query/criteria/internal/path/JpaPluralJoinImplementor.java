/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import javax.persistence.metamodel.PluralAttribute;

import org.hibernate.query.criteria.JpaPluralJoin;
import org.hibernate.query.sqm.tree.from.SqmAttributeJoin;

/**
 * Implementor of JpaJoin.
 *
 * @author Christian Beikov
 */
public interface JpaPluralJoinImplementor<Z, C, X> extends JpaPluralJoin<Z, C, X>, JpaJoinImplementor<Z, X> {

	@Override
	@SuppressWarnings("unchecked")
	default PluralAttribute<? super Z, C, X> getModel() {
		return (PluralAttribute<? super Z, C, X>) ((SqmAttributeJoin) this).getAttributeReference().getReferencedNavigable();
	}

}
