/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping;

import org.hibernate.HibernateException;

/**
 * A problem creating Hibernate's mapping model
 *
 * @author Steve Ebersole
 */
public class MappingModelCreationException extends HibernateException {
	public MappingModelCreationException(String message) {
		super( message );
	}

	public MappingModelCreationException(String message, Throwable cause) {
		super( message, cause );
	}
}
