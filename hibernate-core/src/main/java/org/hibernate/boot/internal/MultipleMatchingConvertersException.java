/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.internal;

import org.hibernate.HibernateException;

/**
 * Indicates that multiple matching AttributeConverters were found
 *
 * @author Steve Ebersole
 */
public class MultipleMatchingConvertersException extends HibernateException {
	public MultipleMatchingConvertersException(String message) {
		super( message );
	}

	public MultipleMatchingConvertersException(String message, Throwable cause) {
		super( message, cause );
	}
}
