/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation;

import org.hibernate.Incubating;
import org.hibernate.engine.jdbc.group.PreparedStatementGroup;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Main contract for performing the mutation.  Accounts for various
 * moving parts such as:<ul>
 *     <li>Should the statements be batched or not?</li>
 *     <li>Should we "logically" group logging of the parameter bindings?</li>
 *     <li>et al.</li>
 * </ul>
 *
 * @author Steve Ebersole
 */
@Incubating
public interface MutationExecutor {
	/**
	 * The collection of prepared-statements for the execution
	 */
	PreparedStatementGroup getStatementGroup();

	/**
	 * Get the delegate to be used to coordinate JDBC parameter binding.
	 */
	ParameterBinder getParameterBinder();

	/**
	 * Perform the execution
	 */
	void execute(SharedSessionContractImplementor session);
}
