/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.entity;

import org.hibernate.HibernateException;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.EntityInitializer;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingOptions;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class DelayedEntityFetchInitializer implements EntityInitializer {
	private final EntityValuedNavigable fetchedNavigable;
	private final FetchParentAccess parentAccess;
	private final DomainResultAssembler fkValueAssembler;

	// per-row state
	private Object entityInstance;
	private Object fkValue;

	protected DelayedEntityFetchInitializer(
			EntityValuedNavigable fetchedNavigable,
			FetchParentAccess parentAccess,
			DomainResultAssembler fkValueAssembler) {
		this.fetchedNavigable = fetchedNavigable;
		this.parentAccess = parentAccess;
		this.fkValueAssembler = fkValueAssembler;
	}

	@Override
	public EntityDescriptor getEntityDescriptor() {
		return fetchedNavigable.getEntityDescriptor();
	}

	@Override
	public Object getEntityInstance() {
		return entityInstance;
	}

	@Override
	public void hydrate(RowProcessingState rowProcessingState) {

	}

	@Override
	public void resolve(RowProcessingState rowProcessingState) {
		final JdbcValuesSourceProcessingOptions processingOptions = rowProcessingState.getJdbcValuesSourceProcessingState() .getProcessingOptions();

		// todo (6.0) : not sure this works for non-PK-based FKs
		fkValue = fkValueAssembler.assemble( rowProcessingState, processingOptions );

		if ( fetchedNavigable.getEntityDescriptor().hasProxy() ) {
			entityInstance = fetchedNavigable.getEntityDescriptor().createProxy(
					fkValue,
					rowProcessingState.getSession()
			);
		}
		else if ( fetchedNavigable.getEntityDescriptor().getBytecodeEnhancementMetadata().isEnhancedForLazyLoading() ) {
			entityInstance = fetchedNavigable.getEntityDescriptor().instantiate(
					fkValue,
					rowProcessingState.getSession()
			);
		}
	}

	@Override
	public Object getResolvedState(
			Navigable navigable,
			RowProcessingState processingState) {
		throw new HibernateException(
				"Entity fetching delayed - unexpected call to access resolved state as FetchParentAccess"
		);
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {
		fkValue = null;
		entityInstance = null;
	}

}
