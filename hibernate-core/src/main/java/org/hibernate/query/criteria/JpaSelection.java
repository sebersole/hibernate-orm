/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria;

import java.util.List;
import javax.persistence.criteria.Selection;

/**
 * Hibernate extensions to the JPA Selection.
 *
 * @author Christian Beikov
 */
public interface JpaSelection<T> extends Selection<T>, JpaTupleElement<T> {
	JpaSelection<T> alias(String name);
	List<JpaSelection<?>> getJpaCompoundSelectionItems();
}
