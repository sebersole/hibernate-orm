/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations;

import org.hibernate.Internal;
import org.hibernate.internal.log.SubSystemLogging;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
@SubSystemLogging(
		name = AnnotationSourceLogging.NAME,
		description = "Logging related to Annotation source processing"
)
@Internal
public final class AnnotationSourceLogging {
	public static final String NAME = "hibernate.orm.boot.annotations";

	public static final Logger ANNOTATION_SOURCE_LOGGER = Logger.getLogger( NAME );

	public static final boolean ANNOTATION_SOURCE_LOGGER_TRACE_ENABLED = ANNOTATION_SOURCE_LOGGER.isTraceEnabled();
	public static final boolean ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED = ANNOTATION_SOURCE_LOGGER.isDebugEnabled();
}
