/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.group;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.ValueBinder;

/**
 * @author Steve Ebersole
 */
public class StandardParameterStrategy implements ParameterStrategy {
	/**
	 * Singleton access
	 */
	public static final StandardParameterStrategy STANDARD_PARAMETER_BINDING_STRATEGY = new StandardParameterStrategy();
	public static final BinderImpl STANDARD_BINDER = new BinderImpl();

	@Override
	public Binder resolveBinder(String tableName, int numberOfParameters) {
		return STANDARD_BINDER;
	}

	@Override
	public void finishUpRow(SharedSessionContractImplementor session) {
		// nothing to do
	}

	private static class BinderImpl implements Binder {
		/**
		 * Singleton access
		 */
		@Override
		public void bindParameter(
				Object value,
				ValueBinder<Object> valueBinder,
				int position,
				PreparedStatement statement,
				SharedSessionContractImplementor session) {
			try {
				valueBinder.bind( statement, value, position, session );
			}
			catch (SQLException e) {
				throw session.getJdbcServices().getSqlExceptionHelper().convert(
						e,
						String.format(
								Locale.ROOT,
								"Unable to bind parameter #%s - %s",
								position,
								value
						)
				);
			}
		}
	}
}
