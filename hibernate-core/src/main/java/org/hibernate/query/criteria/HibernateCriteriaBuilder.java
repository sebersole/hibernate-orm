/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import javax.persistence.criteria.CriteriaBuilder;

/**
 * Hibernate extensions to the JPA CriteriaBuilder.  Currently there are no extensions;
 * these are coming in 6.0 - see https://hibernate.atlassian.net/browse/HHH-11115
 *
 * @author Steve Ebersole
 */
public interface HibernateCriteriaBuilder extends CriteriaBuilder {
	// in-flight ideas:
	//		* operator corresponding to the new "matches" HQL operator
	//		* match for our expanded dynamic-instantiation support
	//		* ?generic support for SQL restrictions? - ala Restrictions.sqlRestriction
	//		* port query-by-example support - org.hibernate.criterion.Example
}
