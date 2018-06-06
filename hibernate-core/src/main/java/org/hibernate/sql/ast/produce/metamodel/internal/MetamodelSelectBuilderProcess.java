/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.Stack;
import org.hibernate.internal.util.collections.StandardStack;
import org.hibernate.loader.spi.AfterLoadAction;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.metamodel.model.domain.spi.BasicValuedNavigable;
import org.hibernate.metamodel.model.domain.spi.EmbeddedValuedNavigable;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.metamodel.model.domain.spi.NavigableContainer;
import org.hibernate.query.sqm.produce.internal.UniqueIdGenerator;
import org.hibernate.sql.ast.JoinType;
import org.hibernate.sql.ast.produce.internal.SqlAstSelectDescriptorImpl;
import org.hibernate.sql.ast.produce.metamodel.spi.SqlAliasBaseGenerator;
import org.hibernate.sql.ast.produce.metamodel.spi.TableGroupInfo;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.NavigablePathStack;
import org.hibernate.sql.ast.produce.spi.NonQualifiableSqlExpressable;
import org.hibernate.sql.ast.produce.spi.QualifiableSqlExpressable;
import org.hibernate.sql.ast.produce.spi.RootTableGroupContext;
import org.hibernate.sql.ast.produce.spi.RootTableGroupProducer;
import org.hibernate.sql.ast.produce.spi.SqlAliasBaseManager;
import org.hibernate.sql.ast.produce.spi.SqlAstBuildingContext;
import org.hibernate.sql.ast.produce.spi.SqlAstSelectDescriptor;
import org.hibernate.sql.ast.produce.spi.SqlExpressable;
import org.hibernate.sql.ast.produce.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.produce.sqm.spi.Callback;
import org.hibernate.sql.ast.tree.spi.QuerySpec;
import org.hibernate.sql.ast.tree.spi.SelectStatement;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.ast.tree.spi.expression.SqlTuple;
import org.hibernate.sql.ast.tree.spi.expression.domain.BasicValuedNavigableReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.EmbeddableValuedNavigableReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.EntityValuedNavigableReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableContainerReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.ast.tree.spi.from.TableGroup;
import org.hibernate.sql.ast.tree.spi.from.TableSpace;
import org.hibernate.sql.ast.tree.spi.predicate.InListPredicate;
import org.hibernate.sql.ast.tree.spi.predicate.Predicate;
import org.hibernate.sql.ast.tree.spi.predicate.RelationalPredicate;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * @author Steve Ebersole
 */
