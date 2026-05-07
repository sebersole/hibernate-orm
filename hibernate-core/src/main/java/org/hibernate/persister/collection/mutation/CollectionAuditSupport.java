/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.persister.collection.mutation;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.hibernate.action.queue.decompose.collection.CollectionMutationTarget;
import org.hibernate.action.queue.meta.CollectionTableDescriptor;
import org.hibernate.audit.ModificationType;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.jdbc.mutation.JdbcValueBindings;
import org.hibernate.engine.jdbc.mutation.ParameterUsage;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.mapping.AuditMapping;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.mutation.EntityAuditSupport;
import org.hibernate.sql.model.MutationOperation;
import org.hibernate.sql.model.MutationOperationGroup;
import org.hibernate.type.Type;

/// Shared construction, diffing, and binding support for audited collection row mutations.
///
/// Like {@link org.hibernate.persister.entity.mutation.EntityAuditSupport},
/// this type stops at the mutation-model boundary. Legacy collection audit
/// coordinators consume the operation groups through the normal mutation
/// executor, while the graph queue materializes the same operations as
/// {@code FlushOperation}s during transaction completion.
///
/// The main invariants guarded here are:
/// <ul>
///     <li>collection audit rows are derived from the collection's original
///     snapshot and final in-memory state;</li>
///     <li>row identity is delegated to {@link AuditCollectionRowMutationHelper}
///     so keyed, indexed, identifier, element, and one-to-many join-column
///     shapes bind consistently;</li>
///     <li>validity-strategy transaction-end updates use the same row identity
///     as audit inserts and restrict on {@code REVEND is null};</li>
///     <li>owner entity MOD audit changes are resolved here as logical audit
///     changes, not by teaching collection coordinators about graph execution.</li>
/// </ul>
///
/// @author Steve Ebersole
///
/// @since 8.0
public class CollectionAuditSupport {
	private final CollectionMutationTarget mutationTarget;
	private final AuditCollectionHelper auditHelper;
	private final EntityAuditSupport ownerMutationSupport;

	public CollectionAuditSupport(
			CollectionMutationTarget mutationTarget,
			SessionFactoryImplementor sessionFactory,
			boolean[] indexColumnIsSettable,
			boolean[] elementColumnIsSettable,
			UnaryOperator<Object> indexIncrementer,
			AuditMapping auditMapping) {
		this.mutationTarget = mutationTarget;
		this.auditHelper = new AuditCollectionHelper(
				mutationTarget,
				sessionFactory,
				indexColumnIsSettable,
				elementColumnIsSettable,
				indexIncrementer,
				auditMapping
		);
		this.ownerMutationSupport = new EntityAuditSupport(
				mutationTarget.getTargetPart().getCollectionDescriptor().getOwnerEntityPersister(),
				sessionFactory
		);
	}

	public CollectionMutationTarget getMutationTarget() {
		return mutationTarget;
	}

	public EntityAuditSupport getOwnerMutationSupport() {
		return ownerMutationSupport;
	}

	/// Resolve the owning entity change implied by an audited collection change.
	///
	/// Collection audit rows record the row-level ADD/DEL changes, while the
	/// owning entity receives a MOD audit row so revision queries can see that the
	/// association changed. The owner is resolved from the persistence context
	/// first, then from the collection wrapper, and finally by entity key lookup.
	/// If the owner cannot be resolved, no owner MOD change is produced; the
	/// collection row audit work can still be recorded.
	public OwnerAuditChange resolveOwnerAuditChange(
			Object ownerId,
			PersistentCollection<?> collection,
			SharedSessionContractImplementor session) {
		final var collectionDescriptor = mutationTarget.getTargetPart().getCollectionDescriptor();
		final var ownerPersister = collectionDescriptor.getOwnerEntityPersister();
		if ( ownerPersister.getAuditMapping() == null ) {
			return null;
		}
		final var ownerEntityKey = session.generateEntityKey( ownerId, ownerPersister );

		final var persistenceContext = session.getPersistenceContextInternal();
		final var persistenceContextOwner = persistenceContext.getCollectionOwner( ownerId, collectionDescriptor );
		final var collectionOwner = persistenceContextOwner != null ? persistenceContextOwner : collection.getOwner();
		final var owner = collectionOwner != null ? collectionOwner : persistenceContext.getEntity( ownerEntityKey );
		if ( owner == null ) {
			return null;
		}

		return new OwnerAuditChange(
				ownerEntityKey,
				owner,
				ownerPersister.getValues( owner )
		);
	}

