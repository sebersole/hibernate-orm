/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;

/**
 * Abstract ParameterBinder implementation for QueryParameter binding.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractParameterBinder implements JdbcParameterBinder {
	@Override
	public int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			ExecutionContext executionContext) throws SQLException {
		final QueryParameterBinding binding = getBinding( executionContext );
		return bindParameterValue(  statement, startPosition, binding, executionContext );
	}

	protected abstract QueryParameterBinding getBinding(ExecutionContext executionContext);

	@SuppressWarnings("unchecked")
	private int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			QueryParameterBinding valueBinding,
			ExecutionContext executionContext) throws SQLException {
		final AllowableParameterType bindType;
		final Object bindValue;

		if ( valueBinding == null ) {
			warnNoBinding();
			return 1;
		}

		if ( valueBinding.getBindType() == null ) {
			bindType = null;
		}
		else {
			bindType = valueBinding.getBindType();
		}

		bindValue = valueBinding.getBindValue();

		if ( bindType == null ) {
			unresolvedType();
		}
		assert bindType != null;
		if ( bindValue == null ) {
			warnNullBindValue();
		}

		bindType.getValueBinder( executionContext.getSession().getFactory().getTypeConfiguration() )
				.bind( statement, startPosition, bindValue, executionContext );

		return bindType.getNumberOfJdbcParametersNeeded();
	}

	protected abstract void warnNoBinding();

	protected abstract void unresolvedType();

	protected abstract void warnNullBindValue();
}
