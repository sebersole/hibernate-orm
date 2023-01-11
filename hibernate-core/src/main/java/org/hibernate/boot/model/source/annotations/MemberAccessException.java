/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations;

import org.hibernate.HibernateException;

/**
 * Indicates a problem accessing a {@link java.lang.reflect.Member}
 * or {@link org.hibernate.boot.model.source.annotations.spi.MemberSource}
 *
 * @author Steve Ebersole
 */
public class MemberAccessException extends HibernateException {
	public MemberAccessException(String message) {
		super( message );
	}

	public MemberAccessException(String message, Throwable cause) {
		super( message, cause );
	}
}
