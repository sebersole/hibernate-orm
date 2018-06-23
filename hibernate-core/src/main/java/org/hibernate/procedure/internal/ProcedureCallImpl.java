/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.procedure.internal;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TransactionRequiredException;

import org.hibernate.HibernateException;
import org.hibernate.JDBCException;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.ScrollMode;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.graph.spi.EntityGraphImplementor;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.jpa.internal.util.ConfigurationHelper;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.procedure.NoSuchParameterException;
import org.hibernate.procedure.ParameterMisuseException;
import org.hibernate.procedure.ParameterStrategyException;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.procedure.ProcedureOutputs;
import org.hibernate.procedure.spi.CallableStatementSupport;
import org.hibernate.procedure.spi.ParameterStrategy;
import org.hibernate.procedure.spi.ProcedureCallImplementor;
import org.hibernate.procedure.spi.ProcedureParamBindings;
import org.hibernate.procedure.spi.ProcedureParameterImplementor;
import org.hibernate.procedure.spi.ProcedureParameterMetadata;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.internal.AbstractQuery;
import org.hibernate.query.internal.QueryOptionsImpl;
import org.hibernate.query.named.internal.NamedCallableQueryMementoImpl;
import org.hibernate.query.named.spi.NamedCallableQueryMemento;
import org.hibernate.query.named.spi.ParameterMemento;
import org.hibernate.query.spi.MutableQueryOptions;
import org.hibernate.query.spi.ParameterBindingContext;
import org.hibernate.query.spi.QueryParameterBindings;
import org.hibernate.query.spi.ResultSetMappingDescriptor;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.query.sqm.AllowableParameterType;
import org.hibernate.result.NoMoreOutputsException;
import org.hibernate.result.Output;
import org.hibernate.result.ResultSetOutput;
import org.hibernate.result.UpdateCountOutput;
import org.hibernate.result.internal.ResultContext;
import org.hibernate.sql.ast.produce.sqm.spi.Callback;
import org.hibernate.sql.results.internal.RowReaderStandardImpl;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultAssembler;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.RowReader;
import org.hibernate.type.Type;

/**
 * Standard implementation of {@link org.hibernate.procedure.ProcedureCall}
 *
 * @author Steve Ebersole
 */
