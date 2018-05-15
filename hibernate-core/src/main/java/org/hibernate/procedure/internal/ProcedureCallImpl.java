/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.procedure.internal;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceException;
import javax.persistence.TemporalType;
import javax.persistence.TransactionRequiredException;

import org.hibernate.HibernateException;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryReturn;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.ScrollMode;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.graph.spi.EntityGraphImplementor;
import org.hibernate.internal.log.DeprecationLogger;
import org.hibernate.jpa.internal.util.ConfigurationHelper;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.procedure.NoMoreReturnsException;
import org.hibernate.procedure.NoSuchParameterException;
import org.hibernate.procedure.Output;
import org.hibernate.procedure.ParameterMisuseException;
import org.hibernate.procedure.ParameterRegistration;
import org.hibernate.procedure.ParameterStrategyException;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.procedure.ProcedureCallMemento;
import org.hibernate.procedure.ProcedureOutputs;
import org.hibernate.procedure.ResultSetOutput;
import org.hibernate.procedure.UpdateCountOutput;
import org.hibernate.procedure.spi.CallableStatementSupport;
import org.hibernate.procedure.spi.ParameterRegistrationImplementor;
import org.hibernate.procedure.spi.ParameterStrategy;
import org.hibernate.procedure.spi.ProcedureCallImplementor;
import org.hibernate.query.QueryParameter;
import org.hibernate.query.internal.AbstractQuery;
import org.hibernate.query.procedure.internal.ProcedureParamBindings;
import org.hibernate.query.internal.QueryOptionsImpl;
import org.hibernate.query.named.spi.NamedCallableQueryDescriptor;
import org.hibernate.query.spi.MutableQueryOptions;
import org.hibernate.query.spi.ResultSetMappingDescriptor;
import org.hibernate.query.spi.ScrollableResultsImplementor;
import org.hibernate.sql.exec.internal.JdbcCallImpl;
import org.hibernate.query.procedure.spi.ProcedureParameterImplementor;
import org.hibernate.query.spi.QueryParameterBinding;
import org.hibernate.query.spi.QueryParameterBindings;
import org.hibernate.sql.exec.internal.JdbcCallParameterBinderImpl;
import org.hibernate.sql.exec.internal.JdbcCallParameterExtractorImpl;
import org.hibernate.sql.exec.internal.JdbcCallParameterRegistrationImpl;
import org.hibernate.sql.exec.internal.JdbcCallRefCursorExtractorImpl;
import org.hibernate.sql.results.internal.RowReaderNoResultsExpectedImpl;
import org.hibernate.sql.results.internal.RowReaderStandardImpl;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultAssembler;
import org.hibernate.sql.results.spi.RowReader;
import org.hibernate.sql.exec.spi.JdbcParameterBinder;
import org.hibernate.type.Type;

/**
 * Standard implementation of {@link org.hibernate.procedure.ProcedureCall}
 *
 * @author Steve Ebersole
 */
