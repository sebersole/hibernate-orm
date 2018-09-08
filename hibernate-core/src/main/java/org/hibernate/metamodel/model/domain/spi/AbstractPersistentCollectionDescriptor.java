/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.boot.model.domain.BasicValueMapping;
import org.hibernate.boot.model.domain.EmbeddedValueMapping;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.MappedColumn;
import org.hibernate.boot.model.relational.MappedNamespace;
import org.hibernate.boot.model.relational.MappedTable;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionDataAccess;
import org.hibernate.cache.spi.entry.CacheEntryStructure;
import org.hibernate.cache.spi.entry.StructuredCollectionCacheEntry;
import org.hibernate.cache.spi.entry.StructuredMapCacheEntry;
import org.hibernate.cache.spi.entry.UnstructuredCacheEntry;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.collection.spi.CollectionSemantics;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.loader.spi.CollectionLoader;
import org.hibernate.mapping.Any;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.IdentifierCollection;
import org.hibernate.mapping.IndexedCollection;
import org.hibernate.mapping.ManyToOne;
import org.hibernate.mapping.OneToMany;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.internal.BasicCollectionElementImpl;
import org.hibernate.metamodel.model.domain.internal.BasicCollectionIndexImpl;
import org.hibernate.metamodel.model.domain.internal.CollectionElementEmbeddedImpl;
import org.hibernate.metamodel.model.domain.internal.CollectionElementEntityImpl;
import org.hibernate.metamodel.model.domain.internal.CollectionIndexEmbeddedImpl;
import org.hibernate.metamodel.model.domain.internal.CollectionIndexEntityImpl;
import org.hibernate.metamodel.model.domain.internal.PluralPersistentAttributeImpl;
import org.hibernate.metamodel.model.domain.internal.SqlAliasStemHelper;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.metamodel.model.relational.spi.ForeignKey;
import org.hibernate.metamodel.model.relational.spi.Table;
import org.hibernate.naming.Identifier;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.JoinType;
import org.hibernate.sql.ast.produce.metamodel.spi.Fetchable;
import org.hibernate.sql.ast.produce.metamodel.spi.SqlAliasBaseGenerator;
import org.hibernate.sql.ast.produce.metamodel.spi.TableGroupInfo;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.JoinedTableGroupContext;
import org.hibernate.sql.ast.produce.spi.RootTableGroupContext;
import org.hibernate.sql.ast.produce.spi.SqlAliasBase;
import org.hibernate.sql.ast.produce.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.tree.spi.expression.ColumnReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.PluralAttributeReference;
import org.hibernate.sql.ast.tree.spi.from.CollectionTableGroup;
import org.hibernate.sql.ast.tree.spi.from.TableGroup;
import org.hibernate.sql.ast.tree.spi.from.TableGroupJoin;
import org.hibernate.sql.ast.tree.spi.from.TableReference;
import org.hibernate.sql.ast.tree.spi.from.TableReferenceJoin;
import org.hibernate.sql.ast.tree.spi.from.TableSpace;
import org.hibernate.sql.ast.tree.spi.predicate.Junction;
import org.hibernate.sql.ast.tree.spi.predicate.Predicate;
import org.hibernate.sql.ast.tree.spi.predicate.RelationalPredicate;
import org.hibernate.sql.results.internal.domain.collection.PluralAttributeResultImpl;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultCreationContext;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.SqlSelectionGroupNode;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.internal.CollectionJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptorRegistry;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractPersistentCollectionDescriptor<O,C,E> implements PersistentCollectionDescriptor<O,C,E> {
	private final SessionFactoryImplementor sessionFactory;

	private final ManagedTypeDescriptor container;
	private final PluralPersistentAttribute attribute;
	private final NavigableRole navigableRole;
	private final CollectionKey foreignKeyDescriptor;

	private CollectionJavaDescriptor<C> javaTypeDescriptor;
	private CollectionIdentifier idDescriptor;
	private CollectionElement elementDescriptor;
	private CollectionIndex indexDescriptor;

	private CollectionDataAccess cacheAccess;

	// todo (6.0) - rework this (and friends) per todo item...
	//		* Redesign `org.hibernate.cache.spi.entry.CacheEntryStructure` and friends (with better names)
	// 			and make more efficient.  At the moment, to cache, we:
	//				.. Create a "cache entry" (object creation)
	//				.. "structure" the "cache entry" (object creation)
	//				.. add "structured data" to the cache.
	private final CacheEntryStructure cacheEntryStructure;



	private Table separateCollectionTable;

	private final String sqlAliasStem;

	private final org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor keyJavaTypeDescriptor;

	private final Set<String> spaces;

	private final int batchSize;
	private final boolean extraLazy;
	private final boolean hasOrphanDeletes;
	private final boolean inverse;

	@SuppressWarnings("unchecked")
	public AbstractPersistentCollectionDescriptor(
			Property pluralProperty,
			ManagedTypeDescriptor runtimeContainer,
			RuntimeModelCreationContext creationContext) throws MappingException, CacheException {

		final Collection collectionBinding = (Collection) pluralProperty.getValue();

		this.sessionFactory = creationContext.getSessionFactory();
		this.container = runtimeContainer;

		this.navigableRole = container.getNavigableRole().append( pluralProperty.getName() );

		this.attribute = new PluralPersistentAttributeImpl(
				this,
				pluralProperty,
				runtimeContainer.getRepresentationStrategy().generatePropertyAccess(
						pluralProperty.getPersistentClass(),
						pluralProperty,
						runtimeContainer,
						sessionFactory.getSessionFactoryOptions().getBytecodeProvider()
				),
				creationContext
		);

		this.foreignKeyDescriptor = new CollectionKey( this, collectionBinding, creationContext );

		if ( sessionFactory.getSessionFactoryOptions().isStructuredCacheEntriesEnabled() ) {
			cacheEntryStructure = collectionBinding.isMap()
					? StructuredMapCacheEntry.INSTANCE
					: StructuredCollectionCacheEntry.INSTANCE;
		}
		else {
			cacheEntryStructure = UnstructuredCacheEntry.INSTANCE;
		}

		cacheAccess = creationContext.getCollectionCacheAccess( getNavigableRole() );

		int spacesSize = 1 + collectionBinding.getSynchronizedTables().size();
		spaces = new HashSet<>( spacesSize );
		spaces.add(
				collectionBinding.getMappedTable()
						.getNameIdentifier()
						.render( sessionFactory.getServiceRegistry().getService( JdbcServices.class ).getDialect() )
		);
		spaces.addAll( collectionBinding.getSynchronizedTables() );

		this.keyJavaTypeDescriptor = collectionBinding.getKey().getJavaTypeMapping().getJavaTypeDescriptor();

		this.sqlAliasStem = SqlAliasStemHelper.INSTANCE.generateStemFromAttributeName( pluralProperty.getName() );

		int batch = collectionBinding.getBatchSize();
		if ( batch == -1 ) {
			batch = sessionFactory.getSessionFactoryOptions().getDefaultBatchFetchSize();
		}
		batchSize = batch;

		separateCollectionTable = resolveCollectionTable( collectionBinding, creationContext );

		this.extraLazy = collectionBinding.isExtraLazy();
		this.hasOrphanDeletes = collectionBinding.hasOrphanDelete();
		this.inverse = collectionBinding.isInverse();
	}

	/**
	 * todo (6.0) - get CollectionSemantics from Collection boot metadata
	 * 		this should have been resolved already when determining that we have a collection
	 * 		as part of boot metamodel building.  this is how custom collection types are hooked in
	 * 		eventually too
	 * or - todo (7.0) - ^^
	 */
	protected abstract CollectionJavaDescriptor resolveCollectionJtd(
			Collection collectionBinding,
			RuntimeModelCreationContext creationContext);

	protected CollectionJavaDescriptor findOrCreateCollectionJtd(
			Class javaType,
			Collection collectionBinding,
			RuntimeModelCreationContext creationContext) {
		final JavaTypeDescriptorRegistry jtdRegistry = creationContext.getTypeConfiguration()
				.getJavaTypeDescriptorRegistry();

		CollectionJavaDescriptor descriptor = (CollectionJavaDescriptor) jtdRegistry.getDescriptor( javaType );
		if ( descriptor == null ) {
			descriptor = new CollectionJavaDescriptor( javaType, determineSemantics( javaType, collectionBinding ) );
		}

		return descriptor;
	}

	protected CollectionSemantics determineSemantics(Class javaType, Collection collectionBinding) {
		return collectionBinding.getCollectionSemantics();
	}

	protected CollectionJavaDescriptor findCollectionJtd(
			Class javaType,
			RuntimeModelCreationContext creationContext) {
		final JavaTypeDescriptorRegistry jtdRegistry = creationContext.getTypeConfiguration()
				.getJavaTypeDescriptorRegistry();

		CollectionJavaDescriptor descriptor = (CollectionJavaDescriptor) jtdRegistry.getDescriptor( javaType );
		if ( descriptor == null ) {
			throw new HibernateException( "Could not locate JavaTypeDescriptor for requested Java type : " + javaType.getName() );
		}

		return descriptor;
	}

	private boolean fullyInitialized;

	@Override
	public void finishInitialization(
			Collection collectionBinding,
			RuntimeModelCreationContext creationContext) {

		if ( fullyInitialized ) {
			return;
		}

		// todo (6.0) : this is technically not the `separateCollectionTable` as for one-to-many it returns the element entity's table.
		//		need to decide how we want to model tables for collections.
		//
		//	^^ the better option seems to be exposing through `#createRootTableGroup` and `#createTableGroupJoin`

//		separateCollectionTable = resolveCollectionTable( collectionBinding, creationContext );

		final Database database = creationContext.getMetadata().getDatabase();
		final JdbcEnvironment jdbcEnvironment = database.getJdbcEnvironment();
		final Dialect dialect = jdbcEnvironment.getDialect();

		final MappedNamespace defaultNamespace = creationContext.getMetadata().getDatabase().getDefaultNamespace();

		final Identifier defaultCatalogName = defaultNamespace.getName().getCatalog();
		final String defaultCatalogNameString = defaultCatalogName == null ? null : defaultNamespace.getName().getCatalog().render( dialect );

		final Identifier defaultSchemaName = defaultNamespace.getName().getSchema();
		final String defaultSchemaNameString = defaultSchemaName == null ? null : defaultNamespace.getName().getSchema().render( dialect );

		if ( collectionBinding instanceof IdentifierCollection ) {
			final IdentifierCollection identifierCollection = (IdentifierCollection) collectionBinding;

			assert identifierCollection.getIdentifier().getColumnSpan() == 1;
			final Column idColumn = creationContext.getDatabaseObjectResolver().resolveColumn(
					(MappedColumn) identifierCollection.getIdentifier().getMappedColumns().get( 0 )
			);

			final IdentifierGenerator identifierGenerator = (identifierCollection).getIdentifier().createIdentifierGenerator(
					creationContext.getIdentifierGeneratorFactory(),
					dialect,
					defaultCatalogNameString,
					defaultSchemaNameString,
					null
			);

			this.idDescriptor = new CollectionIdentifier(
					( (BasicValueMapping) identifierCollection.getIdentifier() ).resolveType(),
					idColumn,
					identifierGenerator
			);
		}
		else {
			idDescriptor = null;
		}

		this.indexDescriptor = resolveIndexDescriptor( this, collectionBinding, creationContext );
		this.elementDescriptor = resolveElementDescriptor( this, collectionBinding, separateCollectionTable, creationContext );

		this.javaTypeDescriptor = (CollectionJavaDescriptor<C>) collectionBinding.getJavaTypeMapping().getJavaTypeDescriptor();

		this.fullyInitialized = true;
	}

	@SuppressWarnings("unchecked")
	private static <J,T extends Type<J>> CollectionIndex<J> resolveIndexDescriptor(
			PersistentCollectionDescriptor descriptor,
			Collection collectionBinding,
			RuntimeModelCreationContext creationContext) {
		if ( !IndexedCollection.class.isInstance( collectionBinding ) ) {
			return null;
		}

		final IndexedCollection indexedCollectionMapping = (IndexedCollection) collectionBinding;
		final Value indexValueMapping = indexedCollectionMapping.getIndex();

		if ( indexValueMapping instanceof Any ) {
			throw new NotYetImplementedException(  );
		}

		if ( indexValueMapping instanceof BasicValueMapping ) {
			return new BasicCollectionIndexImpl(
					descriptor,
					indexedCollectionMapping,
					creationContext
			);
		}

		if ( indexValueMapping instanceof EmbeddedValueMapping ) {
			return new CollectionIndexEmbeddedImpl(
					descriptor,
					indexedCollectionMapping,
					creationContext
			);
		}

		if ( indexValueMapping instanceof OneToMany || indexValueMapping instanceof ManyToOne ) {
			// NOTE : ManyToOne is used to signify the index is a many-to-many
			return new CollectionIndexEntityImpl(
					descriptor,
					indexedCollectionMapping,
					creationContext
			);
		}

		throw new IllegalArgumentException(
				"Could not determine proper CollectionIndex descriptor to generate.  Unrecognized ValueMapping : " +
						indexValueMapping
		);
	}

	protected Table resolveCollectionTable(
			Collection collectionBinding,
			RuntimeModelCreationContext creationContext) {
		final MappedTable mappedTable = collectionBinding.getMappedTable();
		if ( mappedTable == null ) {
			return null;
		}

		return creationContext.resolve( mappedTable );
	}


	@SuppressWarnings("unchecked")
	private static CollectionElement resolveElementDescriptor(
			AbstractPersistentCollectionDescriptor descriptor,
			Collection bootCollectionDescriptor,
			Table separateCollectionTable,
			RuntimeModelCreationContext creationContext) {

		if ( bootCollectionDescriptor.getElement() instanceof Any ) {
			throw new NotYetImplementedException(  );
		}

		if ( bootCollectionDescriptor.getElement() instanceof BasicValueMapping ) {
			return new BasicCollectionElementImpl(
					descriptor,
					bootCollectionDescriptor,
					creationContext
			);
		}

		if ( bootCollectionDescriptor.getElement() instanceof EmbeddedValueMapping ) {
			return new CollectionElementEmbeddedImpl(
					descriptor,
					bootCollectionDescriptor,
					creationContext
			);
		}

		if ( bootCollectionDescriptor.getElement() instanceof ToOne ) {
			return new CollectionElementEntityImpl(
					descriptor,
					bootCollectionDescriptor,
					CollectionElement.ElementClassification.MANY_TO_MANY,
					creationContext
			);
		}

		if ( bootCollectionDescriptor.getElement() instanceof OneToMany ) {
			return new CollectionElementEntityImpl(
					descriptor,
					bootCollectionDescriptor,
					CollectionElement.ElementClassification.ONE_TO_MANY,
					creationContext
			);
		}

		throw new IllegalArgumentException(
				"Could not determine proper CollectionElement descriptor to generate.  Unrecognized ValueMapping : " +
						bootCollectionDescriptor.getElement()
		);
	}

	public SessionFactoryImplementor getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public String getSqlAliasStem() {
		return sqlAliasStem;
	}

	@Override
	public org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor getKeyJavaTypeDescriptor() {
		return keyJavaTypeDescriptor;
	}

	@Override
	public Set<String> getCollectionSpaces() {
		return spaces;
	}

	@Override
	public void initialize(
			Object loadedKey,
			SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public int getBatchSize() {
		return batchSize;
	}


	@Override
	public CollectionSemantics<C> getSemantics() {
		return getJavaTypeDescriptor().getSemantics();
	}
















	@Override
	public TableGroup createRootTableGroup(
			TableGroupInfo tableGroupInfo,
			RootTableGroupContext tableGroupContext) {
		final SqlAliasBase sqlAliasBase = tableGroupContext.getSqlAliasBaseGenerator().createSqlAliasBase(
				getSqlAliasStem()
		);

		RootTableGroupTableReferenceCollector collector = new RootTableGroupTableReferenceCollector(
				tableGroupContext.getTableSpace(),
				null,
				this,
				tableGroupContext.getTableReferenceJoinType(),
				sqlAliasBase,
				tableGroupInfo.getUniqueIdentifier(),
				tableGroupContext.getLockOptions().getEffectiveLockMode( tableGroupInfo.getIdentificationVariable() )
		);

		applyTableReferenceJoins(
				null,
				tableGroupContext.getTableReferenceJoinType(),
				sqlAliasBase,
				collector
		);

		return collector.generateTableGroup();
	}

	// ultimately, "inclusion" in a collection must defined through a single table whether
	// that be:
	//		1) a "separate" collection table (@JoinTable) - could be either:
	//			a) an @ElementCollection - element/index value are contained on this separate table
	//			b) @ManyToMany - the separate table is an association table with column(s) that define the
	//				FK to an entity table.  NOTE that this is true for element and/or index -
	//				The element must be defined via the FK.  In this model, the index could be:
	// 					1) column(s) on the collection table pointing to the tables for
	// 						the entity that defines the index - only valid for map-keys that
	// 						are entities
	//					2) a basic/embedded value on the collection table
	//					3) a basic/embedded value on the element entity table
	//			c) @OneToOne or @ManyToOne - essentially the same as (b) but with
	//				UKs defined on link table restricting cardinality
	//		2) no separate collection table - only valid for @OneToOne or @ManyToOne, although (1.c)
	//			for alternative mapping for @OneToOne or @ManyToOne.  Here the "collection table"
	//			is the primary table for the associated entity

	private static class RootTableGroupTableReferenceCollector implements TableReferenceJoinCollector {
		private final TableSpace tableSpace;
		private final ColumnReferenceQualifier lhs;
		private final AbstractPersistentCollectionDescriptor collectionDescriptor;
		private final JoinType joinType;
		private final SqlAliasBase sqlAliasBase;
		private final String uniqueIdentifier;
		private final LockMode effectiveLockMode;
		private NavigablePath navigablePath;

		private TableReference primaryTableReference;
		private List<TableReferenceJoin> tableReferenceJoins;
		private Predicate predicate;

		public RootTableGroupTableReferenceCollector(
				TableSpace tableSpace,
				ColumnReferenceQualifier lhs,
				AbstractPersistentCollectionDescriptor collectionDescriptor,
				JoinType joinType,
				SqlAliasBase sqlAliasBase,
				String uniqueIdentifier,
				LockMode effectiveLockMode) {
			this.tableSpace = tableSpace;
			this.lhs = lhs;
			this.collectionDescriptor = collectionDescriptor;
			this.joinType = joinType;
			this.sqlAliasBase = sqlAliasBase;
			this.uniqueIdentifier = uniqueIdentifier;
			this.effectiveLockMode = effectiveLockMode;
		}

		@Override
		public void addRoot(TableReference root) {
			if ( primaryTableReference == null ) {
				primaryTableReference = root;
			}
			else {
				collectTableReferenceJoin( makeJoin( lhs, primaryTableReference ) );
			}

			predicate = makePredicate( lhs, primaryTableReference );
		}

		private TableReferenceJoin makeJoin(ColumnReferenceQualifier lhs, TableReference rootTableReference) {
			return new TableReferenceJoin(
					JoinType.LEFT,
					rootTableReference,
					makePredicate( lhs, rootTableReference )
			);
		}

		private Predicate makePredicate(ColumnReferenceQualifier lhs, TableReference rhs) {
			final Junction conjunction = new Junction( Junction.Nature.CONJUNCTION );

			final CollectionKey collectionKeyDescriptor = collectionDescriptor.getCollectionKeyDescriptor();
			final ForeignKey joinForeignKey = collectionKeyDescriptor.getJoinForeignKey();
			final ForeignKey.ColumnMappings joinFkColumnMappings = joinForeignKey.getColumnMappings();

			for ( ForeignKey.ColumnMappings.ColumnMapping columnMapping: joinFkColumnMappings.getColumnMappings() ) {
				final ColumnReference referringColumnReference = lhs.resolveColumnReference( columnMapping.getReferringColumn() );
				final ColumnReference targetColumnReference = rhs.resolveColumnReference( columnMapping.getTargetColumn() );

				// todo (6.0) : we need some kind of validation here that the column references are properly defined

				// todo (6.0) : we could also handle this using SQL row-value syntax, e.g.:
				//		`... where ... [ (rCol1, rCol2, ...) = (tCol1, tCol2, ...) ] ...`

				conjunction.add(
						new RelationalPredicate(
								RelationalPredicate.Operator.EQUAL,
								referringColumnReference,
								targetColumnReference
						)
				);
			}

			return conjunction;
		}

		@Override
		public void collectTableReferenceJoin(TableReferenceJoin tableReferenceJoin) {
			if ( tableReferenceJoins == null ) {
				tableReferenceJoins = new ArrayList<>();
			}
			tableReferenceJoins.add( tableReferenceJoin );
		}

		@SuppressWarnings("WeakerAccess")
		public TableGroupJoin generateTableGroupJoin() {
			final CollectionTableGroup collectionTableGroup = generateTableGroup();
			return new TableGroupJoin( joinType, collectionTableGroup, predicate );
		}

		@SuppressWarnings("WeakerAccess")
		public CollectionTableGroup generateTableGroup() {
			return new CollectionTableGroup(
					uniqueIdentifier,
					tableSpace,
					collectionDescriptor,
					effectiveLockMode,
					navigablePath,
					primaryTableReference,
					tableReferenceJoins
			);
		}
	}

	@Override
	public TableGroupJoin createTableGroupJoin(
			TableGroupInfo tableGroupInfoSource,
			JoinType joinType,
			JoinedTableGroupContext tableGroupJoinContext) {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public TableGroupJoin createTableGroupJoin(
			SqlAliasBaseGenerator sqlAliasBaseGenerator,
			ColumnReferenceQualifier lhsQualifier,
			NavigablePath navigablePath,
			JoinType joinType,
			String identificationVariable,
			LockMode lockMode,
			TableSpace tableSpace) {
		final SqlAliasBase sqlAliasBase = sqlAliasBaseGenerator.createSqlAliasBase( getSqlAliasStem() );

		final TableReferenceJoinCollectorImpl joinCollector = new TableReferenceJoinCollectorImpl(
				tableSpace,
				lhsQualifier,
				navigablePath,
				lockMode
		);


		applyTableReferenceJoins(
				lhsQualifier,
				joinType,
				sqlAliasBase,
				joinCollector
		);

		// handle optional entity references to be outer joins.
		if ( getDescribedAttribute().isNullable() && JoinType.INNER.equals( joinType ) ) {
			joinType = JoinType.LEFT;
		}

		return joinCollector.generateTableGroup( joinType, navigablePath.getFullPath() );
	}

	@Override
	public EntityDescriptor findEntityOwnerDescriptor() {
		return findEntityOwner( getContainer() );
	}

	private EntityDescriptor findEntityOwner(ManagedTypeDescriptor container) {
		if ( EntityDescriptor.class.isInstance( container ) ) {
			return (EntityDescriptor) container;
		}

		if ( MappedSuperclassDescriptor.class.isInstance( container ) ) {
			throw new NotYetImplementedFor6Exception(
					"resolving the 'entity owner' of a collection 'across' a MappedSuperclass is not yet implemented"
			);
		}

		if ( EmbeddedTypeDescriptor.class.isInstance( container ) ) {
			return findEntityOwner( ( (EmbeddedTypeDescriptor) container.getContainer() ) );
		}

		throw new HibernateException( "Expecting an entity (hierarchy) or embeddable, but found : " + container );
	}

	@Override
	public DomainResult createDomainResult(
			NavigableReference navigableReference,
			String resultVariable,
			DomainResultCreationContext creationContext,
			DomainResultCreationState creationState) {
		final PluralAttributeReference collectionReference = (PluralAttributeReference) navigableReference;

		// todo (6.0) : these should be delegated out to the separate subclasses
		// 		which can better account for what parts of a collection should be available

		final DomainResult collectionIdResult;
		final CollectionIdentifier idDescriptor = collectionReference.getNavigable()
				.getPersistentCollectionDescriptor()
				.getIdDescriptor();
		if ( idDescriptor != null ) {
			collectionIdResult = idDescriptor.createDomainResult( null, creationContext, creationState );
		}
		else {
			collectionIdResult = null;
		}

		final DomainResult indexResult;
		if ( getIndexDescriptor() != null ) {
			indexResult = getIndexDescriptor().createDomainResult(
					collectionReference,
					null,
					creationContext,
					creationState
			);
		}
		else {
			indexResult = null;
		}

		// todo (6.0) : (see above comment) again, should be based on the type of collection (Map, List, etc)
		//		possibly also with for lazy, eager, etc

		return new PluralAttributeResultImpl(
				collectionReference.getNavigable(),
				resultVariable,
				getCollectionKeyDescriptor().createDomainResult( null, creationContext, creationState ),
				collectionIdResult,
				indexResult,
				getElementDescriptor().createDomainResult( collectionReference, null, creationContext, creationState )
		);
	}

	@Override
	public void visitFetchables(Consumer<Fetchable> fetchableConsumer) {
		if ( getIndexDescriptor() instanceof Fetchable ) {
			fetchableConsumer.accept( (Fetchable) getIndexDescriptor() );
		}

		if ( getElementDescriptor() instanceof Fetchable ) {
			fetchableConsumer.accept( (Fetchable) getElementDescriptor() );
		}
	}

	/**
	 * @deprecated todo (6.0) remove
	 */
	@Override
	@Deprecated
	public CollectionSemantics getTuplizer() {
		throw new UnsupportedOperationException();
	}






	@Override
	public ManagedTypeDescriptor getContainer() {
		return container;
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public SqlSelectionGroupNode resolveSqlSelections(
			ColumnReferenceQualifier qualifier,
			SqlAstCreationContext creationContext) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public String asLoggableText() {
		return String.format(
				Locale.ROOT,
				"%s(%s)",
				PersistentCollectionDescriptor.class.getSimpleName(),
				getNavigableRole().getFullPath()
		);
	}

	@Override
	public PluralPersistentAttribute getDescribedAttribute() {
		return attribute;
	}

	@Override
	public CollectionKey getCollectionKeyDescriptor() {
		return foreignKeyDescriptor;
	}

	@Override
	public CollectionIdentifier getIdDescriptor() {
		return idDescriptor;
	}

	@Override
	public CollectionElement getElementDescriptor() {
		return elementDescriptor;
	}

	@Override
	public CollectionIndex getIndexDescriptor() {
		return indexDescriptor;
	}

	@Override
	public CollectionLoader getLoader() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public Table getSeparateCollectionTable() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public boolean isInverse() {
		return inverse;
	}

	@Override
	public boolean hasOrphanDelete() {
		return hasOrphanDeletes;
	}

	@Override
	public boolean isOneToMany() {
		return getElementDescriptor().getClassification() == CollectionElement.ElementClassification.ONE_TO_MANY;
	}

	@Override
	public boolean isExtraLazy() {
		return extraLazy;
	}

	@Override
	public boolean isDirty(Object old, Object value, SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public int getSize(Object loadedKey, SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public Boolean indexExists(
			Object loadedKey, Object index, SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public Boolean elementExists(
			Object loadedKey, Object element, SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public Object getElementByIndex(
			Object loadedKey, Object index, SharedSessionContractImplementor session, Object owner) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public CacheEntryStructure getCacheEntryStructure() {
		return cacheEntryStructure;
	}

	@Override
	public CollectionDataAccess getCacheAccess() {
		return cacheAccess;
	}

	@Override
	public String getMappedByProperty() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public CollectionJavaDescriptor<C> getJavaTypeDescriptor() {
		return javaTypeDescriptor;
	}

	@Override
	public boolean isAffectedByEnabledFilters(SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public void applyTableReferenceJoins(
			ColumnReferenceQualifier lhs,
			JoinType joinType,
			SqlAliasBase sqlAliasBase,
			TableReferenceJoinCollector joinCollector) {

		if ( separateCollectionTable != null ) {
			joinCollector.addRoot( new TableReference( separateCollectionTable, sqlAliasBase.generateNewAlias() ) );
		}

		if ( getIndexDescriptor() != null ) {
			getIndexDescriptor().applyTableReferenceJoins( lhs, joinType, sqlAliasBase, joinCollector );
		}

		getElementDescriptor().applyTableReferenceJoins( lhs, joinType, sqlAliasBase, joinCollector );
	}


	private class TableReferenceJoinCollectorImpl implements TableReferenceJoinCollector {
		private final TableSpace tableSpace;
		private final ColumnReferenceQualifier lhs;
		private final NavigablePath navigablePath;
		private final LockMode lockMode;

		private TableReference rootTableReference;
		private List<TableReferenceJoin> tableReferenceJoins;
		private Predicate predicate;

		@SuppressWarnings("WeakerAccess")
		public TableReferenceJoinCollectorImpl(
				TableSpace tableSpace,
				ColumnReferenceQualifier lhs,
				NavigablePath navigablePath,
				LockMode lockMode) {
			this.tableSpace = tableSpace;
			this.lhs = lhs;
			this.navigablePath = navigablePath;
			this.lockMode = lockMode;
		}

		@Override
		public void addRoot(TableReference root) {
			if ( rootTableReference == null ) {
				rootTableReference = root;
			}
			else {
				collectTableReferenceJoin( makeJoin( lhs, rootTableReference ) );
			}

			predicate = makePredicate( lhs, rootTableReference );
		}

		private TableReferenceJoin makeJoin(ColumnReferenceQualifier lhs, TableReference rootTableReference) {
			return new TableReferenceJoin(
					JoinType.LEFT,
					rootTableReference,
					makePredicate( lhs, rootTableReference )
			);
		}

		private Predicate makePredicate(ColumnReferenceQualifier lhs, TableReference rhs) {
			final Junction conjunction = new Junction( Junction.Nature.CONJUNCTION );

			final ForeignKey joinForeignKey = getCollectionKeyDescriptor().getJoinForeignKey();
			final List<ForeignKey.ColumnMappings.ColumnMapping> columnMappings = joinForeignKey.getColumnMappings().getColumnMappings();

			for ( ForeignKey.ColumnMappings.ColumnMapping columnMapping: columnMappings ) {
				final ColumnReference referringColumnReference = lhs.resolveColumnReference( columnMapping.getReferringColumn() );
				final ColumnReference targetColumnReference = rhs.resolveColumnReference( columnMapping.getTargetColumn() );

				// todo (6.0) : we need some kind of validation here that the column references are properly defined

				// todo (6.0) : could also implement this using SQL row-value syntax, e.g
				//		`where ... [(rCol1, rCol2, ...) = (tCol1, tCol2, ...)] ...`
				//
				// 		we know whether Dialects support it

				conjunction.add(
						new RelationalPredicate(
								RelationalPredicate.Operator.EQUAL,
								referringColumnReference,
								targetColumnReference
						)
				);
			}

			return conjunction;
		}

		@Override
		public void collectTableReferenceJoin(TableReferenceJoin tableReferenceJoin) {
			if ( tableReferenceJoins == null ) {
				tableReferenceJoins = new ArrayList<>();
			}
			tableReferenceJoins.add( tableReferenceJoin );
		}

		@SuppressWarnings("WeakerAccess")
		public TableGroupJoin generateTableGroup(JoinType joinType, String uid) {
			final CollectionTableGroup joinedTableGroup = new CollectionTableGroup(
					uid,
					tableSpace,
					AbstractPersistentCollectionDescriptor.this,
					lockMode,
					navigablePath,
					rootTableReference,
					tableReferenceJoins
			);
			return new TableGroupJoin( joinType, joinedTableGroup, predicate );
		}
	}
}