	AuditCollectionRowMutationHelper getRowMutationHelper() {
		return auditHelper.getRowMutationHelper();
	}

	public MutationOperationGroup getAuditInsertOperationGroup() {
		return auditHelper.getAuditInsertOperationGroup();
	}

	public MutationOperationGroup getTransactionEndUpdateGroup() {
		return auditHelper.getTransactionEndUpdateGroup();
	}

	public AuditCollectionOperation resolveAuditInsertOperation() {
		final var group = getAuditInsertOperationGroup();
		return group == null ? null : createOperation( group.getSingleOperation() );
	}

	public AuditCollectionOperation resolveTransactionEndUpdateOperation() {
		final var group = getTransactionEndUpdateGroup();
		return group == null ? null : createOperation( group.getSingleOperation() );
	}

	private AuditCollectionOperation createOperation(MutationOperation operation) {
		return new AuditCollectionOperation(
				createAuditTableDescriptor( operation ),
				operation
		);
	}

	/// Create the graph queue's descriptor for the collection audit table.
	///
	/// Audit collection rows are executed after flush planning, so they do not
	/// participate in graph dependency analysis. The descriptor therefore carries
	/// only the stable table identity, mutation details, and collection key
	/// metadata needed for batching and binding.
	private CollectionTableDescriptor createAuditTableDescriptor(MutationOperation operation) {
		final var sourceDescriptor = mutationTarget.getCollectionTableDescriptor();
		final var tableMapping = operation.getTableDetails();
		return new CollectionTableDescriptor(
				auditHelper.getAuditTableMapping().getTableName(),
				sourceDescriptor.navigableRole(),
				sourceDescriptor.isJoinTable(),
				sourceDescriptor.isInverse(),
				sourceDescriptor.isSelfReferential(),
				sourceDescriptor.hasUniqueConstraints(),
				sourceDescriptor.cascadeDeleteEnabled(),
				tableMapping.getInsertDetails(),
				tableMapping.getUpdateDetails(),
				tableMapping.getDeleteDetails(),
				sourceDescriptor.deleteAllDetails(),
				sourceDescriptor.keyDescriptor()
		);
	}

	public List<AuditCollectionChange> resolveChanges(
			PersistentCollection<?> collection,
			Object originalSnapshot) {
		final var collectionDescriptor = mutationTarget.getTargetPart().getCollectionDescriptor();
		if ( originalSnapshot == null ) {
			final List<AuditCollectionChange> changes = new ArrayList<>();
			final var entries = collection.entries( collectionDescriptor );
			int entryCount = 0;
			while ( entries.hasNext() ) {
				changes.add( new AuditCollectionChange( entries.next(), entryCount++, ModificationType.ADD ) );
			}
			return changes;
		}
		return computeCollectionChanges( collection, collectionDescriptor, originalSnapshot );
	}

	/// Bind values for one legacy collection audit INSERT.
	///
	/// The [AuditCollectionChange#rawEntry()] is passed through unchanged so
	/// the row mutation helper can use the collection wrapper's normal row access
	/// methods for list, map, id-bag, element, and association rows.
	public void bindAuditInsertValues(
			PersistentCollection<?> collection,
			Object ownerId,
			AuditCollectionChange change,
			SharedSessionContractImplementor session,
			JdbcValueBindings jdbcValueBindings) {
		getRowMutationHelper().bindInsertValues(
				collection,
				ownerId,
				change.rawEntry(),
				change.position(),
				change.modificationType(),
				session,
				jdbcValueBindings
		);
	}

	/// Bind values for one graph-queue collection audit INSERT.
	///
	/// This mirrors the legacy binding variant while using graph queue bindings,
	/// which are keyed by column name because the surrounding flush operation
	/// already carries the mutating audit table descriptor.
	public void bindAuditInsertValues(
			PersistentCollection<?> collection,
			Object ownerId,
			AuditCollectionChange change,
			SharedSessionContractImplementor session,
			org.hibernate.action.queue.bind.JdbcValueBindings jdbcValueBindings) {
		getRowMutationHelper().bindInsertValues(
				collection,
				ownerId,
				change.rawEntry(),
				change.position(),
				change.modificationType(),
				session,
				jdbcValueBindings
		);
	}