public class ProcedureCallImpl<R>
		extends AbstractQuery<R>
		implements ProcedureCallImplementor<R> {
	private final String procedureName;
	private final Set<String> synchronizedQuerySpaces;

	private final RowReader<R> rowReader;
	private final ParameterManager parameterManager;

	private final ProcedureParameterMetadata parameterMetadata;
	private final ProcedureParamBindings paramBindings;

	private FunctionReturnImpl functionReturn;

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
		super( session, null );
		this.procedureName = procedureName;

		this.parameterManager = new ParameterManager( this );

		this.synchronizedQuerySpaces = Collections.emptySet();
		this.rowReader = RowReaderNoResultsExpectedImpl.instance();

		this.parameterMetadata = new ProcedureParameterMetadata( this );
		this.paramBindings = new ProcedureParamBindings( parameterMetadata, this );
	}

	/**
	 * The result Class(es) return form
	 *
	 * @param session The session
	 * @param procedureName The name of the procedure to call
	 * @param resultClasses The classes making up the result
	 */
	public ProcedureCallImpl(final SharedSessionContractImplementor session, String procedureName, Class... resultClasses) {
		super( session, null );
		this.procedureName = procedureName;

		this.parameterManager = new ParameterManager( this );

		final Set<String> querySpaces = new HashSet<>();
		final List<QueryResultAssembler> returnAssemblers = new ArrayList<>();
		final List<Initializer> initializers = new ArrayList<>();

		Util.resolveResultClasses(
				new Util.ResultClassesResolutionContext() {
					@Override
					public SessionFactoryImplementor getSessionFactory() {
						return session.getFactory();
					}

					@Override
					public void addQuerySpaces(String... spaces) {
						Collections.addAll( querySpaces, spaces );
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
		this.rowReader = new RowReaderStandardImpl<>(
				returnAssemblers,
				initializers,
				null,
				null
		);

		this.parameterMetadata = new ProcedureParameterMetadata( this );
		this.paramBindings = new ProcedureParamBindings( parameterMetadata, this );
	}

	/**
	 * The result-set-mapping(s) return form
	 *
	 * @param session The session
	 * @param procedureName The name of the procedure to call
	 * @param resultSetMappings The names of the result set mappings making up the result
	 */
	public ProcedureCallImpl(final SharedSessionContractImplementor session, String procedureName, String... resultSetMappings) {
		super( session, null );
		this.procedureName = procedureName;

		this.parameterManager = new ParameterManager( this );

		final Set<String> querySpaces = new HashSet<>();
		final List<QueryResultAssembler> returnAssemblers = new ArrayList<>();
		final List<Initializer> initializers = new ArrayList<>();

		Util.resolveResultSetMappings(
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
				resultSetMappings
		);

		this.synchronizedQuerySpaces = Collections.unmodifiableSet( querySpaces );
		this.rowReader = new RowReaderStandardImpl<>(
				returnAssemblers,
				initializers,
				null,
				null
		);

		this.parameterMetadata = new ProcedureParameterMetadata( this );
		this.paramBindings = new ProcedureParamBindings( parameterMetadata, this );
	}

	/**
	 * The named/stored copy constructor
	 *
	 * @param session The session
	 * @param memento The named/stored memento
	 */
	@SuppressWarnings("unchecked")
	ProcedureCallImpl(SharedSessionContractImplementor session, ProcedureCallMementoImpl memento) {
		super( session, null );
		this.procedureName = memento.getProcedureName();

		this.parameterManager = new ParameterManager( this );
		this.parameterManager.registerParameters( memento );

		this.synchronizedQuerySpaces = Util.copy( memento.getSynchronizedQuerySpaces() );

		this.parameterMetadata = new ProcedureParameterMetadata( this );
		this.paramBindings = new ProcedureParamBindings( parameterMetadata, this );

		this.rowReader = memento.getRowReader();

		for ( Map.Entry<String, Object> entry : memento.getHintsMap().entrySet() ) {
			setHint( entry.getKey(), entry.getValue() );
		}
	}

	@Override
	public String getProcedureName() {
		return procedureName;
	}

	@Override
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
	protected ParameterManager queryParameterBindings() {
		return parameterManager;
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
	public <T> ParameterRegistration<T> registerParameter(int position, Class<T> type, ParameterMode mode) {
		final ProcedureParameterImpl procedureParameter = new ProcedureParameterImpl(
				this,
				position,
				mode,
				type,
				getSession().getFactory().getTypeResolver().heuristicType( type.getName() ),
				globalParameterPassNullsSetting
		);

		registerParameter( procedureParameter );
		return procedureParameter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCall registerParameter0(int position, Class type, ParameterMode mode) {
		parameterManager.registerParameter( position, mode, type );
		return this;
	}

	private void registerParameter(ProcedureParameterImplementor parameter) {
		getParameterMetadata().registerParameter( parameter );
	}

	@Override
	@SuppressWarnings("unchecked")
	public ParameterRegistrationImplementor getParameterRegistration(int position) {
		return getParameterMetadata().getQueryParameter( position );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> ParameterRegistration<T> registerParameter(String name, Class<T> type, ParameterMode mode) {
		final ProcedureParameterImpl parameter = new ProcedureParameterImpl(
				this,
				name,
				mode,
				type,
				getSession().getFactory().getTypeResolver().heuristicType( type.getName() ),
				globalParameterPassNullsSetting
		);

		registerParameter( parameter );

		return parameter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCall registerParameter0(String name, Class type, ParameterMode mode) {
		parameterManager.registerParameter( name, mode, type );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ParameterRegistrationImplementor getParameterRegistration(String name) {
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
		parameterManager.validate();

		if ( isFunctionCall() ) {
			if ( !parameterManager.hasAnyParameterRegistrations() ) {
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

		// todo (6.0) : consider moving the responsibility for creating JdbcCall to CallableStatementSupport
		//		that fixes the disjunct between its current `#shouldUseFunctionSyntax` and
		//		what JDBC type to use for that return

		JdbcCallImpl.Builder jdbcCallBuilder = new JdbcCallImpl.Builder(
				getProcedureName(),
				parameterManager.getParameterStrategy()
		);

		// positional parameters are 0-based..
		//		JDBC positions (1-based) come into play later, although we could calculate them here as well
		int parameterPosition = 0;

		final FunctionReturnImpl functionReturnToUse;
		if ( this.functionReturn != null ) {
			// user explicitly defined the function return via native API - good for them :)
			functionReturnToUse = this.functionReturn;
		}
		else {
			final boolean useFunctionCallSyntax = isFunctionCall()
					|| callableStatementSupport.shouldUseFunctionSyntax( parameterManager );

			if ( useFunctionCallSyntax ) {
				// todo : how to determine the JDBC type code here?
				functionReturnToUse = new FunctionReturnImpl( this, Types.VARCHAR );
				parameterPosition++;
			}
			else {
				functionReturnToUse = null;
			}
		}

		if ( functionReturnToUse != null ) {
			jdbcCallBuilder.addParameterRegistration( functionReturnToUse.toJdbcCallParameterRegistration( getSession() ) );

			// function returns use a parameter
			parameterPosition++;
		}

		for ( ParameterRegistrationImplementor registration : parameterManager.getParameterRegistrations() ) {
			final JdbcCallParameterRegistrationImpl jdbcRegistration;

		final String call = getProducer().getJdbcServices().getJdbcEnvironment().getDialect().getCallableStatementSupport().renderCallableStatement(
				procedureName,
				getParameterMetadata(),
				paramBindings,
				getProducer()
		);

		LOG.debugf( "Preparing procedure call : %s", call );
		final CallableStatement statement = (CallableStatement) getSession()
				.getJdbcCoordinator()
				.getStatementPreparer()
				.prepareStatement( call, true );

				jdbcRegistration = new JdbcCallParameterRegistrationImpl(
						registration.getName(),
						parameterPosition,
						ParameterMode.REF_CURSOR,
						Types.REF_CURSOR,
						ormType,
						parameterBinder,
						parameterExtractor,
						null
				);
			}

		// prepare parameters

		getParameterMetadata().visitRegistrations(
				new Consumer<QueryParameter>() {
					int i = 1;

					@Override
					public void accept(QueryParameter queryParameter) {
						try {
							final ParameterRegistrationImplementor registration = (ParameterRegistrationImplementor) queryParameter;
							registration.prepare( statement, i );
							if ( registration.getMode() == ParameterMode.REF_CURSOR ) {
								i++;
							}
							else {
								i += registration.getSqlTypes().length;
							}
						}
						catch (SQLException e) {
							throw getSession().getJdbcServices().getSqlExceptionHelper().convert(
									e,
									"Error preparing registered callable parameter",
									getProcedureName()
							);
						}
					}
				}
		);

		return new ProcedureOutputsImpl( this, statement );
	}

		return parameterStrategy;
	}

	private AllowableParameterType determineTypeToUse(ParameterRegistrationImplementor parameterRegistration) {
		if ( parameterRegistration.getBind() != null ) {
			if ( parameterRegistration.getBind().getBindType() != null ) {
				return parameterRegistration.getBind().getBindType();
			}
		}

		if ( parameterRegistration.getHibernateType() != null ) {
			return parameterRegistration.getHibernateType();
		}

		return null;
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
		addSynchronizedQuerySpaces( getSession().getFactory().getTypeConfiguration().findEntityDescriptor( entityName ) );
		return this;
	}

	@SuppressWarnings("WeakerAccess")
	protected void addSynchronizedQuerySpaces(EntityDescriptor persister) {
		synchronizedQuerySpaces().addAll( Arrays.asList( persister.getAffectedTableNames() ) );
	}

	@Override
	public ProcedureCallImplementor<R> addSynchronizedEntityClass(Class entityClass) {
		addSynchronizedQuerySpaces( getSession().getFactory().getTypeConfiguration().findEntityDescriptor( entityClass.getName() ) );
		return this;
	}

	@Override
	protected boolean isNativeQuery() {
		return false;
	}

	@Override
	public QueryParameters getQueryParameters() {
		final QueryParameters qp = super.getQueryParameters();
		// both of these are for documentation purposes, they are actually handled directly...
		qp.setAutoDiscoverScalarTypes( true );
		qp.setCallable( true );
		return qp;
	}

	/**
	 * Collects any parameter registrations which indicate a REF_CURSOR parameter type/mode.
	 *
	 * @return The collected REF_CURSOR type parameters.
	 */
	public ParameterRegistrationImplementor[] collectRefCursorParameters() {
		final List<ParameterRegistrationImplementor> refCursorParams = new ArrayList<>();

		getParameterMetadata().visitRegistrations(
				queryParameter -> {
					final ParameterRegistrationImplementor registration = (ParameterRegistrationImplementor) queryParameter;
					if ( registration.getMode() == ParameterMode.REF_CURSOR ) {
						refCursorParams.add( registration );
					}
				}
		);
		return refCursorParams.toArray( new ParameterRegistrationImplementor[refCursorParams.size()] );
	}

	@Override
	public ProcedureCallMemento extractMemento(Map<String, Object> hints) {
		return new ProcedureCallMementoImpl(
				procedureName,
				Util.copy( queryReturns ),
				getParameterMetadata().getParameterStrategy(),
				toParameterMementos( getParameterMetadata() ),
				Util.copy( synchronizedQuerySpaces ),
				Util.copy( hints )
		);
	}

	@Override
	public ProcedureCallMemento extractMemento() {
		return new ProcedureCallMementoImpl(
				procedureName,
				Util.copy( queryReturns ),
				getParameterMetadata().getParameterStrategy(),
				toParameterMementos( getParameterMetadata() ),
				Util.copy( synchronizedQuerySpaces ),
				Util.copy( getHints() )
		);
	}

	private static List<ProcedureCallMementoImpl.ParameterMemento> toParameterMementos(ProcedureParameterMetadata parameterMetadata) {
		if ( parameterMetadata.getParameterStrategy() == ParameterStrategy.UNKNOWN ) {
			// none...
			return Collections.emptyList();
		}

		final List<ProcedureCallMementoImpl.ParameterMemento> copy = new ArrayList<>();

		parameterMetadata.visitRegistrations(
				queryParameter -> {
					final ParameterRegistrationImplementor registration = (ParameterRegistrationImplementor) queryParameter;
					copy.add( ProcedureCallMementoImpl.ParameterMemento.fromRegistration( registration ) );
				}
		);

		return copy;
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
			return rtn != null && ResultSetOutput.class.isInstance( rtn );
		}
		catch (NoMoreReturnsException e) {
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
		catch (NoMoreReturnsException e) {
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
		catch (NoMoreReturnsException e) {
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

		if ( cls.isInstance( parameterManager ) ) {
			return (T) parameterManager;
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
		else if ( IS_FUNCTION_HINT.equals( hintName ) ) {
			DeprecationLogger.DEPRECATION_LOGGER.logDeprecatedQueryHint( IS_FUNCTION_HINT, FUNCTION_RETURN_TYPE_HINT );
			// make a guess as to the type - for now VARCHAR
			// todo : how best to determine JDBC Types code?
			markAsFunctionCall( Types.VARCHAR );
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

	// todo (5.3) : all of the parameter stuff here can be done in AbstractProducedQuery
	//		using #getParameterMetadata and #getQueryParameterBindings for abstraction.
	//		this "win" is to define these in one place

	@Override
	public <P> ProcedureCallImplementor<R> setParameter(QueryParameter<P> parameter, P value) {
		paramBindings.getBinding( getParameterMetadata().resolve( parameter ) ).setBindValue( value );
		return this;
	}

	@Override
	public <P> ProcedureCallImplementor<R> setParameter(Parameter<P> parameter, P value) {
		paramBindings.getBinding( getParameterMetadata().resolve( parameter ) ).setBindValue( value );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(String name, Object value) {
		paramBindings.getBinding( getParameterMetadata().getQueryParameter( name ) ).setBindValue( value );
		return this;
	}

	@Override
	public ProcedureCallImplementor<R> setParameter(int position, Object value) {
		paramBindings.getBinding( getParameterMetadata().getQueryParameter( position ) ).setBindValue( value );
		return this;
	}

	@Override
	public <P> ProcedureCallImplementor<R> setParameter(QueryParameter<P> parameter, P value, Type type) {
		final QueryParameterBinding<P> binding = paramBindings.getBinding( parameter );
		binding.setBindValue( value, type );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(String name, Object value, Type type) {
		final QueryParameterBinding binding = paramBindings.getBinding( getParameterMetadata().getQueryParameter( name ) );
		binding.setBindValue( value, type );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(int position, Object value, Type type) {
		final QueryParameterBinding binding = paramBindings.getBinding( getParameterMetadata().getQueryParameter( position ) );
		binding.setBindValue( value, type );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <P> ProcedureCallImplementor<R> setParameter(QueryParameter<P> parameter, P value, TemporalType temporalType) {
		final QueryParameterBinding binding = paramBindings.getBinding( parameter );
		binding.setBindValue( value, temporalType );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(String name, Object value, TemporalType temporalType) {
		final QueryParameterBinding binding = paramBindings.getBinding( getParameterMetadata().getQueryParameter( name ) );
		binding.setBindValue( value, temporalType );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(int position, Object value, TemporalType temporalType) {
		final QueryParameterBinding binding = paramBindings.getBinding( getParameterMetadata().getQueryParameter( position ) );
		binding.setBindValue( value, temporalType );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(Parameter parameter, Calendar value, TemporalType temporalType) {
		final QueryParameterBinding binding = paramBindings.getBinding( getParameterMetadata().resolve( parameter ) );
		binding.setBindValue( value, temporalType );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(Parameter parameter, Date value, TemporalType temporalType) {
		final QueryParameterBinding binding = paramBindings.getBinding( getParameterMetadata().resolve( parameter ) );
		binding.setBindValue( value, temporalType );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(String name, Calendar value, TemporalType temporalType) {
		final QueryParameterBinding binding = paramBindings.getBinding( name );
		binding.setBindValue( value, temporalType );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(String name, Date value, TemporalType temporalType) {
		final QueryParameterBinding binding = paramBindings.getBinding( name );
		binding.setBindValue( value, temporalType );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(int position, Calendar value, TemporalType temporalType) {
		final QueryParameterBinding binding = paramBindings.getBinding( position );
		binding.setBindValue( value, temporalType );
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public ProcedureCallImplementor<R> setParameter(int position, Date value, TemporalType temporalType) {
		final QueryParameterBinding binding = paramBindings.getBinding( position );
		binding.setBindValue( value, temporalType );
		return this;
	}

//	@Override
//	public Set<Parameter<?>> getParameters() {
//		if ( !parameterManager.hasAnyParameterRegistrations() ) {
//			return Collections.emptySet();
//		}
//
//		return parameterManager
//				.collectParameterRegistrations()
//				.stream()
//				.map( parameter -> (Parameter<?>) parameter )
//				.collect( Collectors.toSet() );
//	}
//
//	@Override
//	public NamedCallableQueryDescriptor toNamedDescriptor(String name) {
//		// todo (6.0) : iiuc NamedCallableQueryDescriptor and ProcedureCallMemento serve the same purpose
//		//		replace ProcedureCallMemento (and friends) with NamedCallableQueryDescriptor
//		throw new NotYetImplementedFor6Exception();
//	}
}
