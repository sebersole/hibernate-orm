/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.exec.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.spi.TypeConfiguration;

import org.jboss.logging.Logger;

/**
 * Implementation of JdbcParameterBinder for JdbcCall handling.  HQL and Criteria
 * define JdbcParameterBinder themselves.
 *
 * @author Steve Ebersole
 */
public class JdbcCallParameterBinderImpl implements JdbcParameterBinder {
	private static final Logger log = Logger.getLogger( JdbcCallParameterBinderImpl.class );

	private final String callName;
	private final String parameterName;
	private final int parameterPosition;
	private final AllowableParameterType ormType;

	public JdbcCallParameterBinderImpl(
			String callName,
			String parameterName,
			int parameterPosition,
			AllowableParameterType ormType) {
		this.callName = callName;
		this.parameterName = parameterName;
		this.parameterPosition = parameterPosition;
		this.ormType = ormType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public int bindParameterValue(
			PreparedStatement statement,
			int startPosition,
			ExecutionContext executionContext) throws SQLException {
		final QueryParameterBinding binding;
		if ( parameterName != null ) {
			binding = executionContext.getParameterBindingContext().getQueryParameterBindings().getBinding( parameterName );
		}
		else {
			binding = executionContext.getParameterBindingContext().getQueryParameterBindings().getBinding( parameterPosition );
		}

		if ( binding == null ) {
			// the user did not bind a value to the parameter...
			log.debugf(
					"Stored procedure [%s] IN/INOUT parameter [%s] not bound; skipping binding (assuming procedure defines default value)",
					callName,
					parameterName == null ? Integer.toString( parameterPosition ) : parameterName
			);
		}
		else  {
			final Object bindValue = binding.getBindValue();

			if ( bindValue == null ) {
				log.debugf(
						"Binding NULL to IN/INOUT parameter [%s] for stored procedure `%s`",
						parameterName == null ? Integer.toString( parameterPosition ) : parameterName,
						callName
				);
			}
			else {
				log.debugf(
						"Binding [%s] to IN/INOUT parameter [%s] for stored procedure `%s`",
						bindValue,
						parameterName == null ? Integer.toString( parameterPosition ) : parameterName,
						callName
				);
			}

			// for the time being we assume the param is basic-type.  See discussion in other
			//		ParameterBinder impls

			final SessionFactoryImplementor factory = executionContext.getSession().getFactory();
			final TypeConfiguration typeConfiguration = factory.getTypeConfiguration();
			final ValueBinder valueBinder = ormType.getValueBinder( typeConfiguration );
			if ( parameterName != null ) {
				valueBinder.bind(
						statement,
						parameterName,
						bindValue,
						executionContext
				);
			}
			else {
				valueBinder.bind(
						statement,
						startPosition,
						bindValue,
						executionContext
				);
			}
		}

		return ormType.getNumberOfJdbcParametersNeeded();
	}
}
