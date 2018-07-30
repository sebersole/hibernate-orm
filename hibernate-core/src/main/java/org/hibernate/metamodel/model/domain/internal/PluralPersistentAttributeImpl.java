/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.util.Collections;
import java.util.List;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.internal.NonNullableTransientDependencies;
import org.hibernate.engine.spi.CollectionKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.MarkerObject;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.spi.AbstractPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.EntityIdentifierSimple;
import org.hibernate.metamodel.model.domain.spi.IdentifiableTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableContainerReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmPluralAttributeReference;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.spi.Fetch;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.LoadingCollectionEntry;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.SqlSelectionGroupNode;
import org.hibernate.sql.results.spi.SqlAstCreationContext;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class PluralPersistentAttributeImpl extends AbstractPersistentAttribute implements PluralPersistentAttribute {
	private static final Logger log = Logger.getLogger( PluralPersistentAttributeImpl.class );

	private static final Object NOT_NULL_COLLECTION = new MarkerObject( "NOT NULL COLLECTION" );

	private final PersistentCollectionDescriptor collectionDescriptor;
	private int stateArrayPosition;

	@SuppressWarnings("unchecked")
	public PluralPersistentAttributeImpl(
			PersistentCollectionDescriptor collectionDescriptor,
			Property bootProperty,
			PropertyAccess propertyAccess,
			RuntimeModelCreationContext creationContext) {
		super( collectionDescriptor.getContainer(), bootProperty, propertyAccess );

		final Collection bootCollectionDescriptor = (Collection) bootProperty.getValue();

		this.collectionDescriptor = collectionDescriptor;

		creationContext.registerCollectionDescriptor( collectionDescriptor, bootCollectionDescriptor );
	}

	@Override
	public PersistentCollectionDescriptor getPersistentCollectionDescriptor() {
		return collectionDescriptor;
	}

	@Override
	public Class getJavaType() {
		return getJavaTypeDescriptor().getJavaType();
	}

	@Override
	public void collectNonNullableTransientEntities(
			Object value,
			ForeignKeys.Nullifier nullifier,
			NonNullableTransientDependencies nonNullableTransientEntities,
			SharedSessionContractImplementor session) {
		// todo (6.0) : prior versions essentially skipped collections when doing this process
		//		verify this is actually correct... the collection can hold non-null, transient entities as
		//		well and not cascade to them, seems like it should add them.  Did previous versions handle
		// 		the collection values differently?
	}

	@Override
	public BindableType getBindableType() {
		return BindableType.PLURAL_ATTRIBUTE;
	}

	@Override
	public ManagedTypeDescriptor getContainer() {
		return getPersistentCollectionDescriptor().getContainer();
	}

	@Override
	public boolean isIncludedInOptimisticLocking() {
		return false;
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return null;
	}

	@Override
	public boolean isAssociation() {
		return false;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	@Override
	public Class getBindableJavaType() {
		return getPersistentCollectionDescriptor().getElementDescriptor().getJavaType();
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public boolean isInsertable() {
		return false;
	}

	@Override
	public boolean isUpdatable() {
		return false;
	}

	@Override
	public int getStateArrayPosition() {
		return stateArrayPosition;
	}

	@Override
	public void setStateArrayPosition(int position) {
		this.stateArrayPosition = position;
	}

	@Override
	public MutabilityPlan getMutabilityPlan() {
		return getJavaTypeDescriptor().getMutabilityPlan();
	}

	@Override
	public Navigable findNavigable(String navigableName) {
		return getPersistentCollectionDescriptor().findNavigable( navigableName );
	}

	@Override
	public void visitNavigables(NavigableVisitationStrategy visitor) {
		getPersistentCollectionDescriptor().visitNavigables( visitor );
	}

	@Override
	public NavigableRole getNavigableRole() {
		return collectionDescriptor.getNavigableRole();
	}

	@Override
	public String asLoggableText() {
		return toString();
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return collectionDescriptor.getJavaTypeDescriptor();
	}

	@Override
	public SqmPluralAttributeReference createSqmExpression(
			SqmFrom sourceSqmFrom,
			SqmNavigableContainerReference containerReference,
			SqmCreationContext creationContext) {
		return new SqmPluralAttributeReference( containerReference, this, creationContext );
	}

	@Override
	public boolean isIncludedInDirtyChecking() {
		// todo (6.0) : depends how we want to handle dirty collections marking container as dirty
		//		this is only important for versioned entities
		//
		// for now return false
		return false;
	}

	@Override
	public Fetch generateFetch(
			FetchParent fetchParent,
			ColumnReferenceQualifier qualifier,
			FetchStrategy fetchStrategy,
			String resultVariable,
			QueryResultCreationContext creationContext) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public FetchStrategy getMappedFetchStrategy() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public ManagedTypeDescriptor getFetchedManagedType() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public List<Column> getColumns() {
		return Collections.emptyList();
	}

	@Override
	public SqlSelectionGroupNode resolveSqlSelections(
			ColumnReferenceQualifier qualifier,
			SqlAstCreationContext creationContext) {
		// collection-id (idbag)?

		// todo (6.0) : this depends on whether the collection is fetched...
		//
		// for now, return nada

		return creationContext.getSqlSelectionResolver().emptySqlSelection();
	}

//		return resolutionContext.getSqlSelectionResolver().emptySqlSelection();
//		final List<ForeignKey.ColumnMappings.ColumnMapping> columnMappings = getPersistentCollectionDescriptor().getCollectionKeyDescriptor()
//				.getJoinForeignKey()
//				.getColumnMappings()
//				.getColumnMappings();
//
//		if ( columnMappings.size() == 1 ) {
//			return resolveSqlSelection( qualifier, resolutionContext, columnMappings.get( 0 ) );
//		}
//
//		final List<SqlSelection> sqlSelections = new ArrayList<>();
//		for ( ForeignKey.ColumnMappings.ColumnMapping columnMapping : columnMappings ) {
//			sqlSelections.add( resolveSqlSelection( qualifier, resolutionContext, columnMapping ) );
//		}
//		return new AggregateSqlSelectionGroupNode( sqlSelections );
//	}
//
//	private SqlSelection resolveSqlSelection(
//			ColumnReferenceQualifier qualifier,
//			SqlSelectionResolutionContext resolutionContext,
//			ForeignKey.ColumnMappings.ColumnMapping columnMapping) {
//		final Expression expression = resolutionContext.getSqlSelectionResolver().resolveSqlExpression(
//				qualifier,
//				columnMapping.getTargetColumn()
//		);
//		return resolutionContext.getSqlSelectionResolver().resolveSqlSelection(
//				expression,
//				,
//		);
//	}

	@Override
	public Object hydrate(Object jdbcValues, SharedSessionContractImplementor session) {
		return NOT_NULL_COLLECTION;
	}

	@Override
	public Object resolveHydratedState(
			Object hydratedForm,
			ExecutionContext executionContext,
			SharedSessionContractImplementor session,
			Object containerInstance) {
		// todo (6.0) : use the collection-key descriptor to resolve the hydrated key value...
		//
		// for now we hack it in such a way that will work for all simple identifiers

		final IdentifiableTypeDescriptor ownerDescriptor = (IdentifiableTypeDescriptor) getContainer();
		final EntityIdentifierSimple identifierDescriptor = (EntityIdentifierSimple) ownerDescriptor.getHierarchy().getIdentifierDescriptor();

		final Object key = identifierDescriptor.asAttribute( identifierDescriptor.getJavaType() ).getPropertyAccess().getGetter().get( containerInstance );

		PersistentCollectionDescriptor collectionDescriptor = getPersistentCollectionDescriptor();
		final PersistenceContext persistenceContext = session.getPersistenceContext();

		// check if collection is currently being loaded
		final LoadingCollectionEntry loadingCollectionEntry = persistenceContext.getLoadContexts().findLoadingCollectionEntry(
				collectionDescriptor,
				key
		);

		PersistentCollection collection = loadingCollectionEntry == null ? null : loadingCollectionEntry.getCollectionInstance();

		if ( collection == null ) {
			final CollectionKey collectionKey = new CollectionKey( collectionDescriptor, key );

			// check if it is already completely loaded, but unowned
			collection = persistenceContext.useUnownedCollection( collectionKey );

			if ( collection == null ) {

				collection = persistenceContext.getCollection( collectionKey );

				if ( collection == null ) {
					// create a new collection wrapper, to be initialized later
					collection = collectionDescriptor.instantiateWrapper( session, key );
					collection.setOwner( containerInstance );

					persistenceContext.addUninitializedCollection( collectionDescriptor, collection, key );

					// todo (6.0) (fetching) : handle fetching or not
					//
					// for now. lazy

//					boolean eager = overridingEager != null ? overridingEager : ! isLazy();
//					if ( initializeImmediately() ) {
//						session.initializeCollection( collection, false );
//					}
//					else if ( eager ) {
//						persistenceContext.addNonLazyCollection( collection );
//					}
//
//					if ( hasHolder() ) {
//						session.getPersistenceContext().addCollectionHolder( collection );
//					}
				}

			}

			if ( log.isTraceEnabled() ) {
				log.tracef(
						"Created collection wrapper: %s",
						MessageHelper.collectionInfoString(
								collectionDescriptor,
								collection,
								key,
								session
						)
				);
			}

		}

		collection.setOwner( containerInstance );
		collection.setCurrentSession( session );

		return collection.getValue();
	}

	@Override
	public Object unresolve(Object value, SharedSessionContractImplementor session) {
//		throw new NotYetImplementedFor6Exception();
		// can't just return null here, since that would
		// cause an owning component to become null
		return NOT_NULL_COLLECTION;
	}

	@Override
	public void dehydrate(
			Object value,
			JdbcValueCollector jdbcValueCollector,
			Clause clause,
			SharedSessionContractImplementor session) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean isDirty(Object originalValue, Object currentValue, SharedSessionContractImplementor session) {
		return !getJavaTypeDescriptor().areEqual( originalValue, currentValue );
	}
}