public class MetamodelSelectBuilderProcess
		implements QueryResultCreationContext,
		SqlAstBuildingContext, SqlExpressionResolver {

	public static SqlAstSelectDescriptor createSelect(
			SessionFactoryImplementor sessionFactory,
			NavigableContainer rootNavigableContainer,
			List<Navigable<?>> navigablesToSelect,
			Navigable restrictedNavigable,
			QueryResult queryResult,
			int numberOfKeysToLoad,
			LoadQueryInfluencers loadQueryInfluencers,
			LockOptions lockOptions) {
		final MetamodelSelectBuilderProcess process = new MetamodelSelectBuilderProcess(
				sessionFactory,
				rootNavigableContainer,
				navigablesToSelect,
				restrictedNavigable,
				queryResult,
				numberOfKeysToLoad,
				loadQueryInfluencers,
				lockOptions
		);

		return process.execute();
	}

	private final SessionFactoryImplementor sessionFactory;
	private final NavigableContainer rootNavigableContainer;
	private final List<Navigable<?>> navigablesToSelect;
	private final Navigable restrictedNavigable;
	private final QueryResult queryResult;
	private final int numberOfKeysToLoad;
	private final LoadQueryInfluencers loadQueryInfluencers;
	private final LockOptions lockOptions;

	private MetamodelSelectBuilderProcess(
			SessionFactoryImplementor sessionFactory,
			NavigableContainer rootNavigableContainer,
			List<Navigable<?>> navigablesToSelect,
			Navigable restrictedNavigable,
			QueryResult queryResult,
			int numberOfKeysToLoad,
			LoadQueryInfluencers loadQueryInfluencers,
			LockOptions lockOptions) {
		this.sessionFactory = sessionFactory;
		this.rootNavigableContainer = rootNavigableContainer;
		this.navigablesToSelect = navigablesToSelect;
		this.restrictedNavigable = restrictedNavigable;
		this.queryResult = queryResult;
		this.numberOfKeysToLoad = numberOfKeysToLoad;
		this.loadQueryInfluencers = loadQueryInfluencers;
		this.lockOptions = lockOptions != null ? lockOptions : LockOptions.NONE;
	}

	private final Stack<TableSpace> tableSpaceStack = new StandardStack<>();
	private final Stack<TableGroup> tableGroupStack = new StandardStack<>();
	private final Stack<FetchParent> fetchParentStack = new StandardStack<>();
	private final NavigablePathStack navigablePathStack = new NavigablePathStack();
	private final Set<String> affectedTables = new HashSet<>();

	private final QuerySpec rootQuerySpec = new QuerySpec( true );

	private SqlAstSelectDescriptor execute() {
		navigablePathStack.push( rootNavigableContainer );

		final SelectStatement selectStatement = new SelectStatement( rootQuerySpec );

		final UniqueIdGenerator uidGenerator = new UniqueIdGenerator();
		final String uid = uidGenerator.generateUniqueId();

		final SqlAliasBaseManager aliasBaseManager = new SqlAliasBaseManager();
		final TableSpace rootTableSpace = rootQuerySpec.getFromClause().makeTableSpace();
		tableSpaceStack.push( rootTableSpace );

		final TableGroup rootTableGroup = makeRootTableGroup( uid, rootQuerySpec, rootTableSpace, aliasBaseManager );
		rootTableSpace.setRootTableGroup( rootTableGroup );
		tableGroupStack.push( rootTableGroup );

		final List<QueryResult> queryResults;

		if ( navigablesToSelect != null && ! navigablesToSelect.isEmpty() ) {
			queryResults = new ArrayList<>();
			int jdbcSelectionCount = 0;
			for ( Navigable navigable : navigablesToSelect ) {
				final NavigableReference navigableReference = makeNavigableReference( rootTableGroup, navigable );
				queryResults.add(
						navigable.createQueryResult(
								navigableReference,
								null,
								this
						)
				);
			}
		}
		else {
			// use the one passed to the constructor or create one (maybe always create and pass?)
			//		allows re-use as they can be re-used to save on memory - they
			//		do not share state between
			final QueryResult queryResult;
			if ( this.queryResult != null ) {
				// used the one passed to the constructor
				queryResult = this.queryResult;
			}
			else {
				// create one
				queryResult = rootNavigableContainer.createQueryResult(
						rootTableSpace.getRootTableGroup().getNavigableReference(),
						null,
						this
				);
			}

			queryResults = Collections.singletonList( queryResult );

			// todo (6.0) : process fetches & entity-graphs
			fetchParentStack.push( (FetchParent) queryResult );
		}


		// add the id/uk/fk restriction
		final List keyReferences = restrictedNavigable.resolveColumnReferences(
				rootTableSpace.getRootTableGroup(),
				this
		);

		final Expression restrictedExpression;

		if ( keyReferences.size() == 1 ) {
			restrictedExpression = (Expression) keyReferences.get( 0 );
		}
		else {
			restrictedExpression = new SqlTuple( keyReferences );
		}

		if ( numberOfKeysToLoad <= 1 ) {
			rootQuerySpec.addRestriction(
					new RelationalPredicate(
							RelationalPredicate.Operator.EQUAL,
							restrictedExpression,
							new LoadIdParameter( (AllowableParameterType) restrictedNavigable )
					)
			);
		}
		else {
			final InListPredicate predicate = new InListPredicate( restrictedExpression );
			for ( int i = 0; i < numberOfKeysToLoad; i++ ) {
				predicate.addExpression(
						new LoadIdParameter( i, (AllowableParameterType) restrictedNavigable )
				);
			}
			rootQuerySpec.addRestriction( predicate );
		}


		return new SqlAstSelectDescriptorImpl(
				selectStatement,
				queryResults,
				affectedTables
		);
	}

	public static NavigableReference makeNavigableReference(TableGroup rootTableGroup, Navigable navigable) {
		if ( navigable instanceof BasicValuedNavigable ) {
			return new BasicValuedNavigableReference(
					(NavigableContainerReference) rootTableGroup.getNavigableReference(),
					(BasicValuedNavigable) navigable,
					rootTableGroup.getNavigableReference().getNavigablePath().append( navigable.getNavigableName() )
			);
		}

		if ( navigable instanceof EmbeddedValuedNavigable ) {
			return new EmbeddableValuedNavigableReference(
					(NavigableContainerReference) rootTableGroup.getNavigableReference(),
					(EmbeddedValuedNavigable) navigable,
					rootTableGroup.getNavigableReference().getNavigablePath().append( navigable.getNavigableName() ),
					LockMode.NONE
			);
		}

		if ( navigable instanceof EntityValuedNavigable ) {
			// todo (6.0) : join?
			return new EntityValuedNavigableReference(
					(NavigableContainerReference) rootTableGroup.getNavigableReference(),
					(EntityValuedNavigable) navigable,
					rootTableGroup.getNavigableReference().getNavigablePath().append( navigable.getNavigableName() ),
					rootTableGroup,
					LockMode.NONE
			);
		}

		throw new NotYetImplementedFor6Exception();
	}

	private TableGroup makeRootTableGroup(
			String uid,
			QuerySpec querySpec,
			TableSpace rootTableSpace,
			SqlAliasBaseManager aliasBaseManager) {
		return ( (RootTableGroupProducer) rootNavigableContainer ).createRootTableGroup(
				new TableGroupInfo() {
					@Override
					public String getUniqueIdentifier() {
						return uid;
					}

					@Override
					public String getIdentificationVariable() {
						// todo (6.0) : is "root" a reserved word?
						return "root";
					}

					@Override
					public EntityDescriptor getIntrinsicSubclassEntityMetadata() {
						return null;
					}
				},
				new RootTableGroupContext() {
					@Override
					public void addRestriction(Predicate predicate) {
						querySpec.addRestriction( predicate );
					}

					@Override
					public QuerySpec getQuerySpec() {
						return querySpec;
					}

					@Override
					public TableSpace getTableSpace() {
						return rootTableSpace;
					}

					@Override
					public SqlAliasBaseGenerator getSqlAliasBaseGenerator() {
						return aliasBaseManager;
					}

					@Override
					public JoinType getTableReferenceJoinType() {
						return null;
					}

					@Override
					public LockOptions getLockOptions() {
						return lockOptions;
					}
				}
		);
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SqlAstBuildingContext

	private List<AfterLoadAction> afterLoadActions;

	private final Callback sqlAstCreationCallback = new Callback() {
		@Override
		public void registerAfterLoadAction(AfterLoadAction afterLoadAction) {
			if ( afterLoadActions == null ) {
				afterLoadActions = new ArrayList<>();
			}
			afterLoadActions.add( afterLoadAction );
		}
	};

	@Override
	public Callback getCallback() {
		return sqlAstCreationCallback;
	}

	@Override
	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public SqlExpressionResolver getSqlSelectionResolver() {
		return this;
	}

	@Override
	public boolean shouldCreateShallowEntityResult() {
		return false;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// QueryResultCreationContext

	@Override
	public LockOptions getLockOptions() {
		return lockOptions;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SqlExpressionResolver

	private Map<SqlExpressable,SqlSelection> sqlSelectionMap;

	@Override
	public Expression resolveSqlExpression(
			ColumnReferenceQualifier qualifier,
			QualifiableSqlExpressable sqlSelectable) {
		return qualifier.qualify( sqlSelectable );
	}

	@Override
	public Expression resolveSqlExpression(NonQualifiableSqlExpressable sqlSelectable) {
		return sqlSelectable.createExpression();
	}

	@Override
	public SqlSelection resolveSqlSelection(Expression expression) {
		final SqlSelection existing;
		if ( sqlSelectionMap == null ) {
			sqlSelectionMap = new HashMap<>();
			existing = null;
		}
		else {
			existing = sqlSelectionMap.get( expression.getExpressable() );
		}

		if ( existing != null ) {
			return existing;
		}

		final SqlSelection sqlSelection = expression.createSqlSelection( sqlSelectionMap.size() );
		rootQuerySpec.getSelectClause().addSqlSelection( sqlSelection );
		sqlSelectionMap.put( expression.getExpressable(), sqlSelection );

		return sqlSelection;
	}
}
