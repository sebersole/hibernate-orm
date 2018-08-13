/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.procedure.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.procedure.UnknownSqlResultSetMappingException;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.spi.ResultSetMappingDescriptor;
import org.hibernate.sql.results.internal.EntityQueryResultImpl;
import org.hibernate.sql.results.internal.ScalarQueryResultImpl;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.SqlAstCreationContext;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.EntityJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.spi.BasicType;

import org.jboss.logging.Logger;

/**
 * Utilities used to implement procedure call support.
 *
 * @author Steve Ebersole
 */
public class Util {
	private static final Logger log = Logger.getLogger( Util.class );

	private Util() {
	}


	/**
	 * Make a (shallow) copy of query spaces to be synchronized
	 *
	 * @param synchronizedQuerySpaces The query spaces
	 *
	 * @return The copy
	 */
	public static Set<String> copy(Set<String> synchronizedQuerySpaces) {
		return CollectionHelper.makeCopy( synchronizedQuerySpaces );
	}

	/**
	 * Make a (shallow) copy of the JPA query hints map
	 *
	 * @param hints The JPA query hints to copy
	 *
	 * @return The copy
	 */
	public static Map<String,Object> copy(Map<String, Object> hints) {
		return CollectionHelper.makeCopy( hints );
	}

	/**
	 * Context for resolving result-set-mapping definitions
	 */
	public static interface ResultSetMappingResolutionContext {
		/**
		 * Access to the SessionFactory
		 *
		 * @return SessionFactory
		 */
		SessionFactoryImplementor getSessionFactory();

		/**
		 * Locate a ResultSetMappingDefinition by name
		 *
		 * @param name The name of the ResultSetMappingDefinition to locate
		 *
		 * @return The ResultSetMappingDefinition
		 */
		ResultSetMappingDescriptor findResultSetMapping(String name);

		/**
		 * Callback to add query returns indicated by the result set mapping(s)
		 *
		 * @param queryReturns The query returns
		 */
		void addQueryReturns(QueryResult... queryReturns);

		/**
		 * Callback to add query spaces indicated by the result set mapping(s)
		 *
		 * @param querySpaces The query spaces
		 */
		void addQuerySpaces(String... querySpaces);
	}

	/**
	 * Resolve the given result set mapping names
	 *
	 * @param context The context for the resolution.  See {@link ResultSetMappingResolutionContext}
	 * @param resultSetMappingNames The names of the result-set-mappings to resolve
	 */
	public static void resolveResultSetMappings(
			ResultSetMappingResolutionContext context,
			String... resultSetMappingNames) {
		final QueryReturnResolver resolver = new QueryReturnResolver( context );

		for ( String resultSetMappingName : resultSetMappingNames ) {
			resolver.resolve( resultSetMappingName );
		}
	}

	public static void resolveResultSetMapping(
			ResultSetMappingResolutionContext context,
			String resultSetMappingName) {
		new QueryReturnResolver( context ).resolve( resultSetMappingName );
	}

	/**
	 * Context for resolving result-class definitions
	 */
	public interface ResultClassesResolutionContext {

		/**
		 * Access to the SessionFactory
		 *
		 * @return SessionFactory
		 */
		SessionFactoryImplementor getSessionFactory();

		/**
		 * Callback to add query returns indicated by the result set mapping(s)
		 *
		 * @param queryReturns The query returns
		 */
		void addQueryResult(QueryResult... queryReturns);

		/**
		 * Callback to add query spaces indicated by the result set mapping(s)
		 *
		 * @param querySpaces The query spaces
		 */
		void addQuerySpaces(String... querySpaces);
		void addQuerySpaces(Collection<String> querySpaces);

		SqlAstCreationContext getSqlAstCreationContext();
	}

	/**
	 * Resolve the given result classes
	 *
	 * @param context The context for the resolution.  See {@link ResultSetMappingResolutionContext}
	 * @param resultClasses The Classes to which the results should be mapped
	 */
	public static void resolveResultClasses(ResultClassesResolutionContext context, Class... resultClasses) {
		for ( Class resultClass : resultClasses ) {
			resolveResultClass( context, resultClass );
		}
	}

