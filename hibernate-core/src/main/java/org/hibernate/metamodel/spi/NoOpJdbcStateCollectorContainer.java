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
public class NoOpJdbcStateCollectorContainer implements JdbcStateCollectorContainer {
	/**
	 * Singleton access
	 */
	public static final NoOpJdbcStateCollectorContainer INSTANCE = new NoOpJdbcStateCollectorContainer();

	@Override
	public void registerJdbcState(Object state) {
		// nothing to do
	}

	@Override
	public void startingPosition(int currentPosition) {
		// nothing to do
	}

	@Override
	public void pushCollector(JdbcStateCollector collector) {
		// nothing to do
	}

	@Override
	public void popCollector() {
		// nothing to do
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public int getDepth() {
		return 0;
	}
}
