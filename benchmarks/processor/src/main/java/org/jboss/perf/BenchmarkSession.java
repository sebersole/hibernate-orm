/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.jboss.perf;

import java.util.function.Consumer;
import java.util.function.Function;
import javax.persistence.EntityManager;

/**
 * @author Andrea Boriero
 */
public interface BenchmarkSession {
	void setUp(Class[] annotatedClasses,String[] hbmfiles, boolean createSchema);

	BenchmarkHQLParserTree getHqlParser();

	BenchmarkHQLSemanticModelInterpreter getHqlInterpreter();

	<T> T inTransaction(Function<EntityManager, T> function);

	void inTransaction(Consumer<EntityManager> consumer);

	void shutDown();
}

