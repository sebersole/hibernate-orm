/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations;

import org.hibernate.Internal;
import org.hibernate.internal.log.SubSystemLogging;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

import static org.jboss.logging.Logger.Level.INFO;

/**
 * todo (annotation-source) : find the proper min/max id range
 * @author Steve Ebersole
 */
@MessageLogger( projectCode = "HHH" )
@ValidIdRange( min = 999901, max = 999999 )
@SubSystemLogging(
		name = AnnotationSourceLogging.NAME,
		description = "Logging related to Annotation source processing"
)
@Internal
public interface AnnotationSourceLogging extends BasicLogger {
	String NAME = "hibernate.orm.boot.annotations";

	Logger ANNOTATION_SOURCE_LOGGER = Logger.getLogger( NAME );
	AnnotationSourceLogging ANNOTATION_SOURCE_MSG_LOGGER = Logger.getMessageLogger( AnnotationSourceLogging.class, NAME );

	boolean ANNOTATION_SOURCE_LOGGER_TRACE_ENABLED = ANNOTATION_SOURCE_LOGGER.isTraceEnabled();
	boolean ANNOTATION_SOURCE_LOGGER_DEBUG_ENABLED = ANNOTATION_SOURCE_LOGGER.isDebugEnabled();

	@LogMessage(level = INFO)
	@Message( id = 999901, value = "Entity `%s` used both @DynamicInsert and @SQLInsert" )
	void dynamicAndCustomInsert(String entityName);

	@LogMessage(level = INFO)
	@Message( id = 999902, value = "Entity `%s` used both @DynamicUpdate and @SQLUpdate" )
	void dynamicAndCustomUpdate(String entityName);
}
