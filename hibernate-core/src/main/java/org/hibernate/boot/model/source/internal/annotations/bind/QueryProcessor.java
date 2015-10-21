/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations.bind;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.persistence.LockModeType;
import javax.persistence.ParameterMode;

import org.hibernate.AnnotationException;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.annotations.CacheModeType;
import org.hibernate.annotations.FlushModeType;
import org.hibernate.annotations.QueryHints;
import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.EnumConversionHelper;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.cfg.annotations.NamedProcedureCallDefinition;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryRootReturn;
import org.hibernate.engine.spi.NamedQueryDefinitionBuilder;
import org.hibernate.engine.spi.NamedSQLQueryDefinitionBuilder;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.LockModeConverter;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.Type;

/**
 * Handles processing of named queries defined via:<ul>
 *     <li>
 *       {@link javax.persistence.NamedQuery} (and
 *       {@link javax.persistence.NamedQueries})
 *     </li>
 *     <li>
 *         {@link javax.persistence.NamedNativeQuery} (and
 *         {@link javax.persistence.NamedNativeQueries})
 *     </li>
 *     <li>
 *         {@link javax.persistence.NamedStoredProcedureQuery} (and
 *         {@link javax.persistence.NamedStoredProcedureQueries})
 *     </li>
 *     <li>
 *         {@link org.hibernate.annotations.NamedQuery} (and
 *         {@link org.hibernate.annotations.NamedQueries})
 *     </li>
 *     <li>
 *         {@link org.hibernate.annotations.NamedNativeQuery} (and
 *         {@link org.hibernate.annotations.NamedNativeQueries})
 *     </li>
 * </ul>
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class QueryProcessor {
	private static final CoreMessageLogger log = CoreLogging.messageLogger( QueryProcessor.class );

	/**
	 * Disallow direct instantiation
	 */
	private QueryProcessor() {
	}

	/**
	 * Main entry point into named query processing
	 *
	 * @param bindingContext the context for annotation binding
	 */
	public static void bind(RootAnnotationBindingContext bindingContext) {
		// JPA @NamedQuery
		Collection<AnnotationInstance> annotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				JpaDotNames.NAMED_QUERY,
				JpaDotNames.NAMED_QUERIES
		);
		for ( AnnotationInstance namedQueryAnnotation : annotations ) {
			bindNamedQuery( namedQueryAnnotation, bindingContext );
		}

		// Hibernate @NamedQuery
		annotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				HibernateDotNames.NAMED_QUERY,
				HibernateDotNames.NAMED_QUERIES
		);
		for ( AnnotationInstance namedQueryAnnotation : annotations ) {
			bindNamedQuery( namedQueryAnnotation, bindingContext );
		}

		// JPA @NamedNativeQuery
		annotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				JpaDotNames.NAMED_NATIVE_QUERY,
				JpaDotNames.NAMED_NATIVE_QUERIES
		);
		for ( AnnotationInstance namedNativeQueryAnnotation : annotations ) {
			bindNamedNativeQuery( namedNativeQueryAnnotation, bindingContext );
		}

		// JPA @NamedNativeQuery
		annotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				HibernateDotNames.NAMED_NATIVE_QUERY,
				HibernateDotNames.NAMED_NATIVE_QUERIES
		);
		for ( AnnotationInstance namedNativeQueryAnnotation : annotations ) {
			bindNamedNativeQuery( namedNativeQueryAnnotation, bindingContext );
		}


		// JPA @NamedStoredProcedureQuery
		annotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				JpaDotNames.NAMED_STORED_PROCEDURE_QUERY,
				JpaDotNames.NAMED_STORED_PROCEDURE_QUERIES
		);
		for ( AnnotationInstance namedStoredProcedureAnnotation : annotations ) {
			bindNamedStoredProcedureQuery( namedStoredProcedureAnnotation , bindingContext);
		}

	}

	/**
	 * Binds {@link javax.persistence.NamedQuery} as well as {@link org.hibernate.annotations.NamedQuery}.
	 */
	private static void bindNamedQuery(AnnotationInstance namedQueryAnnotation, RootAnnotationBindingContext bindingContext) {
		final String name = bindingContext.stringExtractor.extract( namedQueryAnnotation, "name" );
		if ( StringHelper.isEmpty( name ) ) {
			throw new AnnotationException( "A named query must have a name" );
		}

		final NamedQueryDefinitionBuilder builder = new NamedQueryDefinitionBuilder( name );

		final String query = bindingContext.stringExtractor.extract( namedQueryAnnotation, "query" );
		builder.setQuery( query );

		log.debugf( "Binding named query: %s => %s", name, query );

		if ( namedQueryAnnotation.name().equals( JpaDotNames.NAMED_QUERY ) ) {
			bindJpaNamedQueryValues( builder, namedQueryAnnotation, bindingContext );
		}
		else {
			bindHibernateNamedQueryValues( builder, namedQueryAnnotation, bindingContext );
		}

		bindingContext.getMetadataCollector().addNamedQuery( builder.createNamedQueryDefinition() );
	}

	private static void bindJpaNamedQueryValues(
			NamedQueryDefinitionBuilder builder,
			AnnotationInstance namedQueryAnnotation,
			RootAnnotationBindingContext bindingContext) {
		final Map<String,String> hints = extractHints( namedQueryAnnotation, bindingContext );

		builder.setComment( hints.get( QueryHints.COMMENT ) );
		builder.setFlushMode( asFlushMode( hints.get( QueryHints.FLUSH_MODE ), builder.getName(), namedQueryAnnotation ) );
		builder.setReadOnly( asBoolean( hints.get( QueryHints.READ_ONLY ) ) );

		// timeout - both Hibernate and JPA forms, accounting for negatives
		final Integer timeout = negativeAsNull( getTimeout( hints ) );
		builder.setTimeout( timeout );

		// caching
		builder.setCacheable( asBoolean( hints.get( QueryHints.CACHEABLE ) ) );
		builder.setCacheRegion( hints.get( QueryHints.CACHE_REGION ) );
		builder.setCacheMode( CacheMode.interpretExternalSetting( hints.get( QueryHints.CACHE_MODE ) ) );

		// locking
		final LockOptions lockOptions;
		final LockModeType lockModeType = bindingContext.getTypedValueExtractor( LockModeType.class )
				.extract( namedQueryAnnotation, "lockMode" );
		if ( lockModeType == null ) {
			lockOptions = null;
		}
		else {
			lockOptions = new LockOptions( LockModeConverter.convertToLockMode( lockModeType ) );
			final Integer lockTimeout = negativeAsNull( asInteger( hints.get( "javax.persistence.lock.timeout" ) ) );
			lockOptions.setTimeOut( lockTimeout );
		}
		builder.setLockOptions( lockOptions );
	}

	private static Integer asInteger(String value) {
		if ( value == null ) {
			return null;
		}

		return Integer.valueOf( value );
	}

	private static Integer negativeAsNull(Integer value) {
		if ( value == null ) {
			return null;
		}

		if ( value < 0 ) {
			return null;
		}

		return value;
	}

	private static FlushMode asFlushMode(String hintValue, String name, AnnotationInstance namedQueryAnnotation) {
		if ( hintValue == null ) {
			return null;
		}
		try {
			return FlushMode.valueOf( hintValue.toUpperCase() );
		}
		catch ( IllegalArgumentException e ) {
			throw new AnnotationException(
					String.format(
							Locale.ROOT,
							"Unknown FlushMode [%s] in named query : %s - %s",
							hintValue,
							name,
							namedQueryAnnotation.target().toString()
					)
			);
		}
	}

	private static Integer getTimeout(Map<String, String> hints) {
		if ( hints.containsKey( QueryHints.TIMEOUT_JPA ) ) {
			return ( ( Integer.valueOf( hints.get( QueryHints.TIMEOUT_JPA ) ) + 500 ) / 1000 );
		}

		if ( hints.containsKey( QueryHints.TIMEOUT_HIBERNATE ) ) {
			return Integer.valueOf( hints.get( QueryHints.TIMEOUT_HIBERNATE ) );
		}

		return null;
	}

	private static boolean asBoolean(String hintValue) {
		return asBoolean( hintValue, false );
	}

	private static boolean asBoolean(String hintValue, boolean defaultValue) {
		if ( hintValue == null ) {
			return defaultValue;
		}

		return Boolean.valueOf( hintValue );
	}

	private static Map<String, String> extractHints(
			AnnotationInstance jpaNamedQueryAnnotation,
			RootAnnotationBindingContext bindingContext) {
		final AnnotationInstance[] hintAnnotations = bindingContext.nestedArrayExtractor.extract(
				jpaNamedQueryAnnotation,
				"hints"
		);

		if ( hintAnnotations == null || hintAnnotations.length == 0 ) {
			return Collections.emptyMap();
		}

		final Map<String,String> hintMap = new HashMap<String, String>();
		for ( AnnotationInstance hintAnnotation : hintAnnotations ) {
			hintMap.put(
					bindingContext.stringExtractor.extract( hintAnnotation, "name" ),
					bindingContext.stringExtractor.extract( hintAnnotation, "value" )
			);
		}
		return hintMap;
	}

	private static void bindHibernateNamedQueryValues(
			NamedQueryDefinitionBuilder builder,
			AnnotationInstance namedQueryAnnotation,
			RootAnnotationBindingContext bindingContext) {
		builder.setComment( bindingContext.stringExtractor.extract( namedQueryAnnotation, "comment" ) );

		builder.setReadOnly( bindingContext.booleanExtractor.extract( namedQueryAnnotation, "readOnly" ) );

		builder.setFlushMode(
				EnumConversionHelper.flushModeTypeToFlushMode(
						bindingContext.getTypedValueExtractor( FlushModeType.class )
								.extract( namedQueryAnnotation, "flushMode" ),
						namedQueryAnnotation
				)
		);

		builder.setCacheable(
				bindingContext.booleanExtractor.extract(
						namedQueryAnnotation,
						"cacheable"
				)
		);
		builder.setCacheMode(
				EnumConversionHelper.cacheModeTypeToCacheMode(
						bindingContext.getTypedValueExtractor( CacheModeType.class ).extract(
								namedQueryAnnotation,
								"cacheMode"
						),
						namedQueryAnnotation
				)
		);
		builder.setCacheRegion(
				bindingContext.stringExtractor.extract(
						namedQueryAnnotation,
						"cacheRegion"
				)
		);

		builder.setFetchSize(
				negativeAsNull(
						bindingContext.integerExtractor.extract(
								namedQueryAnnotation,
								"fetchSize"
						)
				)
		);

		builder.setTimeout(
				negativeAsNull(
						bindingContext.integerExtractor.extract(
								namedQueryAnnotation,
								"timeout"
						)
				)
		);
	}
	



	private static void bindNamedNativeQuery(
			AnnotationInstance namedNativeQueryAnnotation,
			RootAnnotationBindingContext bindingContext) {
		final String name = bindingContext.stringExtractor.extract( namedNativeQueryAnnotation, "name" );
		if ( StringHelper.isEmpty( name ) ) {
			throw new AnnotationException( "A named native query must have a name : " + namedNativeQueryAnnotation.target().toString() );
		}

		NamedSQLQueryDefinitionBuilder builder = new NamedSQLQueryDefinitionBuilder( name );

		final String query = bindingContext.stringExtractor.extract( namedNativeQueryAnnotation, "query" );
		builder.setQuery( query );

		log.debugf( "Binding named native query: %s => %s", name, query );

		if ( namedNativeQueryAnnotation.name().equals( JpaDotNames.NAMED_NATIVE_QUERY ) ) {
			bindJpaNamedQueryValues( builder, namedNativeQueryAnnotation, bindingContext );

			final String resultSetMapping = bindingContext.stringExtractor.extract( namedNativeQueryAnnotation, "resultSetMapping" );
			if ( StringHelper.isNotEmpty( resultSetMapping ) ) {
				boolean resultSetMappingExists = bindingContext.getMetadataCollector().getResultSetMappingDefinitions().containsKey( resultSetMapping );
				if ( !resultSetMappingExists ) {
					throw new MappingException(
							String.format(
									"NamedNativeQuery [%s] referenced an non-existent result set mapping [%s] : %s",
									name,
									resultSetMapping,
									namedNativeQueryAnnotation.target()
							)
					);
				}
				builder.setResultSetRef( resultSetMapping );
			}
			else {
				AnnotationValue annotationValue = namedNativeQueryAnnotation.value( "resultClass" );
				NativeSQLQueryRootReturn[] queryRoots;
				if ( annotationValue == null ) {
					// pure native scalar query
					queryRoots = new NativeSQLQueryRootReturn[0];
				}
				else {
					queryRoots = new NativeSQLQueryRootReturn[] {
							new NativeSQLQueryRootReturn(
									"alias1",
									annotationValue.asString(),
									new HashMap<String, String[]>(),
									LockMode.READ
							)
					};
				}
				builder.setQueryReturns( queryRoots );
			}
		}
		else {
			bindHibernateNamedQueryValues( builder, namedNativeQueryAnnotation, bindingContext );

			builder.setQueryReturns( new NativeSQLQueryRootReturn[0] );
		}

		bindingContext.getMetadataCollector().addNamedNativeQuery( builder.createNamedQueryDefinition() );
	}

	private static void bindNamedStoredProcedureQuery(
			AnnotationInstance namedStoredProcedureQueryAnnotation,
			RootAnnotationBindingContext bindingContext) {
		final String name = bindingContext.stringExtractor.extract( namedStoredProcedureQueryAnnotation, "name" );
		if ( StringHelper.isEmpty( name ) ) {
			throw new AnnotationException( "A named native query must have a name : " + namedStoredProcedureQueryAnnotation.target().toString() );
		}

		final String procedureName = bindingContext.stringExtractor.extract( namedStoredProcedureQueryAnnotation, "query" );

		log.debugf( "Starting binding of @NamedStoredProcedureQuery(name=%s, procedureName=%s)", name, procedureName );

		final NamedProcedureCallDefinition.Builder builder = new NamedProcedureCallDefinition.Builder(
				name,
				procedureName
		);

		final AnnotationInstance[] parameterAnnotations = bindingContext.nestedArrayExtractor
				.extract( namedStoredProcedureQueryAnnotation, "parameters" );
		if ( parameterAnnotations != null && parameterAnnotations.length > 0 ) {
			for ( AnnotationInstance parameterAnnotation : parameterAnnotations ) {
				builder.addParameter(
						bindingContext.stringExtractor.extract( parameterAnnotation, "name" ),
						bindingContext.getTypedValueExtractor( ParameterMode.class ).extract(
								parameterAnnotation,
								"mode",
								ParameterMode.IN
						),
						parameterJavaClass( bindingContext.classTypeExtractor.extract( parameterAnnotation, "type" ), bindingContext )
				);
			}
		}

		final AnnotationInstance[] hintAnnotations = bindingContext.nestedArrayExtractor
				.extract( namedStoredProcedureQueryAnnotation, "hints" );
		if ( hintAnnotations != null && hintAnnotations.length > 0 ) {
			for ( AnnotationInstance hintAnnotation : hintAnnotations ) {
				builder.addHint(
						hintAnnotation.value( "name" ).asString(),
						hintAnnotation.value( "value" ).asString()
				);
			}
		}

		final AnnotationValue resultClassesValue = namedStoredProcedureQueryAnnotation.value( "resultClasses" );
		if ( resultClassesValue != null ) {
			final String[] resultClassNames = resultClassesValue.asStringArray();
			if ( resultClassNames != null ) {
				for ( String resultClassName : resultClassNames ) {
					builder.addResultClass(
							bindingContext.getBuildingOptions()
									.getServiceRegistry()
									.getService( ClassLoaderService.class )
									.classForName( resultClassName )
					);
				}
			}
		}

		final AnnotationValue resultSetMappingsValue = namedStoredProcedureQueryAnnotation.value( "resultSetMappings" );
		if ( resultSetMappingsValue != null ) {
			final String[] resultSetMappingNames = resultSetMappingsValue.asStringArray();
			if ( resultSetMappingNames != null ) {
				for ( String resultSetMappingName : resultSetMappingNames ) {
					builder.addResultSetMappingName( resultSetMappingName );
				}
			}
		}

		bindingContext.getMetadataCollector().addNamedProcedureCallDefinition(
				builder.buildDefinition()
		);
	}

	private static Class parameterJavaClass(Type parameterType, RootAnnotationBindingContext bindingContext) {
		return bindingContext.getBuildingOptions()
				.getServiceRegistry()
				.getService( ClassLoaderService.class )
				.classForName( parameterType.name().toString() );
	}
}