	public static void resolveResultClass(ResultClassesResolutionContext context, Class resultType) {
		final JavaTypeDescriptor resultTypeDescriptor = context.getSessionFactory()
				.getTypeConfiguration()
				.getJavaTypeDescriptorRegistry()
				.getDescriptor( resultType );

		if ( resultTypeDescriptor instanceof BasicJavaDescriptor ) {
			final BasicType basicType = context.getSessionFactory()
					.getTypeConfiguration()
					.getBasicTypeRegistry()
					.getBasicType( resultType );

			context.addQueryResult(
					new ScalarQueryResultImpl(
							// todo (6.0) : resultVariable?
							null,
							// todo : SqlSelection
							null,
							basicType.getSqlExpressableType( context.getSessionFactory().getTypeConfiguration() )
					)
			);
		}
		else if ( resultTypeDescriptor instanceof EntityJavaDescriptor ) {
			final EntityDescriptor entityDescriptor = context.getSessionFactory().getMetamodel().getEntityDescriptor( resultType.getName() );
			context.addQuerySpaces( entityDescriptor.getAffectedTableNames() );
			context.addQueryResult(
					new EntityQueryResultImpl(
							entityDescriptor,
							// todo (6.0) : resultVariable?
							null,
							// todo : EntitySqlSelectionMappings
							null,
							LockMode.NONE,
							new NavigablePath( entityDescriptor.getEntityName() ),
							context.getSqlAstCreationContext()
					)
			);
		}

		//		int i = 0;
//		for ( Class resultClass : resultClasses ) {
//			final EntityDescriptor entityDescriptor = context.getSessionFactory().getTypeConfiguration().getEntityDescriptor( resultClass.getName() );
//			context.addQuerySpaces( (String[]) entityDescriptor.getAffectedTableNames() );
//			context.addQueryResult( entityDescriptor.generateQueryResult(  )
//					new QueryResultEntityImpl(
//							entityDescriptor,
//							null,
//							// todo : SqlSelection map
//							null,
//							new NavigablePath( entityDescriptor.getEntityName() ),
//							null
//					)
//			);
//		}

		throw new NotYetImplementedFor6Exception(  );
	}

	private static class QueryReturnResolver {
		private final ResultSetMappingResolutionContext context;
		private int selectablesCount = 0;

		Map<String,SqlSelection> sqlSelectionMap = new HashMap<>();
		Map<String, FetchParent> fetchParentMap = null;

		public QueryReturnResolver(ResultSetMappingResolutionContext context) {
			this.context = context;
		}

