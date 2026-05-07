/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.action.queue.internal.audit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.hibernate.Session;
import org.hibernate.action.queue.spi.MutationKind;
import org.hibernate.action.queue.spi.bind.BindPlan;
import org.hibernate.action.queue.spi.bind.JdbcValueBindings;
import org.hibernate.action.queue.spi.bind.OperationResultChecker;
import org.hibernate.action.queue.internal.exec.PlanStepExecutorFactory;
import org.hibernate.action.queue.spi.plan.FlushOperation;
import org.hibernate.audit.EntityTrackingChangesetListener;
import org.hibernate.audit.ModificationType;
import org.hibernate.audit.spi.AuditChangeSet;
import org.hibernate.audit.spi.ChangelogSupplier;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.mutation.EntityAuditSupport;
import org.hibernate.persister.collection.mutation.CollectionAuditSupport;
import org.hibernate.persister.collection.mutation.CollectionAuditSupport.AuditCollectionChange;

/// Transaction-scoped audit mutation collector for the graph action queue.
///
/// @author Steve Ebersole
public class GraphAuditMutationCollector {
	private final AuditChangeSet<EntityAuditSupport, CollectionAuditSupport> changeSet = new AuditChangeSet<>();
	private Object changelog;
	private @Nullable Session changesetSession;

	public void entityChanged(
			EntityKey entityKey,
			Object entity,
			Object[] values,
			ModificationType modificationType,
			EntityAuditSupport mutationSupport) {
		changeSet.addEntityChange( entityKey, entity, values, modificationType, mutationSupport );
	}

	public void collectionChanged(
			CollectionPersister collectionPersister,
			PersistentCollection<?> collection,
			Object ownerId,
			Object originalSnapshot,
			CollectionAuditSupport mutationSupport) {
		changeSet.addCollectionChange( collectionPersister, collection, ownerId, originalSnapshot, mutationSupport );
	}

	public boolean hasWork() {
		return !changeSet.isEmpty();
	}

	public void setChangesetContext(Object changelog, Session changesetSession) {
		this.changelog = changelog;
		this.changesetSession = changesetSession;
	}

	public void executeAuditMutations(SharedSessionContractImplementor session) {
		if ( changeSet.isEmpty() ) {
			return;
		}

		// Audit row bind plans need the current changeset id.  When the id is backed by
		// a @Changelog, resolving it persists the changelog row through a child session.
		// Do that before creating audit-row JDBC batches so the nested changelog insert cannot
		// execute and release an empty parent batch before the audit row is added to it.
		session.getCurrentChangesetIdentifier();

		final List<AuditChangeSet.EntityChange<EntityAuditSupport>> entityChanges = changeSet.entityChanges();
		final List<AuditChangeSet.CollectionChange<CollectionAuditSupport>> collectionChanges =
				changeSet.collectionChanges();
		final List<FlushOperation> operations = new ArrayList<>( entityChanges.size() * 2 + collectionChanges.size() * 4 );
		createEntityTransactionEndOperations( entityChanges, operations );
		createEntityAuditInsertOperations( entityChanges, session, operations );
		createCollectionAuditOperations( collectionChanges, operations );

		try {
			if ( !operations.isEmpty() ) {
				final var executor = PlanStepExecutorFactory.create( session );
				executor.execute( operations, null, null );
				executor.finishUp();
			}
			executeChangesetCallbacks( entityChanges, session );
		}
		finally {
			clear();
		}
	}

	public void clear() {
		changeSet.clear();
		changelog = null;
		changesetSession = null;
	}

	private void executeChangesetCallbacks(
			List<AuditChangeSet.EntityChange<EntityAuditSupport>> entityChanges,
			SharedSessionContractImplementor session) {
		if ( changelog == null || entityChanges.isEmpty() ) {
			return;
		}

		final var supplier = ChangelogSupplier.resolve( session.getFactory().getServiceRegistry() );
		final var listener = resolveTrackingListener( supplier );
		if ( listener != null ) {
			for ( var change : entityChanges ) {
				final var entityKey = change.entityKey();
				listener.entityChanged(
						entityKey.getPersister().getMappedClass(),
						entityKey.getIdentifier(),
						change.modificationType(),
						changelog
				);
			}
		}
		populateModifiedEntityNames( supplier, entityChanges, session );
	}

