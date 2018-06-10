/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

import org.hibernate.EntityNameResolver;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.boot.model.domain.EntityMapping;
import org.hibernate.boot.model.domain.IdentifiableTypeMapping;
import org.hibernate.boot.model.domain.MappedJoin;
import org.hibernate.boot.model.domain.spi.ManagedTypeMappingImplementor;
import org.hibernate.boot.model.relational.MappedTable;
import org.hibernate.bytecode.internal.BytecodeEnhancementMetadataNonPojoImpl;
import org.hibernate.bytecode.internal.BytecodeEnhancementMetadataPojoImpl;
import org.hibernate.bytecode.spi.BytecodeEnhancementMetadata;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.EntityEntryFactory;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.FilterHelper;
import org.hibernate.loader.internal.StandardMultiIdEntityLoader;
import org.hibernate.loader.internal.StandardNaturalIdLoader;
import org.hibernate.loader.internal.StandardSingleIdEntityLoader;
import org.hibernate.loader.spi.EntityLocker;
import org.hibernate.loader.spi.MultiIdEntityLoader;
import org.hibernate.loader.spi.MultiIdLoaderSelectors;
import org.hibernate.loader.spi.NaturalIdLoader;
import org.hibernate.loader.spi.SingleIdEntityLoader;
import org.hibernate.loader.spi.SingleUniqueKeyEntityLoader;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.Subclass;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.RepresentationMode;
import org.hibernate.metamodel.model.domain.internal.EntityHierarchyImpl;
import org.hibernate.metamodel.model.domain.internal.EntityIdentifierCompositeAggregatedImpl;
import org.hibernate.metamodel.model.domain.internal.EntityIdentifierSimpleImpl;
import org.hibernate.metamodel.model.domain.internal.SqlAliasStemHelper;
import org.hibernate.metamodel.model.relational.spi.ForeignKey;
import org.hibernate.metamodel.model.relational.spi.JoinedTableBinding;
import org.hibernate.metamodel.model.relational.spi.PhysicalColumn;
import org.hibernate.metamodel.model.relational.spi.PhysicalTable;
import org.hibernate.metamodel.model.relational.spi.Table;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.JoinType;
import org.hibernate.sql.ast.produce.metamodel.spi.TableGroupInfo;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.RootTableGroupContext;
import org.hibernate.sql.ast.produce.spi.SqlAliasBase;
import org.hibernate.sql.ast.produce.spi.TableGroupContext;
import org.hibernate.sql.ast.tree.spi.expression.domain.EntityValuedNavigableReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.ast.tree.spi.from.EntityTableGroup;
import org.hibernate.sql.ast.tree.spi.from.TableReference;
import org.hibernate.sql.ast.tree.spi.from.TableReferenceJoin;
import org.hibernate.sql.ast.tree.spi.predicate.Junction;
import org.hibernate.sql.ast.tree.spi.predicate.Predicate;
import org.hibernate.sql.ast.tree.spi.predicate.RelationalPredicate;
import org.hibernate.sql.results.internal.EntityQueryResultImpl;
import org.hibernate.sql.results.internal.EntitySqlSelectionGroupImpl;
import org.hibernate.sql.results.spi.EntitySqlSelectionGroup;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.SqlSelectionResolutionContext;
import org.hibernate.type.descriptor.java.spi.EntityJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.IdentifiableJavaDescriptor;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractEntityDescriptor<J>
		extends AbstractIdentifiableType<J>
		implements Lockable<J> {
	private static final CoreMessageLogger log = CoreLogging.messageLogger( AbstractEntityDescriptor.class );

	private final SessionFactoryImplementor factory;
	private final EntityHierarchy hierarchy;

	private final NavigableRole navigableRole;


	private final Table rootTable;
	private final List<JoinedTableBinding> secondaryTableBindings;

	private final BytecodeEnhancementMetadata bytecodeEnhancementMetadata;
	private final Instantiator<J> instantiator;

	private final String sqlAliasStem;

	private final Dialect dialect;

	private final boolean canReadFromCache;
	private final boolean canWriteToCache;

	@SuppressWarnings("UnnecessaryBoxing")
	public AbstractEntityDescriptor(
			EntityMapping bootMapping,
			IdentifiableTypeDescriptor<? super J> superTypeDescriptor,
			RuntimeModelCreationContext creationContext) throws HibernateException {
		super(
				bootMapping,
				superTypeDescriptor,
				resolveJavaTypeDescriptorFromJavaTypeMapping( bootMapping ),
				creationContext
		);

		this.factory = creationContext.getSessionFactory();

		this.navigableRole = new NavigableRole( bootMapping.getEntityName() );

		this.hierarchy = resolveEntityHierarchy( bootMapping, superTypeDescriptor, creationContext );

		this.rootTable = resolveRootTable( bootMapping, creationContext );
		this.secondaryTableBindings = resolveSecondaryTableBindings( bootMapping, creationContext );

		final RepresentationMode representation = getRepresentationStrategy().getMode();
		if ( representation == RepresentationMode.POJO ) {
			this.bytecodeEnhancementMetadata = BytecodeEnhancementMetadataPojoImpl.from( bootMapping );
		}
		else {
			this.bytecodeEnhancementMetadata = new BytecodeEnhancementMetadataNonPojoImpl( bootMapping.getEntityName() );
		}

		this.instantiator = getRepresentationStrategy().resolveInstantiator(
				bootMapping,
				this,
				Environment.getBytecodeProvider()
		);

		log.debugf(
				"Instantiated persister [%s] for entity [%s (%s)]",
				this,
				bootMapping.getEntityName(),
				bootMapping.getJpaEntityName()
		);

		this.sqlAliasStem = SqlAliasStemHelper.INSTANCE.generateStemFromEntityName( bootMapping.getEntityName() );
		this.dialect = factory.getServiceRegistry().getService( JdbcServices.class ).getDialect();

		if ( creationContext.getSessionFactory().getSessionFactoryOptions().isSecondLevelCacheEnabled() ) {
			PersistentClass persistentClass = (PersistentClass) bootMapping;
			this.canWriteToCache = persistentClass.isCached();
			this.canReadFromCache = determineCanReadFromCache( persistentClass );
		}
		else {
			this.canWriteToCache = false;
			this.canReadFromCache = false;
		}

		// Handle any filters applied to the class level
		this.filterHelper = new FilterHelper( bootMapping.getFilters(), factory );
	}

	@SuppressWarnings("unchecked")
	private boolean determineCanReadFromCache(PersistentClass persistentClass) {
		if ( persistentClass.isCached() ) {
			return true;
		}

		final Iterator<Subclass> subclassIterator = persistentClass.getSubclassIterator();
		while ( subclassIterator.hasNext() ) {
			final Subclass subclass = subclassIterator.next();
			if ( subclass.isCached() ) {
				return true;
			}
		}
		return false;
	}

	private EntityHierarchy resolveEntityHierarchy(
			IdentifiableTypeMapping bootMapping,
			IdentifiableTypeDescriptor superTypeDescriptor,
			RuntimeModelCreationContext creationContext) {
		if ( bootMapping instanceof RootClass ) {
			return new EntityHierarchyImpl( this, (RootClass) bootMapping, creationContext );
		}
		else {
			return superTypeDescriptor.getHierarchy();
		}
	}

	// todo (6.0) : the root-table may not need to be phyically stored here
	// 		table structures vary by inheritance type
	//
	private Table resolveRootTable(EntityMapping entityMapping, RuntimeModelCreationContext creationContext) {
		final MappedTable rootMappedTable = entityMapping.getRootTable();
		return resolveTable( rootMappedTable, creationContext );
	}

	private Table resolveTable(MappedTable mappedTable, RuntimeModelCreationContext creationContext) {
		return creationContext.getDatabaseObjectResolver().resolveTable( mappedTable );
	}

	private List<JoinedTableBinding> resolveSecondaryTableBindings(
			EntityMapping entityMapping,
			RuntimeModelCreationContext creationContext) {
		final Collection<MappedJoin> mappedJoins = entityMapping.getMappedJoins();
		if ( mappedJoins.size() <= 0 ) {
			return Collections.emptyList();
		}

		if ( mappedJoins.size() == 1 ) {
			return Collections.singletonList(
					generateJoinedTableBinding( mappedJoins.iterator().next(), creationContext )
			);
		}

		final ArrayList<JoinedTableBinding> bindings = new ArrayList<>();
		for ( MappedJoin mappedJoin : mappedJoins ) {
			bindings.add(
					generateJoinedTableBinding( mappedJoin, creationContext )
			);
		}
		return bindings;
	}

	private JoinedTableBinding generateJoinedTableBinding(MappedJoin bootJoinTable, RuntimeModelCreationContext creationContext) {
		final Table joinedTable = resolveTable( bootJoinTable.getMappedTable(), creationContext );

		// todo (6.0) : resolve "join predicate" as ForeignKey.ColumnMappings
		//		see note on MappedJoin regarding what to expose there


		return new JoinedTableBinding(
				// NOTE : for secondary tables, it is the secondary table that is
				//		the referring table
				joinedTable,
				getPrimaryTable(),
				creationContext.getDatabaseObjectResolver().resolveForeignKey( bootJoinTable.getJoinMapping() ),
				bootJoinTable.isOptional()
		);
	}

	private static <T> IdentifiableJavaDescriptor<T> resolveJavaTypeDescriptorFromJavaTypeMapping(
			EntityMapping entityMapping) {
		return (IdentifiableJavaDescriptor<T>) entityMapping.getJavaTypeMapping().resolveJavaTypeDescriptor();
	}

	@Override
	public void finishInitialization(
			ManagedTypeMappingImplementor bootDescriptor,
			RuntimeModelCreationContext creationContext) {
		super.finishInitialization( bootDescriptor, creationContext );

		this.singleIdLoader = new StandardSingleIdEntityLoader<>( this );

		log.debugf(
				"Completed initialization of descriptor [%s] for entity [%s (%s)]",
				this,
				getJavaTypeDescriptor().getEntityName(),
				getJavaTypeDescriptor().getJpaEntityName()
		);
	}

	@Override
	public SessionFactoryImplementor getFactory() {
		return factory;
	}

	@Override
	public EntityHierarchy getHierarchy() {
		return hierarchy;
	}

	@Override
	public boolean canReadFromCache() {
		return canReadFromCache;
	}

	@Override
	public boolean canWriteToCache() {
		return canWriteToCache;
	}

	@Override
	public EntityJavaDescriptor<J> getJavaTypeDescriptor() {
		return (EntityJavaDescriptor<J>) super.getJavaTypeDescriptor();
	}

	@Override
	public String getEntityName() {
		return getJavaTypeDescriptor().getEntityName();
	}

	@Override
	public String getJpaEntityName() {
		return getJavaTypeDescriptor().getJpaEntityName();
	}

	@Override
	public String getName() {
		return getJpaEntityName();
	}

	@Override
	public NavigableContainer getContainer() {
		return null;
	}

	@Override
	public Table getPrimaryTable() {
		return rootTable;
	}

	@Override
	public List<JoinedTableBinding> getSecondaryTableBindings() {
		return secondaryTableBindings;
	}

	@Override
	public Class<J> getBindableJavaType() {
		return getJavaType();
	}

	@Override
	public BytecodeEnhancementMetadata getBytecodeEnhancementMetadata() {
		return bytecodeEnhancementMetadata;
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public String getNavigableName() {
		return navigableRole.getNavigableName();
	}

	@Override
	public EntityDescriptor<J> getEntityDescriptor() {
		return this;
	}

	@Override
	public EntityEntryFactory getEntityEntryFactory() {
		return getHierarchy().getMutabilityPlan().getEntityEntryFactory();
	}

	@Override
	public List<EntityNameResolver> getEntityNameResolvers() {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Y> SingularAttribute<? super J, Y> getId(Class<Y> type) {
		return getHierarchy().getIdentifierDescriptor().asAttribute( type );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Y> SingularAttribute<J, Y> getDeclaredId(Class<Y> type) {
		return (SingularAttribute<J, Y>) getHierarchy().getIdentifierDescriptor().asAttribute( type );
	}

	@Override
	public <Y> SingularAttribute<? super J, Y> getVersion(Class<Y> type) {
		return getHierarchy().getVersionDescriptor();
	}

	@Override
	public <Y> SingularAttribute<J, Y> getDeclaredVersion(Class<Y> type) {
		return getHierarchy().getVersionDescriptor();
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return getIdentifierDescriptor() instanceof EntityIdentifierSimpleImpl
				|| getIdentifierDescriptor() instanceof EntityIdentifierCompositeAggregatedImpl;
	}

	@Override
	public boolean hasVersionAttribute() {
		return getHierarchy().getVersionDescriptor() != null;
	}

	@Override
	public Set<SingularAttribute<? super J, ?>> getIdClassAttributes() {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public Type<?> getIdType() {
		return getHierarchy().getIdentifierDescriptor();
	}

	@Override
	public BindableType getBindableType() {
		return BindableType.ENTITY_TYPE;
	}

	private final SingleIdEntityLoader customQueryLoader = null;
	private StandardSingleIdEntityLoader<J> singleIdLoader;

	private final FilterHelper filterHelper;
	private final Set<String> affectingFetchProfileNames = new HashSet<>();

	@Override
	@SuppressWarnings("unchecked")
	public SingleIdEntityLoader getSingleIdLoader() {
		if ( customQueryLoader != null ) {
			// if the user specified that we should use a custom query for loading this entity, we need
			// 		to always use that custom loader.
			return customQueryLoader;
		}

		return singleIdLoader;
	}

	@Override
	public boolean isAffectedByEnabledFilters(LoadQueryInfluencers loadQueryInfluencers) {
		assert filterHelper != null;
		return loadQueryInfluencers.hasEnabledFilters()
				&& filterHelper.isAffectedBy( loadQueryInfluencers.getEnabledFilters() );
	}

	@Override
	public boolean isAffectedByEnabledFetchProfiles(LoadQueryInfluencers loadQueryInfluencers) {
		for ( String s : loadQueryInfluencers.getEnabledFetchProfileNames() ) {
			if ( affectingFetchProfileNames.contains( s ) ) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAffectedByEntityGraph(LoadQueryInfluencers loadQueryInfluencers) {
		return loadQueryInfluencers.getFetchGraph() != null
				|| loadQueryInfluencers.getLoadGraph() != null;
	}

	@SuppressWarnings("WeakerAccess")
	protected SingleIdEntityLoader createSingleIdLoader(LockOptions lockOptions, LoadQueryInfluencers loadQueryInfluencers) {
		return singleIdLoader;
	}

	@Override
	public NaturalIdLoader getNaturalIdLoader() {
		if ( ! hasNaturalIdentifier() ) {
			throw new UnsupportedOperationException( "Entity [" + getEntityName() + "] does not define a natural-id" );
		}

		// todo (6.0) : can this be cached like `singleIdLoader`?
		return new StandardNaturalIdLoader( this );
	}

	@Override
	public MultiIdEntityLoader getMultiIdLoader(MultiIdLoaderSelectors selectors) {
		if ( customQueryLoader != null ) {
			throw new HibernateException(
					"Cannot perform multi-id loading on an entity defined with a custom load query : " + getEntityName()
			);
		}

		// todo (6.0) : maybe cache the QueryResult reference?
		// todo (6.0) : or cache the StandardMultiIdEntityLoader and have it cache things appropriately internally

		return new StandardMultiIdEntityLoader( this, selectors );
	}

	@Override
	public SingleUniqueKeyEntityLoader getSingleUniqueKeyLoader(Navigable navigable, LoadQueryInfluencers loadQueryInfluencers) {
		throw new NotYetImplementedFor6Exception();
	}

	private Map<LockMode,EntityLocker> lockers;

	@Override
	public EntityLocker getLocker(LockOptions lockOptions, LoadQueryInfluencers loadQueryInfluencers) {
		EntityLocker entityLocker = null;
		if ( lockers == null ) {
			lockers = new ConcurrentHashMap<>();
		}
		else {
			entityLocker = lockers.get( lockOptions.getLockMode() );
		}

		if ( entityLocker == null ) {
			throw new NotYetImplementedFor6Exception(  );
//			entityLocker = new EntityLocker() {
//				final LockingStrategy strategy = getFactory().getJdbcServices()
//						.getJdbcEnvironment()
//						.getDialect()
//						.getLockingStrategy( ... );
//				@Override
//				public void lock(
//						Serializable id,
//						Object version,
//						Object object,
//						SharedSessionContractImplementor session,
//						Options options) {
//					strategy.lock( id, version, object, options.getTimeout(), session );
//				}
//			};
//			lockers.put( lockOptions.getLockMode(), entityLocker );
		}
		return entityLocker;
	}

	@Override
	public String getSqlAliasStem() {
		return sqlAliasStem;
	}

	@Override
	public EntityTableGroup createRootTableGroup(TableGroupInfo info, RootTableGroupContext tableGroupContext) {
		final SqlAliasBase sqlAliasBase = tableGroupContext.getSqlAliasBaseGenerator().createSqlAliasBase( getSqlAliasStem() );

		final TableReference primaryTableReference = resolvePrimaryTableReference( sqlAliasBase );

		final List<TableReferenceJoin> joins = new ArrayList<>(  );
		resolveTableReferenceJoins( primaryTableReference, sqlAliasBase, tableGroupContext, joins::add );

		final EntityTableGroup group = new EntityTableGroup(
				info.getUniqueIdentifier(),
				tableGroupContext.getTableSpace(),
				this,
				tableGroupContext.getLockOptions().getEffectiveLockMode( info.getIdentificationVariable() ),
				new NavigablePath( getEntityName() ),
				primaryTableReference,
				joins
		);

		// todo (6.0) - apply filters - which needs access to Session, or at least its LoadQueryInfluencers
		//		the filter conditions would be added to the SQL-AST's where-clause via tableGroupContext
		//		for now, add null, this is just here as a placeholder
		tableGroupContext.addRestriction( null );

		return group;
	}

	protected TableReference resolvePrimaryTableReference(SqlAliasBase sqlAliasBase) {
		return new TableReference( getPrimaryTable(), sqlAliasBase.generateNewAlias() );
	}

	private void resolveTableReferenceJoins(
			TableReference rootTableReference,
			SqlAliasBase sqlAliasBase,
			TableGroupContext context,
			Consumer<TableReferenceJoin> collector) {

		for ( JoinedTableBinding joinedTableBinding : getSecondaryTableBindings() ) {
			collector.accept( createTableReferenceJoin( joinedTableBinding, rootTableReference, sqlAliasBase, context ) );
		}
	}

	protected TableReferenceJoin createTableReferenceJoin(
			JoinedTableBinding joinedTableBinding,
			TableReference rootTableReference,
			SqlAliasBase sqlAliasBase,
			TableGroupContext context) {
		final TableReference joinedTableReference = new TableReference(
				joinedTableBinding.getReferringTable(),
				sqlAliasBase.generateNewAlias()
		);

		return new TableReferenceJoin(
				joinedTableBinding.isOptional()
						? JoinType.LEFT
						: context.getTableReferenceJoinType(),
				joinedTableReference,
				generateJoinPredicate( rootTableReference, joinedTableReference, joinedTableBinding.getJoinForeignKey() )
		);
	}

	private Predicate generateJoinPredicate(
			TableReference rootTableReference,
			TableReference joinedTableReference,
			ForeignKey joinForeignKey) {
		assert rootTableReference.getTable() == joinForeignKey.getTargetTable();
		assert joinedTableReference.getTable() == joinForeignKey.getReferringTable();
		assert !joinForeignKey.getColumnMappings().getColumnMappings().isEmpty();

		final Junction conjunction = new Junction( Junction.Nature.CONJUNCTION );

		for ( ForeignKey.ColumnMappings.ColumnMapping columnMapping : joinForeignKey.getColumnMappings().getColumnMappings() ) {
			conjunction.add(
					new RelationalPredicate(
							RelationalPredicate.Operator.EQUAL,
							rootTableReference.resolveColumnReference( columnMapping.getTargetColumn() ),
							joinedTableReference.resolveColumnReference( columnMapping.getReferringColumn() )
					)
			);
		}

		return conjunction;
	}

	@Override
	public void applyTableReferenceJoins(
			ColumnReferenceQualifier lhs,
			JoinType joinType,
			SqlAliasBase sqlAliasBase,
			TableReferenceJoinCollector joinCollector,
			TableGroupContext tableGroupContext) {
		final TableReference root = resolvePrimaryTableReference( sqlAliasBase );
		joinCollector.addRoot( root );
		resolveTableReferenceJoins( root, sqlAliasBase, tableGroupContext, joinCollector::collectTableReferenceJoin );
	}

	@Override
	public QueryResult createQueryResult(
			NavigableReference navigableReference,
			String resultVariable,
			QueryResultCreationContext creationContext) {
		assert navigableReference instanceof EntityValuedNavigableReference;

		return new EntityQueryResultImpl(
				(EntityValuedNavigable) navigableReference.getNavigable(),
				resultVariable,
				resolveSqlSelections( navigableReference.getSqlExpressionQualifier(), creationContext ),
				( (EntityValuedNavigableReference) navigableReference ).getLockMode(),
				navigableReference.getNavigablePath(),
				creationContext
		);
	}

	// todo (6.0) : we need some way here to limit which attributes are rendered as how "deep" we render them
	//		* which to render comes down to bytecode enhanced laziness
	//		* how deep (associations) comes down to fetching



	@Override
	public EntitySqlSelectionGroup resolveSqlSelections(
			ColumnReferenceQualifier qualifier,
			SqlSelectionResolutionContext resolutionContext) {
		return EntitySqlSelectionGroupImpl.buildSqlSelectionGroup( this, qualifier, resolutionContext );
	}

	@Override
	public String getRootTableName() {
		return ( (PhysicalTable) rootTable ).getTableName().render( dialect );
	}

	@Override
	public String[] getRootTableIdentifierColumnNames() {
		final List<PhysicalColumn> columns = rootTable.getPrimaryKey().getColumns();
		String[] columnNames = new String[columns.size()];
		int i = 0;
		for ( PhysicalColumn column : columns ) {
			columnNames[i] = column.getName().render( dialect );
			i++;
		}
		return columnNames;
	}

	@Override
	public String getVersionColumnName() {
		return ( (PhysicalColumn) getHierarchy().getVersionDescriptor().getBoundColumn() )
				.getName()
				.render( dialect );
	}

	@Override
	public boolean hasNaturalIdentifier() {
		return getHierarchy().getNaturalIdDescriptor() != null;
	}

	@Override
	public Object instantiate(Object id, SharedSessionContractImplementor session) {
		final J instance = instantiator.instantiate( session );
		setIdentifier( instance, id, session );
		return instance;
	}

	@Override
	public boolean isInstance(Object object) {
		return instantiator.isInstance( object, getFactory() );
	}

	@Override
	public void setPropertyValues(Object object, Object[] values) {
		// todo (6.0) : hook in ReflectionOptimizer.AccessOptimizer
		super.setPropertyValues( object, values );
	}

	@Override
	public void resetIdentifier(
			Object entity,
			Object currentId,
			Object currentVersion,
			SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public Object getVersion(Object object) throws HibernateException {
		return null;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"%s(`%s`)@%s",
				getClass().getSimpleName(),
				getEntityName(),
				hashCode()
		);
	}


	@Override
	public void insert(
			Object id,
			Object[] fields,
			Object object,
			SharedSessionContractImplementor session) {
		insertInternal( id, fields, object, session );
	}

	protected Object insertInternal(
			Object id,
			Object[] fields,
			Object object,
			SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public Object insert(
			Object[] fields,
			Object object,
			SharedSessionContractImplementor session) {
		return insertInternal( null, fields, object, session );
	}
}
