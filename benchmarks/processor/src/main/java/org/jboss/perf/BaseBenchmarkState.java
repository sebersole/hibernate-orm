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

import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

/**
 * @author Andrea Boriero
 */
@State(Scope.Benchmark)
public class BaseBenchmarkState {

	private BenchmarkSession benchmarkSession;

	private BenchmarkHQLParserTree hqlParser;
	private BenchmarkHQLSemanticModelInterpreter hqlInterpreter;

	@Setup
	public void setup() {
		try {
			benchmarkSession = BenchmarkFactory.buildTestSession();
			benchmarkSession.setUp( getAnnotatedClasses(), getHbmFiles(), createSchema() );

			hqlParser = benchmarkSession.getHqlParser();
			hqlInterpreter = benchmarkSession.getHqlInterpreter();
			populateDatabase();
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}

	protected void populateDatabase() {
	}

	protected void cleanUpDatabase() {

	}

	protected Class[] getAnnotatedClasses() {
		return new Class[] {};
	}

	protected String[] getHbmFiles() {
		return new String[] { "benchmark.hbm.xml" };
	}

	protected boolean createSchema() {
		return false;
	}

	public BenchmarkHQLParserTree getHqlParser() {
		return hqlParser;
	}

	public BenchmarkHQLSemanticModelInterpreter getHQLInterpreter() {
		return hqlInterpreter;
	}

	<T> T inTransaction(Function<EntityManager, T> sessionConsumer) {
		return benchmarkSession.inTransaction( sessionConsumer );
	}

	void inTransaction(Consumer<EntityManager> sessionConsumer) {
		benchmarkSession.inTransaction( sessionConsumer );
	}

	@TearDown
	public void shutdown() {
		try {
			cleanUpDatabase();
			benchmarkSession.shutDown();
		}
		catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}
}