	/// Bind values for one legacy validity-strategy transaction-end UPDATE.
	///
	/// The UPDATE closes the previous audit row for the same collection row
	/// identity. Zero affected rows are tolerated by callers because a row being
	/// added for the first time has no previous audit row to close.
	public void bindTransactionEndValues(
			PersistentCollection<?> collection,
			Object ownerId,
			AuditCollectionChange change,
			SharedSessionContractImplementor session,
			JdbcValueBindings jdbcValueBindings) {
		final var tableName = auditHelper.getAuditTableMapping().getTableName();
		final var txId = session.getCurrentTransactionIdentifier();
		final var auditMapping = mutationTarget.getTargetPart().getAuditMapping();
		final var collectionTableName = mutationTarget.getCollectionTableMapping().getTableName();
		final var revEndMapping = auditMapping.getTransactionEndMapping( collectionTableName );

		if ( !auditHelper.useServerTransactionTimestamps() ) {
			jdbcValueBindings.bindValue( txId, tableName, revEndMapping.getSelectionExpression(), ParameterUsage.SET );
		}

		final var revEndTsMapping = auditMapping.getTransactionEndTimestampMapping( collectionTableName );
		if ( revEndTsMapping != null ) {
			jdbcValueBindings.bindValue(
					java.time.Instant.now(),
					tableName,
					revEndTsMapping.getSelectionExpression(),
					ParameterUsage.SET
			);
		}

			getRowMutationHelper().bindRestrictValues(
					collection,
					ownerId,
				change.rawEntry(),
				change.position(),
				session,
				jdbcValueBindings
			);
	}

	/// Bind values for one graph-queue validity-strategy transaction-end UPDATE.
	///
	/// @see #bindTransactionEndValues(PersistentCollection, Object, AuditCollectionChange, SharedSessionContractImplementor, JdbcValueBindings)
	public void bindTransactionEndValues(
			PersistentCollection<?> collection,
			Object ownerId,
			AuditCollectionChange change,
			SharedSessionContractImplementor session,
			org.hibernate.action.queue.bind.JdbcValueBindings jdbcValueBindings) {
		final var auditMapping = mutationTarget.getTargetPart().getAuditMapping();
		final var collectionTableName = mutationTarget.getCollectionTableMapping().getTableName();
		final var revEndMapping = auditMapping.getTransactionEndMapping( collectionTableName );

		if ( !auditHelper.useServerTransactionTimestamps() ) {
			jdbcValueBindings.bindValue(
					session.getCurrentTransactionIdentifier(),
					revEndMapping.getSelectionExpression(),
					ParameterUsage.SET
			);
		}

		final var revEndTsMapping = auditMapping.getTransactionEndTimestampMapping( collectionTableName );
		if ( revEndTsMapping != null ) {
			jdbcValueBindings.bindValue(
					java.time.Instant.now(),
					revEndTsMapping.getSelectionExpression(),
					ParameterUsage.SET
			);
		}

			getRowMutationHelper().bindRestrictValues(
					collection,
					ownerId,
				change.rawEntry(),
				change.position(),
				session,
				jdbcValueBindings
			);
	}

	/// Compute row-level audit changes for an existing collection.
	///
	/// Indexed collections compare by index/key, while unindexed collections match
	/// by element type equality and consume matched snapshot entries. The resulting
	/// ADD/DEL changes intentionally describe audit rows, not SQL DML operations;
	/// replacements are represented as both a DEL of the old row and an ADD of
	/// the new row.
	private List<AuditCollectionChange> computeCollectionChanges(
			PersistentCollection<?> collection,
			CollectionPersister collectionDescriptor,
			Object snapshot) {
		final Type elementType = collectionDescriptor.getElementType();
		if ( collectionDescriptor.hasIndex() ) {
			return snapshot instanceof Map<?, ?> ?
					computeMapChanges( collection, collectionDescriptor, (Map<?, ?>) snapshot, elementType ) :
					computeListChanges( collection, collectionDescriptor, snapshot, elementType );
		}
		else {
			final Collection<?> snapshotElements = snapshot instanceof Map<?, ?> snapshotMap
					? snapshotMap.values()
					: (Collection<?>) snapshot;
			return computeUnindexedChanges( collection, collectionDescriptor, snapshotElements, elementType );
		}
	}