public class ProcedureCallImpl<R>
		extends AbstractQuery<R>
		implements ProcedureCallImplementor<R>, ResultContext {
	private final String procedureName;

	private FunctionReturnImpl functionReturn;

	private final ProcedureParameterMetadata parameterMetadata;
	private final ProcedureParamBindings paramBindings;

	private final List<RowReader> rowReaders;

	private Set<String> synchronizedQuerySpaces;

	private ProcedureOutputsImpl outputs;

	private final QueryOptionsImpl queryOptions = new QueryOptionsImpl();


	/**
	 * The no-returns form.
	 *
	 * @param session The session
	 * @param procedureName The name of the procedure to call
	 */
	public ProcedureCallImpl(SharedSessionContractImplementor session, String procedureName) {
		super( session );
		this.procedureName = procedureName;

		this.parameterMetadata = new ProcedureParameterMetadata( this );
		this.paramBindings = new ProcedureParamBindings( parameterMetadata, this );

		this.synchronizedQuerySpaces = Collections.emptySet();
		this.rowReaders = Collections.emptyList();
	}

	/**
	 * The result Class(es) return form
	 *
	 * @param session The session
	 * @param procedureName The name of the procedure to call
	 * @param resultClasses The classes making up the result
	 */
	public ProcedureCallImpl(final SharedSessionContractImplementor session, String procedureName, Class... resultClasses) {
		super( session );
		this.procedureName = procedureName;

		this.parameterMetadata = new ProcedureParameterMetadata( this );
		this.paramBindings = new ProcedureParamBindings( parameterMetadata, this );

		final Set<String> querySpaces = new HashSet<>();
		final List<QueryResultAssembler> returnAssemblers = new ArrayList<>();
		final List<Initializer> initializers = new ArrayList<>();

		Util.resolveResultClasses(
				new Util.ResultClassesResolutionContext() {
					@Override
					public QueryResultCreationContext getQueryResultCreationContext() {
						throw new NotYetImplementedFor6Exception();
					}

					@Override
					public SessionFactoryImplementor getSessionFactory() {
						return session.getFactory();
					}

					@Override
					public void addQuerySpaces(String... spaces) {
						Collections.addAll( querySpaces, spaces );
					}

					@Override
					public void addQuerySpaces(Collection<String> spaces) {
						querySpaces.addAll( spaces );
					}

					@Override
					public void addQueryResult(QueryResult... queryResults) {
						for ( QueryResult queryResult : queryResults ) {
							queryResult.registerInitializers( initializers::add );
							returnAssemblers.add( queryResult.getResultAssembler() );
						}
					}
				},
				resultClasses
		);

		this.synchronizedQuerySpaces = Collections.unmodifiableSet( querySpaces );
		this.rowReaders = Collections.singletonList(
				new RowReaderStandardImpl<>(
						returnAssemblers,
						initializers,
						null,
						null
				)
		);
	}

	/**
	 * The result-set-mapping(s) return form
	 *
	 * @param session The session
	 * @param procedureName The name of the procedure to call
	 * @param resultSetMappings The names of the result set mappings making up the result
	 */
	public ProcedureCallImpl(final SharedSessionContractImplementor session, String procedureName, String... resultSetMappings) {
		super( session );
		this.procedureName = procedureName;

		this.parameterMetadata = new ProcedureParameterMetadata( this );
		this.paramBindings = new ProcedureParamBindings( parameterMetadata, this );

		final Set<String> querySpaces = new HashSet<>();

		this.rowReaders = CollectionHelper.arrayList( resultSetMappings.length );

		for ( String resultSetMapping : resultSetMappings ) {
			final List<QueryResultAssembler> returnAssemblers = new ArrayList<>();
			final List<Initializer> initializers = new ArrayList<>();

			Util.resolveResultSetMapping(
					new Util.ResultSetMappingResolutionContext() {
						@Override
						public SessionFactoryImplementor getSessionFactory() {
							return session.getFactory();
						}

						@Override
						public ResultSetMappingDescriptor findResultSetMapping(String name) {
							return session.getFactory().getQueryEngine().getNamedQueryRepository().getResultSetMappingDescriptor( name );
						}

						@Override
						public void addQuerySpaces(String... spaces) {
							Collections.addAll( querySpaces, spaces );
						}

						@Override
						public void addQueryReturns(QueryResult... queryResults) {
							for ( QueryResult queryResult : queryResults ) {
								queryResult.registerInitializers( initializers::add );
								returnAssemblers.add( queryResult.getResultAssembler() );
							}
						}

						// todo : add QuerySpaces to JdbcOperation
						// todo : add JdbcOperation#shouldFlushAffectedQuerySpaces()
					},
					resultSetMapping
			);

			rowReaders.add(
					new RowReaderStandardImpl<>(
							returnAssemblers,
							initializers,
							null,
							null
					)
			);
		}

		this.synchronizedQuerySpaces = Collections.unmodifiableSet( querySpaces );
	}

	/**
	 * The named/stored copy constructor
	 *
	 * @param session The session
	 * @param memento The named/stored memento
	 */
	@SuppressWarnings("unchecked")
	public ProcedureCallImpl(SharedSessionContractImplementor session, NamedCallableQueryMemento memento, Class<R> resultType) {
		super( session );

		this.procedureName = memento.getCallableName();

		this.parameterMetadata = new ProcedureParameterMetadata( this );
		this.paramBindings = new ProcedureParamBindings( parameterMetadata, this );

		if ( memento.getResultClasses().length > 0 ) {
			final Set<String> querySpaces = new HashSet<>();
			final List<QueryResultAssembler> returnAssemblers = new ArrayList<>();
			final List<Initializer> initializers = new ArrayList<>();

			Util.resolveResultClasses(
					new Util.ResultClassesResolutionContext() {
						@Override
						public QueryResultCreationContext getQueryResultCreationContext() {
							throw new NotYetImplementedFor6Exception();
						}

						@Override
						public SessionFactoryImplementor getSessionFactory() {
							return session.getFactory();
						}

						@Override
						public void addQuerySpaces(String... spaces) {
							Collections.addAll( querySpaces, spaces );
						}

						@Override
						public void addQuerySpaces(Collection<String> spaces) {
							querySpaces.addAll( spaces );
						}

						@Override
						public void addQueryResult(QueryResult... queryResults) {
							for ( QueryResult queryResult : queryResults ) {
								queryResult.registerInitializers( initializers::add );
								returnAssemblers.add( queryResult.getResultAssembler() );
							}
						}
					},
					memento.getResultClasses()
			);

			this.synchronizedQuerySpaces = Collections.unmodifiableSet( querySpaces );
			this.rowReaders = Collections.singletonList(
					new RowReaderStandardImpl<>(
							returnAssemblers,
							initializers,
							null,
							null
					)
			);
		}
		else if ( memento.getResultSetMappingNames().length > 0 ) {
			final Set<String> querySpaces = new HashSet<>();

			this.rowReaders = CollectionHelper.arrayList( memento.getResultSetMappingNames().length );

			for ( String resultSetMapping : memento.getResultSetMappingNames() ) {
				final List<QueryResultAssembler> returnAssemblers = new ArrayList<>();
				final List<Initializer> initializers = new ArrayList<>();

				Util.resolveResultSetMapping(
						new Util.ResultSetMappingResolutionContext() {
							@Override
							public SessionFactoryImplementor getSessionFactory() {
								return session.getFactory();
							}

							@Override
							public ResultSetMappingDescriptor findResultSetMapping(String name) {
								return session.getFactory().getQueryEngine().getNamedQueryRepository().getResultSetMappingDescriptor( name );
							}

							@Override
							public void addQuerySpaces(String... spaces) {
								Collections.addAll( querySpaces, spaces );
							}

							@Override
							public void addQueryReturns(QueryResult... queryResults) {
								for ( QueryResult queryResult : queryResults ) {
									queryResult.registerInitializers( initializers::add );
									returnAssemblers.add( queryResult.getResultAssembler() );
								}
							}

							// todo : add QuerySpaces to JdbcOperation
							// todo : add JdbcOperation#shouldFlushAffectedQuerySpaces()
						},
						resultSetMapping
				);

				rowReaders.add(
						new RowReaderStandardImpl<>(
								returnAssemblers,
								initializers,
								null,
								null
						)
				);
			}

			this.synchronizedQuerySpaces = Collections.unmodifiableSet( querySpaces );
		}
		else {
			this.rowReaders = Collections.emptyList();
			this.synchronizedQuerySpaces = new HashSet<>();
		}

		this.synchronizedQuerySpaces.addAll( memento.getQuerySpaces() );

		memento.getHints().forEach( this::setHint );
	}

	@Override
	public String getProcedureName() {
		return procedureName;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureParameterMetadata getParameterMetadata() {
		return parameterMetadata;
	}

	@Override
	public QueryParameterBindings getQueryParameterBindings() {
		return paramBindings;
	}

	@Override
	public MutableQueryOptions getQueryOptions() {
		return queryOptions;
	}

	@Override
	public boolean isFunctionCall() {
		return functionReturn != null;
	}

	@Override
	public ProcedureCall markAsFunctionCall(int sqlType) {
		functionReturn = new FunctionReturnImpl( this, sqlType );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureParameterImplementor registerParameter(int position, Class type, ParameterMode mode) {
		final ProcedureParameterImpl procedureParameter = new ProcedureParameterImpl(
				position,
				mode,
				type,
				getSession().getFactory().getTypeConfiguration().getBasicTypeRegistry().getBasicType( type ),
				false
		);
		parameterMetadata.registerParameter( procedureParameter );
		return procedureParameter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCall registerParameter0(int position, Class type, ParameterMode mode) {
		registerParameter( position, type, mode );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureParameterImplementor getParameterRegistration(int position) {
		return getParameterMetadata().getQueryParameter( position );
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureParameterImplementor registerParameter(String name, Class type, ParameterMode mode) {
		final ProcedureParameterImpl procedureParameter = new ProcedureParameterImpl(
				name,
				mode,
				type,
				getSession().getFactory().getTypeConfiguration().getBasicTypeRegistry().getBasicType( type ),
				false
		);

		parameterMetadata.registerParameter( procedureParameter );

		return procedureParameter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCall registerParameter0(String name, Class type, ParameterMode mode) {
		registerParameter( name, type, mode );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureParameterImplementor getParameterRegistration(String name) {
		return getParameterMetadata().getQueryParameter( name );
	}

	@Override
	@SuppressWarnings("unchecked")
	public List getRegisteredParameters() {
		return new ArrayList( getParameterMetadata().collectAllParameters() );
	}

	@Override
	public ProcedureOutputs getOutputs() {
		if ( outputs == null ) {
			outputs = buildOutputs();
		}

		return outputs;
	}

	private ProcedureOutputsImpl buildOutputs() {
		paramBindings.validate();

		if ( isFunctionCall() ) {
			if ( parameterMetadata.getParameterCount() <= 0 ) {
				// should we simply warn and switch this back to a proc-call?
				throw new IllegalStateException(
						"A function call was requested, but no parameter registrations were made; " +
								"at least need to register the function result param"
				);
			}
		}

		// todo : a lot of this should be delegated to something like SqlTreeExecutor
		// 		^^ and there should be a generalized contracts pulled out of SqlTreeExecutor concept
		// 		for individually:
		//			1) Preparing the PreparedStatement for execution (PreparedStatementPreparer) - including:
		//				* getting the PreparedStatement/CallableStatement from the Connection
		//				* registering CallableStatement parameters (JdbcCallParameterRegistrations)
		//				* binding any IN/INOUT parameter values into the PreparedStatement
		//			2) Executing the PreparedStatement and giving access to the outputs - PreparedStatementExecutor
		//				- ? PreparedStatementResult as an abstraction at the Jdbc level?
		//			3) For ResultSet outputs, extracting the "jdbc values" - for integration into
		// 				the org.hibernate.sql.results.process stuff.  This allows, for example, easily
		// 				applying query result caching over a ProcedureCall for its ResultSet outputs (if we
		//				decide that is a worthwhile feature.

		// the current approach to this in the SQM stuff is that SqlTreeExecutor is passed delegates
		// that handle the 3 phases mentioned above (create statement, execute, extract results) - it
		// acts as the "coordinator"
		//
		// but another approach would be to have different "SqlTreeExecutor" impls, i.e.. JdbcCallExecutor, JdbcSelectExecutor, etc
		// this approach has a lot of benefits

		final CallableStatementSupport callableStatementSupport = getSession().getFactory().getJdbcServices()
				.getJdbcEnvironment()
				.getDialect()
				.getCallableStatementSupport();

		return new ProcedureOutputsImpl(
				this,
				callableStatementSupport.interpretCall(
						getProcedureName(),
						functionReturn,
						parameterMetadata,
						paramBindings,
						getSession()
				),
				parameterMetadata.getParameterStrategy(),
				queryOptions,
				paramBindings,
				getSession()
		);
	}

	@Override
	public String getQueryString() {
		return getProcedureName() + "(...)";
	}

	/**
	 * Use this form instead of {@link #getSynchronizedQuerySpaces()} when you want to make sure the
	 * underlying Set is instantiated (aka, on add)
	 *
	 * @return The spaces
	 */
	@SuppressWarnings("WeakerAccess")
	protected Set<String> synchronizedQuerySpaces() {
		return synchronizedQuerySpaces == null ? Collections.emptySet() : synchronizedQuerySpaces;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getSynchronizedQuerySpaces() {
		if ( synchronizedQuerySpaces == null ) {
			return Collections.emptySet();
		}
		else {
			return Collections.unmodifiableSet( synchronizedQuerySpaces );
		}
	}

	@Override
	public ProcedureCallImplementor<R> addSynchronizedQuerySpace(String querySpace) {
		synchronizedQuerySpaces().add( querySpace );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> addSynchronizedEntityName(String entityName) {
		addSynchronizedQuerySpaces( getSession().getFactory().getMetamodel().findEntityDescriptor( entityName ) );
		return this;
	}

	@SuppressWarnings("WeakerAccess")
	protected void addSynchronizedQuerySpaces(EntityDescriptor<?> descriptor) {
		synchronizedQuerySpaces().addAll( descriptor.getAffectedTableNames() );
	}

	@Override
	public ProcedureCallImplementor<R> addSynchronizedEntityClass(Class entityClass) {
		addSynchronizedQuerySpaces( getSession().getFactory().getMetamodel().findEntityDescriptor( entityClass.getName() ) );
		return this;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// JPA StoredProcedureQuery impl

	private ProcedureOutputs procedureResult;

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> registerStoredProcedureParameter(int position, Class type, ParameterMode mode) {
		getSession().checkOpen( true );

		try {
			registerParameter( position, type, mode );
		}
		catch (HibernateException he) {
			throw getSession().getExceptionConverter().convert( he );
		}
		catch (RuntimeException e) {
			getSession().markForRollbackOnly();
			throw e;
		}

		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> registerStoredProcedureParameter(String parameterName, Class type, ParameterMode mode) {
		getSession().checkOpen( true );
		try {
			registerParameter( parameterName, type, mode );
		}
		catch (HibernateException he) {
			throw getSession().getExceptionConverter().convert( he );
		}
		catch (RuntimeException e) {
			getSession().markForRollbackOnly();
			throw e;
		}

		return this;
	}


	// outputs ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public boolean execute() {
		try {
			final Output rtn = outputs().getCurrent();
			return ResultSetOutput.class.isInstance( rtn );
		}
		catch (NoMoreOutputsException e) {
			return false;
		}
		catch (HibernateException he) {
			throw getSession().getExceptionConverter().convert( he );
		}
		catch (RuntimeException e) {
			getSession().markForRollbackOnly();
			throw e;
		}
	}

	protected ProcedureOutputs outputs() {
		if ( procedureResult == null ) {
			procedureResult = getOutputs();
		}
		return procedureResult;
	}

	@Override
	protected int doExecuteUpdate() {
		if ( ! getSession().isTransactionInProgress() ) {
			throw new TransactionRequiredException( "javax.persistence.Query.executeUpdate requires active transaction" );
		}

		// the expectation is that there is just one Output, of type UpdateCountOutput
		try {
			execute();
			return getUpdateCount();
		}
		finally {
			outputs().release();
		}
	}

	@Override
	public Object getOutputParameterValue(int position) {
		// NOTE : according to spec (specifically), an exception thrown from this method should not mark for rollback.
		try {
			return outputs().getOutputParameterValue( position );
		}
		catch (ParameterStrategyException e) {
			throw new IllegalArgumentException( "Invalid mix of named and positional parameters", e );
		}
		catch (NoSuchParameterException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	@Override
	public Object getOutputParameterValue(String parameterName) {
		// NOTE : according to spec (specifically), an exception thrown from this method should not mark for rollback.
		try {
			return outputs().getOutputParameterValue( parameterName );
		}
		catch (ParameterStrategyException e) {
			throw new IllegalArgumentException( "Invalid mix of named and positional parameters", e );
		}
		catch (NoSuchParameterException e) {
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	@Override
	public boolean hasMoreResults() {
		return outputs().goToNext() && ResultSetOutput.class.isInstance( outputs().getCurrent() );
	}

	@Override
	public int getUpdateCount() {
		try {
			final Output rtn = outputs().getCurrent();
			if ( rtn == null ) {
				return -1;
			}
			else if ( UpdateCountOutput.class.isInstance( rtn ) ) {
				return ( (UpdateCountOutput) rtn ).getUpdateCount();
			}
			else {
				return -1;
			}
		}
		catch (NoMoreOutputsException e) {
			return -1;
		}
		catch (HibernateException he) {
			throw getSession().getExceptionConverter().convert( he );
		}
		catch (RuntimeException e) {
			getSession().markForRollbackOnly();
			throw e;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected List<R> doList() {
		if ( getMaxResults() == 0 ) {
			return Collections.EMPTY_LIST;
		}
		try {
			final Output rtn = outputs().getCurrent();
			if ( ! ResultSetOutput.class.isInstance( rtn ) ) {
				throw new IllegalStateException( "Current CallableStatement ou was not a ResultSet, but getResultList was called" );
			}

			return ( (ResultSetOutput) rtn ).getResultList();
		}
		catch (NoMoreOutputsException e) {
			// todo : the spec is completely silent on these type of edge-case scenarios.
			// Essentially here we'd have a case where there are no more results (ResultSets nor updateCount) but
			// getResultList was called.
			return null;
		}
		catch (HibernateException he) {
			throw getSession().getExceptionConverter().convert( he );
		}
		catch (RuntimeException e) {
			getSession().markForRollbackOnly();
			throw e;
		}
	}

	@Override
	protected ScrollableResultsImplementor doScroll(ScrollMode scrollMode) {
		throw new UnsupportedOperationException( "Query#scroll is not valid for ProcedureCall/StoredProcedureQuery" );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> cls) {
		if ( cls.isInstance( this ) ) {
			return (T) this;
		}

		if ( cls.isInstance( parameterMetadata ) ) {
			return (T) parameterMetadata;
		}

		if ( cls.isInstance( paramBindings ) ) {
			return (T) paramBindings;
		}

		if ( cls.isInstance( queryOptions ) ) {
			return (T) queryOptions;
		}

		if ( cls.isInstance( getSession() ) ) {
			return (T) getSession();
		}

		if ( ProcedureOutputs.class.isAssignableFrom( cls ) ) {
			return (T) getOutputs();
		}

		throw new PersistenceException( "Unrecognized unwrap type : " + cls.getName() );
	}

	@Override
	@SuppressWarnings("deprecation")
	public ProcedureCallImplementor<R> setHint(String hintName, Object value) {
		if ( FUNCTION_RETURN_TYPE_HINT.equals( hintName ) ) {
			markAsFunctionCall( ConfigurationHelper.getInteger( value ) );
		}

		super.setHint( hintName, value );

		return this;
	}

	@Override
	protected void applyEntityGraphQueryHint(String hintName, EntityGraphImplementor entityGraph) {
		throw new IllegalStateException( "EntityGraph hints are not supported for ProcedureCall/StoredProcedureQuery" );
	}

	@Override
	protected boolean canApplyAliasSpecificLockModes() {
		return false;
	}

	@Override
	protected void verifySettingLockMode() {
		throw new IllegalStateException( "Illegal attempt to set lock mode on a ProcedureCall / StoredProcedureQuery" );
	}

	@Override
	protected void verifySettingAliasSpecificLockModes() {
		throw new IllegalStateException( "Illegal attempt to set lock mode on a ProcedureCall / StoredProcedureQuery" );
	}

	@Override
	public ProcedureCallImplementor<R> setLockMode(LockModeType lockMode) {
		throw new IllegalStateException( "javax.persistence.Query.setLockMode not valid on javax.persistence.StoredProcedureQuery" );
	}

	@Override
	public LockModeType getLockMode() {
		throw new IllegalStateException( "javax.persistence.Query.getLockMode not valid on javax.persistence.StoredProcedureQuery" );
	}

	@Override
	public ProcedureCallImplementor<R> setFlushMode(FlushModeType flushModeType) {
		super.setFlushMode( flushModeType );
		return this;
	}



	@Override
	public ProcedureCallImplementor<R> setParameterList(int position, Collection values) {
		throw new ParameterMisuseException( "ProcedureCall/StoredProcedureQuery parameters cannot be multi-valued" );
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(int position, Collection values, Type type) {
		throw new ParameterMisuseException( "ProcedureCall/StoredProcedureQuery parameters cannot be multi-valued" );
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(int position, Object[] values, Type type) {
		throw new ParameterMisuseException( "ProcedureCall/StoredProcedureQuery parameters cannot be multi-valued" );
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(int position, Object[] values) {
		throw new ParameterMisuseException( "ProcedureCall/StoredProcedureQuery parameters cannot be multi-valued" );
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(Parameter<Instant> param, Instant value, TemporalType temporalType) {
		super.setParameter( param, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(Parameter<LocalDateTime> param, LocalDateTime value, TemporalType temporalType) {
		super.setParameter( param, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(Parameter<ZonedDateTime> param, ZonedDateTime value, TemporalType temporalType) {
		super.setParameter( param, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(Parameter<OffsetDateTime> param, OffsetDateTime value, TemporalType temporalType) {
		super.setParameter( param, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, Instant value, TemporalType temporalType) {
		super.setParameter( name, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, LocalDateTime value, TemporalType temporalType) {
		super.setParameter( name, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, ZonedDateTime value, TemporalType temporalType) {
		super.setParameter( name, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, OffsetDateTime value, TemporalType temporalType) {
		super.setParameter( name, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, Instant value, TemporalType temporalType) {
		super.setParameter( position, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, LocalDateTime value, TemporalType temporalType) {
		super.setParameter( position, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, ZonedDateTime value, TemporalType temporalType) {
		super.setParameter( position, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, OffsetDateTime value, TemporalType temporalType) {
		super.setParameter( position, value, temporalType );
		return this;
	}

	@Override
	public <P> ProcedureCallImplementor<R> setParameter(QueryParameter<P> parameter, P value) {
		super.setParameter( parameter, value );
		return this;
	}

	@Override
	public <P> ProcedureCallImplementor<R> setParameter(Parameter<P> parameter, P value) {
		super.setParameter( parameter, value );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, Object value) {
		super.setParameter( name, value );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, Object value) {
		super.setParameter( position, value );
		return this;
	}

	@Override
	public <P> ProcedureCallImplementor<R> setParameter(QueryParameter<P> parameter, P value, AllowableParameterType type) {
		super.setParameter( parameter, value, type );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, Object value, AllowableParameterType type) {
		super.setParameter( name, value, type );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, Object value, AllowableParameterType type) {
		super.setParameter( position, value, type );
		return this;
	}

	@Override
	public <P> ProcedureCallImplementor<R> setParameter(QueryParameter<P> parameter, P value, TemporalType temporalType) {
		super.setParameter( parameter, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, Object value, TemporalType temporalType) {
		super.setParameter( name, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, Object value, TemporalType temporalType) {
		super.setParameter( position, value, temporalType );
		return this;
	}

	@Override
	public <P> ProcedureCallImplementor<R> setParameterList(QueryParameter<P> parameter, Collection<P> values) {
		super.setParameterList( parameter, values );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(String name, Collection values) {
		super.setParameterList( name, values );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(String name, Collection values, AllowableParameterType type) {
		super.setParameterList( name, values, type );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(int position, Collection values, AllowableParameterType type) {
		super.setParameterList( position, values, type );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(String name, Object[] values) {
		super.setParameterList( name, values );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(String name, Object[] values, AllowableParameterType type) {
		super.setParameterList( name, values, type );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(int position, Object[] values, AllowableParameterType type) {
		super.setParameterList( position, values, type );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(String name, Collection values, Class type) {
		super.setParameterList( name, values, type );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameterList(int position, Collection values, Class type) {
		super.setParameterList( position, values, type );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
		super.setParameter( param, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
		super.setParameter( param, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, Calendar value, TemporalType temporalType) {
		super.setParameter( name, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, Date value, TemporalType temporalType) {
		super.setParameter( name, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, Calendar value, TemporalType temporalType) {
		super.setParameter( position, value, temporalType );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, Date value, TemporalType temporalType) {
		super.setParameter( position, value, temporalType );
		return this;
	}

	@Override
	public NamedCallableQueryMemento toMemento(String name, SessionFactoryImplementor factory) {
		return new NamedCallableQueryMementoImpl(
				name,
				procedureName,
				getParameterMetadata().getParameterStrategy(),
				toParameterMementos( getParameterMetadata() ),
				// row-readers
				null,
				synchronizedQuerySpaces,
				isCacheable(),
				getCacheRegion(),
				getCacheMode(),
				getHibernateFlushMode(),
				isReadOnly(),
				getLockOptions(),
				getTimeout(),
				getFetchSize(),
				getComment(),
				Util.copy( getHints() )
		);
	}

	private static List<ParameterMemento> toParameterMementos(ProcedureParameterMetadata<ProcedureParameterImplementor<?>> parameterMetadata) {
		if ( parameterMetadata.getParameterStrategy() == ParameterStrategy.UNKNOWN ) {
			// none...
			return Collections.emptyList();
		}

		final List<ParameterMemento> copy = new ArrayList<>();
		parameterMetadata.visitRegistrations( queryParameter -> copy.add( queryParameter.toMemento() ) );
		return copy;
	}

	@Override
	public ParameterBindingContext getParameterBindingContext() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public Callback getCallback() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public List<org.hibernate.sql.results.spi.ResultSetMappingDescriptor> getResultSetMappings() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public JDBCException convertException(SQLException e, String message) {
		throw new NotYetImplementedFor6Exception();
	}
}
