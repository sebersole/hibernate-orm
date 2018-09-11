/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.entity;

import java.util.Locale;
import javax.persistence.EntityNotFoundException;

import org.hibernate.annotations.NotFoundAction;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.spi.SingleEntityLoader;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.EntityInitializer;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingOptions;
import org.hibernate.sql.results.spi.RowProcessingState;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractImmediateEntityFetchInitializer implements EntityInitializer {
	private static final Logger log = Logger.getLogger( ImmediatePkEntityFetchInitializer.class );

	private final EntityValuedNavigable fetchedNavigable;
	private final SingleEntityLoader loader;
	private final FetchParentAccess parentAccess;
	private final DomainResultAssembler keyValueAssembler;
	private final NotFoundAction notFoundAction;

	private boolean keyHydrated;
	private Object keyValue;

	private Object entityInstance;

	public AbstractImmediateEntityFetchInitializer(
			EntityValuedNavigable fetchedNavigable,
			SingleEntityLoader loader,
			FetchParentAccess parentAccess,
			DomainResultAssembler keyValueAssembler,
			NotFoundAction notFoundAction) {
		this.fetchedNavigable = fetchedNavigable;
		this.loader = loader;
		this.parentAccess = parentAccess;
		this.keyValueAssembler = keyValueAssembler;
		this.notFoundAction = notFoundAction;
	}

	@Override
	public EntityDescriptor getEntityDescriptor() {
		return fetchedNavigable.getEntityDescriptor();
	}

	public EntityValuedNavigable getFetchedNavigable() {
		return fetchedNavigable;
	}

	public FetchParentAccess getParentAccess() {
		return parentAccess;
	}

	private Object getKeyValue() {
		return keyValue;
	}

	@Override
	public Object getEntityInstance() {
		return entityInstance;
	}

	protected void setEntityInstance(Object entityInstance) {
		assert this.entityInstance == null;
		this.entityInstance = entityInstance;
	}

	protected abstract boolean isLoadingEntityInstance();

	@Override
	public void hydrate(RowProcessingState rowProcessingState) {
		if ( keyHydrated ) {
			return;
		}

		final JdbcValuesSourceProcessingOptions processingOptions = rowProcessingState.getJdbcValuesSourceProcessingState().getProcessingOptions();

		keyValue = keyValueAssembler.assemble( rowProcessingState, processingOptions );
		keyHydrated = true;

		log.debugf( "Hydrated fetched entity key : %s#%s", getEntityDescriptor().getEntityName(), keyValue );

		afterHydrate( keyValue, rowProcessingState );
	}

	protected abstract void afterHydrate(Object keyValue, RowProcessingState rowProcessingState);

	@Override
	public void resolve(RowProcessingState rowProcessingState) {
		if ( !isLoadingEntityInstance() ) {
			return;
		}

		if ( entityInstance != null ) {
			return;
		}

		if ( getKeyValue() == null ) {
			return;
		}

		final SharedSessionContractImplementor session = rowProcessingState.getSession();

		// todo (6.0) : add "non-existent" short-circuit?
		//		basically keep track of EntityDescriptor + non-null-keys we have encountered
		//		that we know do not exist
		//
		//		the downside is that adds over-head just to cater to bad models

		entityInstance = loader.load(
				getKeyValue(),
				rowProcessingState.getQueryOptions().getLockOptions(),
				session
		);

		if ( entityInstance == null ) {
			// we had a key value, but it resolved to no entity...
			// consult NotFoundAction to see how to react
			if ( notFoundAction == NotFoundAction.EXCEPTION ) {
				throw new EntityNotFoundException(
						"Unable to locate entity by key - " +
								loader.getLoadedNavigable().getNavigableName() +
								"#" + getKeyValue()
				);
			}
		}
		else {
			afterLoad( entityInstance, rowProcessingState );
		}
	}

	protected abstract void afterLoad(Object entityInstance, RowProcessingState rowProcessingState);

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		keyHydrated = false;
		keyValue = null;
		entityInstance = null;
	}

	@Override
	public String toString() {
		return String.format(
				Locale.ROOT,
				"%s(%s - %s) - current state : keyValue=%s, entityInstance=%s",
				getClass().getSimpleName(),
				getFetchedNavigable().getNavigableRole().getFullPath(),
				keyType().name(),
				keyValue,
				entityInstance
		);
	}

	protected enum KeyType { PK, UK }

	protected abstract KeyType keyType();
}
