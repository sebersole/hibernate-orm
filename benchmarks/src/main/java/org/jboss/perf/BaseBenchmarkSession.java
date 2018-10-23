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
import javax.persistence.EntityManagerFactory;

/**
 * @author Andrea Boriero
 */
public abstract class BaseBenchmarkSession implements BenchmarkSession {
	protected Class[] annotatedClasses;
	protected String[] hbmfiles = new String[0];
	protected boolean createSchema;
	protected EntityManagerFactory entityManagerFactory;

	private BenchmarkHQLParserTree hqlParser;
	private BenchmarkHQLSemanticModelInterpreter hqlInterpreter;

	@Override
	public void setUp(Class[] annotatedClasses, String[] hbmfiles, boolean createSchema) {
		this.annotatedClasses = annotatedClasses;
		this.hbmfiles = hbmfiles;
		this.createSchema = createSchema;
		this.entityManagerFactory = buildEntityManagerFactory();
		this.hqlParser = buildHqlParser();
		hqlInterpreter = buildHqInterpreter();

		System.out.println( "Running benchmark with HQL Parser: " + hqlParser.getClass().getName() );
		System.out.println( "Running benchmark with HQL Interpreter: " + hqlInterpreter.getClass().getName() );

	}

	protected abstract EntityManagerFactory buildEntityManagerFactory();

	protected abstract BenchmarkHQLParserTree buildHqlParser();

	protected abstract BenchmarkHQLSemanticModelInterpreter buildHqInterpreter();

	@Override
	public BenchmarkHQLParserTree getHqlParser() {
		return hqlParser;
	}

	@Override
	public BenchmarkHQLSemanticModelInterpreter getHqlInterpreter() {
		return hqlInterpreter;
	}

	@Override
	public <T> T inTransaction(Function<EntityManager, T> function) {
		EntityManager em = entityManagerFactory.createEntityManager();
		T result = null;
		try {
			em.getTransaction().begin();
			result = function.apply( em );
			em.getTransaction().commit();
		}
		catch (Exception e) {
			if ( em.getTransaction().isActive() ) {
				em.getTransaction().rollback();
			}
		}
		finally {
			em.close();
		}
		return result;
	}

	@Override
	public void inTransaction(Consumer<EntityManager> consumer) {
		EntityManager em = entityManagerFactory.createEntityManager();
		try {
			em.getTransaction().begin();
			consumer.accept( em );
			em.getTransaction().commit();
		}
		catch (Exception e) {
			if ( em.getTransaction().isActive() ) {
				em.getTransaction().rollback();
			}
		}
		finally {
			em.close();
		}
	}

	@Override
	public void shutDown() {
		entityManagerFactory.close();
	}


}
