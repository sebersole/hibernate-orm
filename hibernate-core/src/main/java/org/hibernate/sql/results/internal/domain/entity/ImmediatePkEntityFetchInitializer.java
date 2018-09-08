/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.entity;

import org.hibernate.annotations.NotFoundAction;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.spi.SingleEntityLoader;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.LoadingEntityEntry;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class ImmediatePkEntityFetchInitializer extends AbstractImmediateEntityFetchInitializer {
	private EntityKey entityKey;
	private Object entityInstance;

	private boolean isLoadingEntity;

	public ImmediatePkEntityFetchInitializer(
			EntityValuedNavigable fetchedNavigable,
			SingleEntityLoader loader,
			FetchParentAccess parentAccess,
			DomainResultAssembler keyValueAssembler,
			NotFoundAction notFoundAction) {
		super( fetchedNavigable, loader, parentAccess, keyValueAssembler, notFoundAction );
	}


	@Override
	protected void afterHydrate(Object keyValue, RowProcessingState rowProcessingState) {
		if ( entityKey != null ) {
			return;
		}

		if ( keyValue == null ) {
			return;
		}

		entityKey = new EntityKey( keyValue, getEntityDescriptor() );

		final SharedSessionContractImplementor session = rowProcessingState.getSession();

		final LoadingEntityEntry existingEntry = session.getPersistenceContext()
				.getLoadContexts()
				.findLoadingEntityEntry( entityKey );

		if ( existingEntry != null ) {
			entityInstance = existingEntry.getEntityInstance();
			isLoadingEntity = false;
			return;
		}

		final Object managed = session.getPersistenceContext().getEntity( entityKey );
		if ( managed != null ) {
			entityInstance = managed;
			isLoadingEntity = false;
			return;
		}

		// todo (6.0) : second-level cache

		final Object entityInstance = getEntityDescriptor().instantiate( keyValue, session );
		final LoadingEntityEntry entityEntry = new LoadingEntityEntry(
				entityKey,
				getEntityDescriptor(),
				entityInstance,
				// todo (6.0) : rowId
				null
		);

		rowProcessingState.getJdbcValuesSourceProcessingState().registerLoadingEntity( entityKey, entityEntry );
		isLoadingEntity = true;
	}


	@Override
	protected boolean isLoadingEntityInstance() {
		return isLoadingEntity;
	}

	@Override
	protected void afterLoad(Object entityInstance, RowProcessingState rowProcessingState) {
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		entityKey = null;
		entityInstance = null;
		isLoadingEntity = false;
	}

	@Override
	public Object getEntityInstance() {
		return entityInstance;
	}
}
