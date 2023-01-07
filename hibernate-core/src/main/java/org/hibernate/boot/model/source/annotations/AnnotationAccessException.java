/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.boot.model.source.annotations;

import org.hibernate.HibernateException;

/**
 * Indicates a problem accessing annotation details from the domain model
 *
 * @author Steve Ebersole
 */
public class AnnotationAccessException extends HibernateException {
	public AnnotationAccessException(String message) {
		super( message );
	}

	public AnnotationAccessException(String message, Throwable cause) {
		super( message, cause );
	}
}