	private void populateModifiedEntityNames(
			@Nullable ChangelogSupplier<?> supplier,
			List<AuditChangeSet.EntityChange<EntityAuditSupport>> entityChanges,
			SharedSessionContractImplementor session) {
		if ( supplier == null || supplier.getModifiedEntitiesProperty() == null ) {
			return;
		}

		final var persister = session.getEntityPersister(
				supplier.getChangelogClass().getName(),
				changelog
		);
		final var attr = persister.findAttributeMapping( supplier.getModifiedEntitiesProperty() );
		//noinspection unchecked
		var entityNames = (Set<String>) persister.getValue( changelog, attr.getStateArrayPosition() );
		if ( entityNames == null ) {
			entityNames = new HashSet<>();
			persister.setValue( changelog, attr.getStateArrayPosition(), entityNames );
		}
		for ( var change : entityChanges ) {
			entityNames.add( change.entityKey().getEntityName() );
		}
		if ( changesetSession != null ) {
			changesetSession.flush();
		}
	}

	private static @Nullable EntityTrackingChangesetListener resolveTrackingListener(
			@Nullable ChangelogSupplier<?> supplier) {
		if ( supplier != null && supplier.getListener() instanceof EntityTrackingChangesetListener listener ) {
			return listener;
		}
		return null;
	}

	private void createEntityTransactionEndOperations(
			List<AuditChangeSet.EntityChange<EntityAuditSupport>> changes,
			List<FlushOperation> operations) {
		int ordinal = 0;
		for ( var change : changes ) {
			final EntityAuditSupport mutationSupport = change.entityAuditHandler();
			final Object id = change.entityKey().getIdentifier();
			for ( var operation : mutationSupport.resolveTransactionEndUpdateOperations() ) {
				operations.add( new FlushOperation(
						operation.tableDescriptor(),
						MutationKind.UPDATE,
						operation.operation(),
						new TransactionEndBindPlan( mutationSupport, operation.tableIndex(), id, change.modificationType() ),
						ordinal++,
						"GraphAuditEntity(" + change.entityKey().getEntityName() + "#end)"
				) );
			}
		}
	}

	private void createEntityAuditInsertOperations(
			List<AuditChangeSet.EntityChange<EntityAuditSupport>> changes,
			SharedSessionContractImplementor session,
			List<FlushOperation> operations) {
		int ordinal = operations.size();
		for ( var change : changes ) {
			final EntityAuditSupport mutationSupport = change.entityAuditHandler();
			final Object id = change.entityKey().getIdentifier();
			final boolean[] propertyInclusions = mutationSupport.resolvePropertyInclusions(
					change.entity(),
					change.values(),
					session
			);
			for ( var operation :
					mutationSupport.resolveAuditInsertOperations( propertyInclusions, change.entity(), session ) ) {
				operations.add( new FlushOperation(
						operation.tableDescriptor(),
						MutationKind.INSERT,
						operation.operation(),
						new AuditInsertBindPlan(
								mutationSupport,
								operation.tableIndex(),
								id,
								change.values(),
								propertyInclusions,
								change.modificationType()
						),
						ordinal++,
						"GraphAuditEntity(" + change.entityKey().getEntityName() + "#insert)"
				) );
			}
		}
	}

