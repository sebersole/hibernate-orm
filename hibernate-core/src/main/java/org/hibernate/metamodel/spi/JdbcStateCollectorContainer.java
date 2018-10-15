/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.spi;

/**
 * @author Steve Ebersole
 */
public interface JdbcStateCollectorContainer {
	/**
	 * Registers the incoming JDBC state with the current JdbcStateCollector, if one
	 */
	void registerJdbcState(Object state);

	void startingPosition(int currentPosition);

	/**
	 * Called by things that want to collect the JDBC state
	 *
	 * @see NestableJdbcStateCollector
	 */
	void pushCollector(JdbcStateCollector collector);

	/**
	 * Every {@linkplain #pushCollector push} should have a corresponding pop.
	 */
	void popCollector();

	boolean isEmpty();

	int getDepth();
}
