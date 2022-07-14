/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.id.insert;

import java.sql.PreparedStatement;

import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.engine.jdbc.group.PreparedStatementDetails;
import org.hibernate.engine.jdbc.mutation.spi.ParameterBinderImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Responsible for handling delegation relating to variants in how
 * insert-generated-identifier generator strategies dictate processing:<ul>
 * <li>building the sql insert statement
 * <li>determination of the generated identifier value
 * </ul>
 *
 * @author Steve Ebersole
 */
public interface InsertGeneratedIdentifierDelegate {

	/**
	 * Build a {@link org.hibernate.sql.Insert} specific to the delegate's mode
	 * of handling generated key values.
	 *
	 * @param context A context to help generate SQL strings
	 * @return The insert object.
	 */
	IdentifierGeneratingInsert prepareIdentifierGeneratingInsert(SqlStringGenerationContext context);

	PreparedStatement prepareStatement(String insertSql, SharedSessionContractImplementor session);

	Object performInsert(
			PreparedStatementDetails insertStatementDetails,
			ParameterBinderImplementor parameterBinder,
			Object entity,
			SharedSessionContractImplementor session);

	/**
	 * Append SQL specific to the delegate's mode
	 * of handling generated key values.
	 *
	 * @return The insert SQL.
	 */
	default String prepareIdentifierGeneratingInsert(String insertSQL) {
		return insertSQL;
	}

	/**
	 * Perform the indicated insert SQL statement and determine the identifier value
	 * generated.
	 *
	 *
	 * @param insertSQL The INSERT statement string
	 * @param session The session in which we are operating
	 * @param binder The param binder
	 * 
	 * @return The generated identifier value.
	 */
	Object performInsert(String insertSQL, SharedSessionContractImplementor session, Binder binder);

}
