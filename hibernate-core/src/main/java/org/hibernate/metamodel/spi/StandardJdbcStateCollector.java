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
public class StandardJdbcStateCollector implements NestableJdbcStateCollector {
	private final Object[] collectedState;

	private int currentPosition;

	public StandardJdbcStateCollector(int collectedStateSize) {
		this.collectedState = new Object[ collectedStateSize ];
	}

	@Override
	public void startingPosition(int currentPosition) {
		assert currentPosition < collectedState.length;
		this.currentPosition = currentPosition;
	}

	@Override
	public Object[] getCollectedState() {
		return collectedState;
	}

	@Override
	public void registerState(Object state) {
		this.collectedState[ currentPosition ] = state;
	}

	@Override
	public void startingSubCollector(JdbcStateCollector subCollector) {
		registerState( subCollector.getCollectedState() );
	}
}