		public void resolve(String resultSetMappingName) {
			log.tracef( "Starting attempt to resolve named result-set-mapping : %s", resultSetMappingName );

			final ResultSetMappingDescriptor mapping = context.findResultSetMapping( resultSetMappingName );
			if ( mapping == null ) {
				throw new UnknownSqlResultSetMappingException( "Unknown SqlResultSetMapping [" + resultSetMappingName + "]" );
			}

			log.tracef( "Found result-set-mapping : %s", mapping.getName() );


			// even though we only read from JDBC via positions now, we can still leverage the specified
			//		aliases here as a key to resolve SqlSelections
			//	todo : implement ^^

			throw new NotYetImplementedFor6Exception();
		}
//			for ( NativeSQLQueryReturn nativeQueryReturn : mapping.getQueryResultBuilders() ) {
//				if ( nativeQueryReturn instanceof NativeSQLQueryScalarReturn ) {
//					final NativeSQLQueryScalarReturn rtn = (NativeSQLQueryScalarReturn) nativeQueryReturn;
//					final QueryResultScalarImpl scalarReturn = new QueryResultScalarImpl(
//							null,
//							resolveSqlSelection( (BasicType) rtn.getType(), rtn.getColumnAlias() ),
//							null,
//							(BasicType) rtn.getType()
//					);
//					context.addQueryReturns( scalarReturn );
//				}
//				else if ( nativeQueryReturn instanceof NativeSQLQueryConstructorReturn ) {
//					final NativeSQLQueryConstructorReturn rtn = (NativeSQLQueryConstructorReturn) nativeQueryReturn;
//					final QueryResultDynamicInstantiationImpl dynamicInstantiationReturn = new QueryResultDynamicInstantiationImpl(
//							new DynamicInstantiation( rtn.getTargetClass() ),
//							null,
//							buildDynamicInstantiationAssembler( rtn )
//					);
//					context.addQueryReturns( dynamicInstantiationReturn );
//				}
//				else if ( nativeQueryReturn instanceof NativeSQLQueryCollectionReturn ) {
//					final NativeSQLQueryCollectionReturn rtn = (NativeSQLQueryCollectionReturn) nativeQueryReturn;
//					final String role = rtn.getOwnerEntityName() + '.' + rtn.getOwnerProperty();
//					final PersistentCollectionDescriptor persister = context.getSessionFactory().getTypeConfiguration().findCollectionDescriptor( role );
//					//context.addQueryReturns( ... );
//					throw new NotYetImplementedException( "Collection Returns not yet implemented" );
//				}
//				else if ( nativeQueryReturn instanceof NativeSQLQueryRootReturn ) {
//					final NativeSQLQueryRootReturn rtn = (NativeSQLQueryRootReturn) nativeQueryReturn;
//					final EntityDescriptor persister = context.getSessionFactory().getTypeConfiguration().getEntityDescriptor( rtn.getReturnEntityName() );
//					final QueryResultEntityImpl entityReturn = new QueryResultEntityImpl(
//							null,
//							persister,
//							null,
//							// todo : SqlSelections
//							null,
//							new NavigablePath( persister.getEntityName() ),
//							null
//					);
//					context.addQueryReturns( entityReturn );
//					if ( fetchParentMap == null ) {
//						fetchParentMap = new HashMap<>();
//					}
//					fetchParentMap.put( rtn.getAlias(), entityReturn );
//				}
//				else if ( nativeQueryReturn instanceof NativeSQLQueryJoinReturn ) {
//					final NativeSQLQueryJoinReturn rtn = (NativeSQLQueryJoinReturn) nativeQueryReturn;
//					// tod finish
//				}
//			}
//		}
//
//		private SqlSelection resolveSqlSelection(BasicType ormType, String alias) {
//			return sqlSelectionMap.computeIfAbsent(
//					alias,
//					s -> new SqlSelectionImpl(
//							ormType.getSqlSelectionReader()
//							new SqlSelectionReaderImpl( ormType ),
//							selectablesCount++
//					)
//			);
//		}
//
//		private QueryResultAssembler buildDynamicInstantiationAssembler(NativeSQLQueryConstructorReturn nativeQueryReturn) {
//			final JavaTypeDescriptor resultType = context.getSessionFactory()
//					.getTypeConfiguration()
//					.getJavaTypeDescriptorRegistry()
//					.getDescriptor( nativeQueryReturn.getTargetClass() );
//			final Class targetJavaType = resultType.getJavaType();
//
//			if ( Map.class.equals( targetJavaType ) ) {
//				throw new HibernateException( "Map dynamic-instantiations not allowed for native/procedure queries" );
//			}
//
//			final List<ArgumentReader> argumentReaders = new ArrayList<>();
//
//			for ( NativeSQLQueryScalarReturn argument : nativeQueryReturn.getColumnReturns() ) {
//				final BasicType ormType = (BasicType) argument.getType();
//				final ScalarQueryResultImpl argumentReturn = new ScalarQueryResultImpl(
//						null,
//						resolveSqlSelection( ormType, argument.getColumnAlias() ),
//						null,
//						ormType
//				);
//				argumentReaders.add( new ArgumentReader( argumentReturn.getResultAssembler(), null ) );
//			}
//
//			if ( List.class.equals( targetJavaType ) ) {
//				return new DynamicInstantiationListAssemblerImpl( (BasicJavaDescriptor<List>) resultType, argumentReaders );
//			}
//			else {
//				// find a constructor matching argument types
//				constructor_loop:
//				for ( Constructor constructor : targetJavaType.getDeclaredConstructors() ) {
//					if ( constructor.getParameterTypes().length != argumentReaders.size() ) {
//						continue;
//					}
//
//					for ( int i = 0; i < argumentReaders.size(); i++ ) {
//						final ArgumentReader argumentReader = argumentReaders.get( i );
//						// todo : move Compatibility from SQM into ORM?  It is only used here
//						final boolean assignmentCompatible = Compatibility.areAssignmentCompatible(
//								resolveJavaTypeDescriptor( constructor.getParameterTypes()[i] ),
//								argumentReader.getJavaTypeDescriptor()
//						);
//						if ( !assignmentCompatible ) {
//							log.debugf(
//									"Skipping constructor for dynamic-instantiation match due to argument mismatch [%s] : %s -> %s",
//									i,
//									constructor.getParameterTypes()[i],
//									argumentReader.getJavaTypeDescriptor().getJavaType().getName()
//							);
//							continue constructor_loop;
//						}
//					}
//
//					constructor.setAccessible( true );
//					return new DynamicInstantiationConstructorAssemblerImpl( constructor, resultType, argumentReaders );
//				}
//
//				throw new HibernateException(
//						"Could not locate appropriate constructor for dynamic instantiation of [" + targetJavaType.getName() + "]"
//				);
//			}
//		}

		@SuppressWarnings("unchecked")
		private JavaTypeDescriptor resolveJavaTypeDescriptor(Class javaType) {
			return context.getSessionFactory()
					.getTypeConfiguration()
					.getJavaTypeDescriptorRegistry()
					.getDescriptor( javaType );
		}
	}
}
