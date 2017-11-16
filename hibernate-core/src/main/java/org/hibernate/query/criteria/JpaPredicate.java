/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.List;
import javax.persistence.criteria.Predicate;

/**
 * Hibernate extensions to the JPA Predicate.
 *
 * @author Christian Beikov
 */
public interface JpaPredicate extends Predicate, JpaExpression<Boolean> {
	List<JpaExpression<Boolean>> getJpaExpressions();

	JpaPredicate not();
}
