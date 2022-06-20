/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.mutation;

import org.hibernate.internal.log.SubSystemLogging;

import org.jboss.logging.Logger;

import static org.hibernate.engine.jdbc.mutation.MutationExecutionLogging.NAME;

/**
 * @author Steve Ebersole
 */
@SubSystemLogging(
		name = NAME,
		description = "Logging related to execution of entity and collection mutations"
)
public class MutationExecutionLogging {
	public static final String NAME = SubSystemLogging.BASE + ".jdbc.mutation.exec";

	public static final Logger MUTATION_EXEC_LOGGER = Logger.getLogger( NAME );

	public static final boolean MUTATION_EXEC_TRACE_ENABLED = MUTATION_EXEC_LOGGER.isTraceEnabled();
	public static final boolean MUTATION_EXEC_DEBUG_ENABLED = MUTATION_EXEC_LOGGER.isDebugEnabled();
}
