/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.batch;

import org.hibernate.internal.log.SubSystemLogging;

import org.jboss.logging.Logger;

/**
 * Logging related to JDBC batch execution
 *
 * @author Steve Ebersole
 */
@SubSystemLogging(
		name = JdbcBatchLogging.NAME,
		description = "Logging related to JDBC batch execution"
)
public final class JdbcBatchLogging {
	public static final String NAME = "org.hibernate.orm.jdbc.batch";

	public static final Logger BATCH_LOGGER = Logger.getLogger( NAME );

	public static final boolean BATCH_TRACE_ENABLED = BATCH_LOGGER.isTraceEnabled();
	public static final boolean BATCH_DEBUG_ENABLED = BATCH_LOGGER.isDebugEnabled();
}
