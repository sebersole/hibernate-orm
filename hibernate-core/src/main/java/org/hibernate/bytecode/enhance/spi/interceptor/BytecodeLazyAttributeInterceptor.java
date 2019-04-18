/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.bytecode.enhance.spi.interceptor;

import org.hibernate.bytecode.enhance.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.PersistentAttributeInterceptor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Steve Ebersole
 */
public interface BytecodeLazyAttributeInterceptor
		extends PersistentAttributeInterceptor, LazyPropertyInitializer.InterceptorImplementor {
	SharedSessionContractImplementor getLinkedSession();

	boolean allowLoadOutsideTransaction();

	String getSessionFactoryUuid();

	void setSession(SharedSessionContractImplementor session);

	void unsetSession();
}
