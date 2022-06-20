/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.group;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.ValueBinder;

/**
 * @author Steve Ebersole
 */
public class LoggingOrderParameterStrategy implements ParameterStrategy {
	private final Map<String, BinderImpl> binders = new HashMap<>();

	@Override
	public Binder resolveBinder(String tableName, int numberOfParameters) {
		final BinderImpl existing = binders.get( tableName );
		if ( existing != null ) {
			return existing;
		}

		final BinderImpl created = new BinderImpl( numberOfParameters );
		binders.put( tableName, created );
		return created;
	}

	@Override
	public void finishUpRow(SharedSessionContractImplementor session) {
		//noinspection CodeBlock2Expr
		binders.forEach( (tableName, binder) -> {
			binder.finishUpRow( session );
		} );

		binders.clear();
	}

	private static class Binding {
		private final Object value;
		private final ValueBinder<Object> valueBinder;
		private final int position;
		private final PreparedStatement statement;

		public Binding(Object value, ValueBinder<Object> valueBinder, int position, PreparedStatement statement) {
			this.value = value;
			this.valueBinder = valueBinder;
			this.position = position;
			this.statement = statement;
		}

		@Override
		public int hashCode() {
			return position;
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null || getClass() != o.getClass() ) {
				return false;
			}
			return position == ( (Binding) o ).position;
		}
	}

	private static class BinderImpl implements Binder {
		private final Set<Binding> bindings;

		public BinderImpl(int numberOfParameters) {
			this.bindings = new HashSet<>();
		}

		@Override
		public void bindParameter(
				Object value,
				ValueBinder<Object> valueBinder,
				int position,
				PreparedStatement statement,
				SharedSessionContractImplementor session) {
			bindings.add( new Binding( value, valueBinder, position, statement ) );
		}

		public void finishUpRow(SharedSessionContractImplementor session) {
			for ( Binding binding : bindings ) {
				try {
					binding.valueBinder.bind( binding.statement, binding.value, binding.position, session );
				}
				catch (SQLException e) {
					throw session.getJdbcServices().getSqlExceptionHelper().convert(
							e,
							String.format(
									Locale.ROOT,
									"Unable to bind parameter #%s - %s",
									binding.position,
									binding.value
							)
					);
				}
			}

			bindings.clear();
		}
	}
}