	private void createCollectionAuditOperations(
			List<AuditChangeSet.CollectionChange<CollectionAuditSupport>> collectionChanges,
			List<FlushOperation> operations) {
		int ordinal = operations.size();
		for ( var collectionChange : collectionChanges ) {
			final var mutationSupport = collectionChange.collectionAuditHandler();
			final var changes = mutationSupport.resolveChanges(
					collectionChange.collection(),
					collectionChange.originalSnapshot()
			);
			final var transactionEndOperation = mutationSupport.resolveTransactionEndUpdateOperation();
			if ( transactionEndOperation != null ) {
				for ( var change : changes ) {
					operations.add( new FlushOperation(
							transactionEndOperation.tableDescriptor(),
							MutationKind.UPDATE,
							transactionEndOperation.operation(),
							new CollectionTransactionEndBindPlan(
									mutationSupport,
									collectionChange.collection(),
									collectionChange.ownerId(),
									change
							),
							ordinal++,
							"GraphAuditCollection(" + mutationSupport.getMutationTarget().getRolePath() + "#end)"
					) );
				}
			}
			final var insertOperation = mutationSupport.resolveAuditInsertOperation();
			if ( insertOperation != null ) {
				for ( var change : changes ) {
					operations.add( new FlushOperation(
							insertOperation.tableDescriptor(),
							MutationKind.INSERT,
							insertOperation.operation(),
							new CollectionAuditInsertBindPlan(
									mutationSupport,
									collectionChange.collection(),
									collectionChange.ownerId(),
									change
							),
							ordinal++,
							"GraphAuditCollection(" + mutationSupport.getMutationTarget().getRolePath() + "#insert)"
					) );
				}
			}
		}
	}

	private record AuditInsertBindPlan(
			EntityAuditSupport mutationSupport,
			int tableIndex,
			Object id,
			Object[] values,
			boolean[] propertyInclusions,
			ModificationType modificationType) implements BindPlan {

		@Override
		public Object getEntityId() {
			return id;
		}

		@Override
		public void bindValues(
				JdbcValueBindings valueBindings,
				FlushOperation flushOperation,
				SharedSessionContractImplementor session) {
			mutationSupport.bindAuditInsertValues(
					tableIndex,
					id,
					values,
					propertyInclusions,
					modificationType,
					session,
					valueBindings
			);
		}
	}

	private record TransactionEndBindPlan(
			EntityAuditSupport mutationSupport,
			int tableIndex,
			Object id,
			ModificationType modificationType) implements BindPlan, OperationResultChecker {

		@Override
		public Object getEntityId() {
			return id;
		}

		@Override
		public void bindValues(
				JdbcValueBindings valueBindings,
				FlushOperation flushOperation,
				SharedSessionContractImplementor session) {
			mutationSupport.bindTransactionEndValues( tableIndex, id, session, valueBindings );
		}

		@Override
		public boolean checkResult(
				int affectedRowCount,
				int batchPosition,
				String sqlString,
				org.hibernate.engine.spi.SessionFactoryImplementor sessionFactory) throws SQLException {
			return EntityAuditSupport.verifyTransactionEndOutcome(
					affectedRowCount,
					modificationType,
					mutationSupport.getEntityPersister().getEntityName(),
					id
			);
		}
	}

	private record CollectionAuditInsertBindPlan(
			CollectionAuditSupport mutationSupport,
			PersistentCollection<?> collection,
			Object ownerId,
			AuditCollectionChange change) implements BindPlan {

		@Override
		public void bindValues(
				JdbcValueBindings valueBindings,
				FlushOperation flushOperation,
				SharedSessionContractImplementor session) {
			mutationSupport.bindAuditInsertValues( collection, ownerId, change, session, valueBindings );
		}
	}

	private record CollectionTransactionEndBindPlan(
			CollectionAuditSupport mutationSupport,
			PersistentCollection<?> collection,
			Object ownerId,
			AuditCollectionChange change) implements BindPlan, OperationResultChecker {

		@Override
		public void bindValues(
				JdbcValueBindings valueBindings,
				FlushOperation flushOperation,
				SharedSessionContractImplementor session) {
			mutationSupport.bindTransactionEndValues( collection, ownerId, change, session, valueBindings );
		}

		@Override
		public boolean checkResult(
				int affectedRowCount,
				int batchPosition,
				String sqlString,
				org.hibernate.engine.spi.SessionFactoryImplementor sessionFactory) {
			return true;
		}
	}
}
