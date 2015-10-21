/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.util;

import org.jboss.jandex.MethodInfo;

/**
 * Groups together the methods for JPA callbacks.
 *
 * @author Steve Ebersole
 */
public interface CallbackMethodGroup {
	MethodInfo getPrePersistCallbackMethod();

	MethodInfo getPreRemoveCallbackMethod();

	MethodInfo getPreUpdateCallbackMethod();

	MethodInfo getPostLoadCallbackMethod();

	MethodInfo getPostPersistCallbackMethod();

	MethodInfo getPostRemoveCallbackMethod();

	MethodInfo getPostUpdateCallbackMethod();
}
