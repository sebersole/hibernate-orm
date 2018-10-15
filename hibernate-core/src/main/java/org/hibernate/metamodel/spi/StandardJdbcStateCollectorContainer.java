/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.spi;

import org.hibernate.internal.util.collections.Stack;
import org.hibernate.internal.util.collections.StandardStack;

/**
 * @author Steve Ebersole
 */
public class StandardJdbcStateCollectorContainer implements JdbcStateCollectorContainer {

	private final Stack<JdbcStateCollector> jdbcStateCollectorStack = new StandardStack<>();

	@Override
	public void registerJdbcState(Object state) {
		if ( jdbcStateCollectorStack.isEmpty() ) {
			return;
		}

		jdbcStateCollectorStack.getCurrent().registerState( state );
	}

	@Override
	public void startingPosition(int currentPosition) {
		if ( jdbcStateCollectorStack.isEmpty() ) {
			return;
		}

		jdbcStateCollectorStack.getCurrent().startingPosition( currentPosition );
	}

	@Override
	public void pushCollector(JdbcStateCollector collector) {
		if ( ! jdbcStateCollectorStack.isEmpty() ) {
			final JdbcStateCollector current = jdbcStateCollectorStack.getCurrent();
			if ( current instanceof NestableJdbcStateCollector ) {
				( (NestableJdbcStateCollector) current ).startingSubCollector( collector );
			}
		}

		jdbcStateCollectorStack.push( collector );
	}

	@Override
	public void popCollector() {
		assert ! jdbcStateCollectorStack.isEmpty();

		jdbcStateCollectorStack.pop();
	}

	@Override
	public boolean isEmpty() {
		return jdbcStateCollectorStack.isEmpty();
	}

	@Override
	public int getDepth() {
		return jdbcStateCollectorStack.depth();
	}
}
