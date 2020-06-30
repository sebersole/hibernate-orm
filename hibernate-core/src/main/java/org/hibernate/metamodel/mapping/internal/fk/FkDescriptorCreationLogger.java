/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import org.hibernate.metamodel.mapping.MappingModelCreationLogger;

import org.jboss.logging.Logger;

/**
 * Logger for FK descriptor creation
 *
 * @author Steve Ebersole
 */
public interface FkDescriptorCreationLogger {
	Logger LOGGER = MappingModelCreationLogger.subLogger( "fk" );

	boolean DEBUG_ENABLED = LOGGER.isDebugEnabled();
	boolean TRACE_ENABLED = LOGGER.isTraceEnabled();
}