	private List<AuditCollectionChange> computeMapChanges(
			PersistentCollection<?> collection,
			CollectionPersister collectionDescriptor,
			Map<?, ?> snapshot,
			Type elementType) {
		final List<AuditCollectionChange> changes = new ArrayList<>();
		final var currentMap = (Map<?, ?>) collection;

		final var entries = collection.entries( collectionDescriptor );
		int i = 0;
		while ( entries.hasNext() ) {
			final var entry = (Map.Entry<?, ?>) entries.next();
			if ( entry.getValue() != null ) {
				final Object snapshotValue = snapshot.get( entry.getKey() );
				if ( snapshotValue == null || !elementType.isSame( entry.getValue(), snapshotValue ) ) {
					changes.add( new AuditCollectionChange( entry, i, ModificationType.ADD ) );
				}
			}
			i++;
		}

		for ( var entry : snapshot.entrySet() ) {
			if ( entry.getValue() != null ) {
				final Object currentValue = currentMap.get( entry.getKey() );
				if ( currentValue == null || !elementType.isSame( entry.getValue(), currentValue ) ) {
					changes.add( new AuditCollectionChange( entry, i++, ModificationType.DEL ) );
				}
			}
		}

		return changes;
	}

	private List<AuditCollectionChange> computeListChanges(
			PersistentCollection<?> collection,
			CollectionPersister collectionDescriptor,
			Object snapshot,
			Type elementType) {
		final List<AuditCollectionChange> changes = new ArrayList<>();
		final List<?> snapshotList = snapshot instanceof List<?> list ? list : null;
		final int snapshotSize = snapshotList != null ? snapshotList.size() : Array.getLength( snapshot );

		final var entries = collection.entries( collectionDescriptor );
		int i = 0;
		while ( entries.hasNext() ) {
			final Object current = collection.getElement( entries.next() );
			final Object old = i < snapshotSize ? ( snapshotList != null ? snapshotList.get( i ) : Array.get( snapshot, i ) ) : null;
			final boolean same = current != null && old != null && elementType.isSame( current, old );
			if ( current != null && !same ) {
				changes.add( new AuditCollectionChange( current, i, ModificationType.ADD ) );
			}
			if ( old != null && !same ) {
				changes.add( new AuditCollectionChange( old, i, ModificationType.DEL ) );
			}
			i++;
		}

		for ( ; i < snapshotSize; i++ ) {
			final Object old = snapshotList != null ? snapshotList.get( i ) : Array.get( snapshot, i );
			if ( old != null ) {
				changes.add( new AuditCollectionChange( old, i, ModificationType.DEL ) );
			}
		}

		return changes;
	}

	private List<AuditCollectionChange> computeUnindexedChanges(
			PersistentCollection<?> collection,
			CollectionPersister collectionDescriptor,
			Collection<?> snapshotElements,
			Type elementType) {
		final var remaining = new ArrayList<>( snapshotElements );
		final List<AuditCollectionChange> changes = new ArrayList<>();

		final var entries = collection.entries( collectionDescriptor );
		int i = 0;
		while ( entries.hasNext() ) {
			final Object element = collection.getElement( entries.next() );
			if ( element != null ) {
				boolean matched = false;
				for ( var it = remaining.iterator(); it.hasNext(); ) {
					if ( elementType.isSame( element, it.next() ) ) {
						it.remove();
						matched = true;
						break;
					}
				}
				if ( !matched ) {
					changes.add( new AuditCollectionChange( element, i, ModificationType.ADD ) );
				}
			}
			i++;
		}

		for ( var element : remaining ) {
			changes.add( new AuditCollectionChange( element, i++, ModificationType.DEL ) );
		}

		return changes;
	}

	public record AuditCollectionOperation(
			CollectionTableDescriptor tableDescriptor,
			MutationOperation operation) {
	}

	public record AuditCollectionChange(
			Object rawEntry,
			int position,
			ModificationType modificationType) {
	}

	public record OwnerAuditChange(
			EntityKey entityKey,
			Object entity,
			Object[] values) {
	}
}
