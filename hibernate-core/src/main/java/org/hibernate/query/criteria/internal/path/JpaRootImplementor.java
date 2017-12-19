/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.query.criteria.internal.path;

import javax.persistence.metamodel.EntityType;

import org.hibernate.query.criteria.JpaRoot;

/**
 * Implementor of JpaRoot.
 *
 * @author Christian Beikov
 */
public interface JpaRootImplementor<T> extends JpaRoot<T>, JpaFromImplementor<T, T> {

	@Override
	default EntityType<T> getModel() {
		return getIntrinsicSubclassEntityMetadata();
	}
}
