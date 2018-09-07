/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
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

	private Object entityInstance;

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

		if ( fetchedNavigable.getEntityDescriptor().hasProxy() ) {
			entityInstance = fetchedNavigable.getEntityDescriptor().createProxy(
					fkValueAssembler.assemble( rowProcessingState, processingOptions ),
					rowProcessingState.getSession()
			);
		}
		else if ( fetchedNavigable.getEntityDescriptor().getBytecodeEnhancementMetadata().isEnhancedForLazyLoading() ) {
			entityInstance = null;
		}
	}

	@Override
	public void finishUpRow(RowProcessingState rowProcessingState) {

	}
}
