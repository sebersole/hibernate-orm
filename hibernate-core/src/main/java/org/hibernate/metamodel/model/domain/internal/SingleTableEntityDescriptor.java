/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.AttributeOverride;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.SecondaryTable;
import javax.persistence.Table;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.boot.model.domain.EntityMapping;
import org.hibernate.bytecode.enhance.spi.LazyPropertyInitializer;
import org.hibernate.cache.spi.entry.CacheEntry;
import org.hibernate.cache.spi.entry.CacheEntryStructure;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.ValueInclusion;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.internal.FilterAliasGenerator;
import org.hibernate.loader.internal.TemplateParameterBindingContext;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractEntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.IdentifiableTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.query.internal.QueryOptionsImpl;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableContainerReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.consume.spi.InsertToJdbcInsertConverter;
import org.hibernate.sql.ast.consume.spi.SqlDeleteToJdbcDeleteConverter;
import org.hibernate.sql.ast.consume.spi.UpdateToJdbcUpdateConverter;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.SqlAstDeleteDescriptor;
import org.hibernate.sql.ast.produce.sqm.spi.Callback;
import org.hibernate.sql.ast.tree.spi.DeleteStatement;
import org.hibernate.sql.ast.tree.spi.InsertStatement;
import org.hibernate.sql.ast.tree.spi.UpdateStatement;
import org.hibernate.sql.ast.tree.spi.assign.Assignment;
import org.hibernate.sql.ast.tree.spi.expression.ColumnReference;
import org.hibernate.sql.ast.tree.spi.expression.LiteralParameter;
import org.hibernate.sql.ast.tree.spi.from.TableReference;
import org.hibernate.sql.ast.tree.spi.predicate.Junction;
import org.hibernate.sql.ast.tree.spi.predicate.RelationalPredicate;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.exec.spi.JdbcMutationExecutor;
import org.hibernate.sql.exec.spi.ParameterBindingContext;
import org.hibernate.sql.results.spi.SqlAstCreationContext;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class SingleTableEntityDescriptor<T> extends AbstractEntityDescriptor<T> {

	public SingleTableEntityDescriptor(
			EntityMapping bootMapping,
			IdentifiableTypeDescriptor<? super T> superTypeDescriptor,
			RuntimeModelCreationContext creationContext) throws HibernateException {
		super( bootMapping, superTypeDescriptor, creationContext );
	}


	// `select ... from Person p order by p`
	@Override
	public SqmNavigableReference createSqmExpression(
			SqmFrom sourceSqmFrom,
			SqmNavigableContainerReference containerReference,
			SqmCreationContext creationContext) {
		return sourceSqmFrom.getNavigableReference();
	}

	@Override
	public List<ColumnReference> resolveColumnReferences(
			ColumnReferenceQualifier qualifier, SqlAstCreationContext resolutionContext) {
		return getIdentifierDescriptor().resolveColumnReferences( qualifier, resolutionContext );
	}

	@Override
	public String asLoggableText() {
		return String.format( "SingleTableEntityDescriptor<%s>", getEntityName() );

	}

	@Override
	public Set<String> getAffectedTableNames() {
		return Collections.emptySet();
	}

	@Override
	public int[] findDirty(
			Object[] currentState,
			Object[] previousState,
			Object owner,
			SharedSessionContractImplementor session) {
		final List<Integer> results = new ArrayList<>();

		visitStateArrayContributors(
				contributor -> {
					final int index = contributor.getStateArrayPosition();
					final boolean dirty = currentState[index] != LazyPropertyInitializer.UNFETCHED_PROPERTY &&
							( previousState[index] == LazyPropertyInitializer.UNFETCHED_PROPERTY ||
									( contributor.isIncludedInDirtyChecking() &&
											contributor.isDirty( previousState[index], currentState[index], session ) ) );

					if ( dirty ) {
						results.add( index );
					}
				}
		);

		if ( results.size() == 0 ) {
			return null;
		}
		else {
			return results.stream().mapToInt( i-> i ).toArray();
		}
	}

	@Override
	public int[] findModified(
			Object[] old, Object[] current, Object object, SharedSessionContractImplementor session) {
		return new int[0];
	}

	@Override
	public void lock(
			Object id, Object version, Object object, LockMode lockMode, SharedSessionContractImplementor session)
			throws HibernateException {

	}

	@Override
	public void lock(
			Object id,
			Object version,
			Object object,
			LockOptions lockOptions,
			SharedSessionContractImplementor session) throws HibernateException {

	}

	protected Object insertInternal(
			Object id,
			Object[] fields,
			Object object,
			SharedSessionContractImplementor session) {
		// generate id if needed
		if ( id == null ) {
			final IdentifierGenerator generator = getHierarchy().getIdentifierDescriptor().getIdentifierValueGenerator();
			if ( generator != null ) {
				id = generator.generate( session, object );
			}
		}

		final Object unresolvedId = getHierarchy().getIdentifierDescriptor().unresolve( id, session );
		final ExecutionContext executionContext = getExecutionContext( session );

		// for now - just root table
		// for now - we also regenerate these SQL AST objects each time - we can cache these
		executeInsert( fields, session, unresolvedId, executionContext, new TableReference( getPrimaryTable(), null) );

		getSecondaryTableBindings().forEach(
				tableBindings -> executeInsert(
						fields,
						session,
						unresolvedId,
						executionContext,
						new TableReference( tableBindings.getReferringTable(), null )
				)
		);

		return id;
	}

	private ExecutionContext getExecutionContext(SharedSessionContractImplementor session) {
		return new ExecutionContext() {
			private final ParameterBindingContext parameterBindingContext = new TemplateParameterBindingContext( session.getFactory() );

			@Override
			public SharedSessionContractImplementor getSession() {
				return session;
			}

			@Override
			public QueryOptions getQueryOptions() {
				return new QueryOptionsImpl();
			}

			@Override
			public ParameterBindingContext getParameterBindingContext() {
				return parameterBindingContext;
			}

			@Override
			public Callback getCallback() {
				return afterLoadAction -> {
				};
			}
		};
	}

	private void executeInsert(
			Object[] fields,
			SharedSessionContractImplementor session,
			Object unresolvedId,
			ExecutionContext executionContext,
			TableReference tableReference) {
		final InsertStatement insertStatement = new InsertStatement( tableReference );

		// todo (6.0) : account for non-generated identifiers

		getHierarchy().getIdentifierDescriptor().dehydrate(
				getHierarchy().getIdentifierDescriptor().unresolve( unresolvedId, session ),
				(jdbcValue, type, boundColumn) -> {
					insertStatement.addTargetColumnReference( new ColumnReference( boundColumn ) );
					insertStatement.addValue(
							new LiteralParameter(
									jdbcValue,
									boundColumn.getExpressableType(),
									Clause.INSERT,
									session.getFactory().getTypeConfiguration()
							)
					);
				},
				Clause.INSERT,
				session
		);

		if ( getHierarchy().getDiscriminatorDescriptor() != null ) {
			insertStatement.addTargetColumnReference(
					new ColumnReference( getHierarchy().getDiscriminatorDescriptor().getBoundColumn() )
			);
			insertStatement.addValue(
					new LiteralParameter(
							getHierarchy().getDiscriminatorDescriptor().unresolve( getDiscriminatorValue(), session ),
							getHierarchy().getDiscriminatorDescriptor().getBoundColumn().getExpressableType(),
							Clause.INSERT,
							session.getFactory().getTypeConfiguration()
					)
			);
		}

		if ( getHierarchy().getTenantDiscrimination() != null ) {
			insertStatement.addTargetColumnReference(
					new ColumnReference( getHierarchy().getTenantDiscrimination().getBoundColumn() )
			);
			insertStatement.addValue(
					new LiteralParameter(
							getHierarchy().getTenantDiscrimination().unresolve( session.getTenantIdentifier(), session ),
							getHierarchy().getTenantDiscrimination().getBoundColumn().getExpressableType(),
							Clause.INSERT,
							session.getFactory().getTypeConfiguration()
					)
			);
		}

		visitStateArrayContributors(
				contributor -> {
					int position = contributor.getStateArrayPosition();
					final Object domainValue = fields[position];
					contributor.dehydrate(
							contributor.unresolve( domainValue, session ),
							(jdbcValue, type, boundColumn) -> {
								if ( boundColumn.getSourceTable().equals( tableReference.getTable() ) ) {
									insertStatement.addTargetColumnReference( new ColumnReference( boundColumn ) );
									insertStatement.addValue(
											new LiteralParameter(
													jdbcValue,
													type,
													Clause.INSERT,
													session.getFactory().getTypeConfiguration()
											)
									);
								}
							},
							Clause.INSERT,
							session
					);
				}
		);

		JdbcMutationExecutor.WITH_AFTER_STATEMENT_CALL.execute(
				InsertToJdbcInsertConverter.createJdbcInsert(
						insertStatement,
						executionContext.getSession().getSessionFactory()
				),
				executionContext,
				Connection::prepareStatement
		);
	}

	@Override
	public void delete(
			Object id,
			Object version,
			Object object,
			SharedSessionContractImplementor session)
			throws HibernateException {

		// todo (6.0) - initial basic pass at entity deletes

		final Object unresolvedId = getHierarchy().getIdentifierDescriptor().unresolve( id, session );
		final ExecutionContext executionContext = getExecutionContext( session );

		final TableReference tableReference = new TableReference( getPrimaryTable(), null );

		Junction identifierJunction = new Junction( Junction.Nature.CONJUNCTION );
		getHierarchy().getIdentifierDescriptor().dehydrate(
				unresolvedId,
				(jdbcValue, type, boundColumn) -> {
					identifierJunction.add(
							new RelationalPredicate(
									RelationalPredicate.Operator.EQUAL,
									new ColumnReference( boundColumn ),
									new LiteralParameter(
											jdbcValue,
											boundColumn.getExpressableType(),
											Clause.DELETE,
											session.getFactory().getTypeConfiguration()
									)
							)
					);
				},
				Clause.DELETE,
				session
		);

		final DeleteStatement deleteStatement = new DeleteStatement( tableReference, identifierJunction );

		JdbcMutationExecutor.WITH_AFTER_STATEMENT_CALL.execute(
				SqlDeleteToJdbcDeleteConverter.interpret(
						new SqlAstDeleteDescriptor() {
							@Override
							public DeleteStatement getSqlAstStatement() {
								return deleteStatement;
							}

							@Override
							public Set<String> getAffectedTableNames() {
								return Collections.singleton(
										deleteStatement.getTargetTable().getTable().getTableExpression()
								);
							}
						},
						executionContext.getSession().getSessionFactory()
				),
				executionContext,
				Connection::prepareStatement
		);
	}

	@Embeddable
	class Name {
		public String fname;
		public String lname;
	}

	@Entity
	@Table( name = "t1" )
	@SecondaryTable( name = "t2" )
	class Person {
		public Integer id;

		@Embedded
		@AttributeOverride( name = "fname", column = @javax.persistence.Column( table = "t1" ) )
		@AttributeOverride( name = "lname", column = @javax.persistence.Column( table = "t2" ) )
		public Name name;
	}


	@Override
	public void update(
			Object id,
			Object[] fields,
			int[] dirtyFields,
			boolean hasDirtyCollection,
			Object[] oldFields,
			Object oldVersion,
			Object object,
			Object rowId,
			SharedSessionContractImplementor session) throws HibernateException {

		// todo (6.0) - initial basic pass at entity updates

		final Object unresolvedId = getHierarchy().getIdentifierDescriptor().unresolve( id, session );
		final ExecutionContext executionContext = getExecutionContext( session );

		final TableReference tableReference = new TableReference( getPrimaryTable(), null );

		List<Assignment> assignments = new ArrayList<>();
		for ( int dirtyField : dirtyFields ) {
			final StateArrayContributor contributor = getStateArrayContributors().get( dirtyField );
			final Object domainValue = fields[contributor.getStateArrayPosition()];
			List<Column> columns = contributor.getColumns();
			if ( columns != null && !columns.isEmpty() ) {
				if ( contributor.isUpdatable() ) {
					contributor.dehydrate(
							contributor.unresolve( domainValue, session ),
							(jdbcValue, type, boundColumn) -> {
								if ( boundColumn.getSourceTable().equals( tableReference.getTable() ) ) {
									assignments.add(
											new Assignment(
													new ColumnReference( boundColumn ),
													new LiteralParameter(
															jdbcValue,
															boundColumn.getExpressableType(),
															Clause.UPDATE,
															session.getFactory().getTypeConfiguration()
													)
											)
									);
								}
							},
							Clause.UPDATE,
							session
					);
				}
			}
		}

		Junction identifierJunction = new Junction( Junction.Nature.CONJUNCTION );
		getHierarchy().getIdentifierDescriptor().dehydrate(
				unresolvedId,
				(jdbcValue, type, boundColumn) -> {
					identifierJunction.add(
							new RelationalPredicate(
									RelationalPredicate.Operator.EQUAL,
									new ColumnReference( boundColumn ),
									new LiteralParameter( jdbcValue, boundColumn.getExpressableType(), Clause.UPDATE, session.getFactory().getTypeConfiguration()  )
							)
					);
				},
				Clause.UPDATE,
				session
		);

		// todo (6.0) : depending on optimistic-lock strategy may need to adjust where clause

		final UpdateStatement updateStatement = new UpdateStatement(
				tableReference,
				assignments,
				identifierJunction
		);

		JdbcMutationExecutor.WITH_AFTER_STATEMENT_CALL.execute(
				UpdateToJdbcUpdateConverter.createJdbcUpdate(
						updateStatement,
						executionContext.getSession().getSessionFactory()
				),
				executionContext,
				Connection::prepareStatement
		);
	}

	@Override
	public Type[] getPropertyTypes() {
		return new Type[0];
	}

	@Override
	public JavaTypeDescriptor[] getPropertyJavaTypeDescriptors() {
		return null;
	}

	@Override
	public String[] getPropertyNames() {
		return new String[0];
	}

	@Override
	public boolean[] getPropertyInsertability() {
		return new boolean[0];
	}

	@Override
	public ValueInclusion[] getPropertyInsertGenerationInclusions() {
		return new ValueInclusion[0];
	}

	@Override
	public ValueInclusion[] getPropertyUpdateGenerationInclusions() {
		return new ValueInclusion[0];
	}

	@Override
	public boolean[] getPropertyUpdateability() {
		return new boolean[0];
	}

	@Override
	public boolean[] getPropertyCheckability() {
		return new boolean[0];
	}

	@Override
	public boolean[] getPropertyNullability() {
		return new boolean[0];
	}

	@Override
	public boolean[] getPropertyVersionability() {
		return new boolean[0];
	}

	@Override
	public boolean[] getPropertyLaziness() {
		return new boolean[0];
	}

	@Override
	public CascadeStyle[] getPropertyCascadeStyles() {
		return new CascadeStyle[0];
	}

	@Override
	public boolean hasCascades() {
		return false;
	}

	@Override
	public Type getIdentifierType() {
		return null;
	}

	@Override
	public String getIdentifierPropertyName() {
		return null;
	}

	@Override
	public boolean isCacheInvalidationRequired() {
		return false;
	}

	@Override
	public boolean isLazyPropertiesCacheable() {
		return false;
	}

	@Override
	public CacheEntryStructure getCacheEntryStructure() {
		return null;
	}

	@Override
	public CacheEntry buildCacheEntry(
			Object entity, Object[] state, Object version, SharedSessionContractImplementor session) {
		return null;
	}

	@Override
	public boolean isBatchLoadable() {
		return false;
	}

	@Override
	public boolean isSelectBeforeUpdateRequired() {
		return false;
	}

	@Override
	public Object[] getDatabaseSnapshot(Object id, SharedSessionContractImplementor session)
			throws HibernateException {
		return new Object[0];
	}

	@Override
	public Serializable getIdByUniqueKey(
			Serializable key, String uniquePropertyName, SharedSessionContractImplementor session) {
		return null;
	}

	@Override
	public Object getCurrentVersion(Object id, SharedSessionContractImplementor session)
			throws HibernateException {
		return null;
	}

	@Override
	public Object forceVersionIncrement(
			Object id, Object currentVersion, SharedSessionContractImplementor session)
			throws HibernateException {
		return null;
	}

	@Override
	public boolean isInstrumented() {
		return false;
	}

	@Override
	public boolean hasInsertGeneratedProperties() {
		return false;
	}

	@Override
	public boolean hasUpdateGeneratedProperties() {
		return false;
	}

	@Override
	public boolean isVersionPropertyGenerated() {
		return false;
	}

	@Override
	public void afterInitialize(Object entity, SharedSessionContractImplementor session) {

	}

	@Override
	public void afterReassociate(Object entity, SharedSessionContractImplementor session) {

	}

	@Override
	public Boolean isTransient(Object object, SharedSessionContractImplementor session) throws HibernateException {
		return null;
	}

	@Override
	public Object[] getPropertyValuesToInsert(
			Object object,
			Map mergeMap,
			SharedSessionContractImplementor session) throws HibernateException {
//		final Object[] values = getPropertyValues( object );
//
//		assert values.length == getStateArrayContributors().size();
//
		final Object[] stateArray = new Object[ getStateArrayContributors().size() ];
		visitStateArrayContributors(
				contributor -> {
					stateArray[ contributor.getStateArrayPosition() ] = contributor.getPropertyAccess().getGetter().getForInsert(
							object,
							mergeMap,
							session
					);
				}
		);

		return stateArray;
	}

	@Override
	public void processInsertGeneratedProperties(
			Object id, Object entity, Object[] state, SharedSessionContractImplementor session) {

	}

	@Override
	public void processUpdateGeneratedProperties(
			Object id, Object entity, Object[] state, SharedSessionContractImplementor session) {

	}

	@Override
	public Class getMappedClass() {
		return null;
	}

	@Override
	public boolean implementsLifecycle() {
		return false;
	}

	@Override
	public boolean hasUninitializedLazyProperties(Object object) {
		return false;
	}

	@Override
	public EntityDescriptor getSubclassEntityPersister(
			Object instance, SessionFactoryImplementor factory) {
		if ( getSubclassTypes().isEmpty() ) {
			return this;
		}
		else {
			throw new NotYetImplementedFor6Exception(  );
		}
	}

	@Override
	public FilterAliasGenerator getFilterAliasGenerator(String rootAlias) {
		return null;
	}

	@Override
	public int[] resolveAttributeIndexes(String[] attributeNames) {
		return new int[0];
	}

	@Override
	public boolean canUseReferenceCacheEntries() {
		return false;
	}

	@Override
	public void registerAffectingFetchProfile(String fetchProfileName) {

	}

	Boolean hasCollections;

	@Override
	public boolean hasCollections() {
		// todo (6.0) : do this init up front?
		if ( hasCollections == null ) {
			hasCollections = false;
			controlledVisitAttributes(
					attr -> {
						if ( attr instanceof PluralPersistentAttribute ) {
							hasCollections = true;
							return false;
						}

						return true;
					}
			);
		}

		return hasCollections;
	}
}
