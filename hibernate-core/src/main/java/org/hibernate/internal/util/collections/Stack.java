/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.internal.util.collections;

/**
 * @author Steve Ebersole
 */
public interface Stack<T> {
	void push(T newCurrent);

	T pop();

	T getCurrent();

	T getPrevious();

	int depth();

	boolean isEmpty();

	void clear();
}
