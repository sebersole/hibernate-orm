/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.jboss.perf;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public abstract class AbstractBenchmarkHQLSemanticModelInterpreter implements BenchmarkHQLSemanticModelInterpreter {
	protected EntityManagerFactory entityManagerFactory;

	protected static MethodHandle getMethodHandle(Class<?> theClass, String methodName, Class<?>... arguments) {
		try {
			Method theMethod = theClass.getDeclaredMethod( methodName, arguments );
			theMethod.setAccessible( true );
			return MethodHandles.lookup().unreflect( theMethod );
		} catch ( NoSuchMethodException | IllegalAccessException e ) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void configure(EntityManagerFactory entityManager) {
		entityManagerFactory = entityManager;
	}
}
