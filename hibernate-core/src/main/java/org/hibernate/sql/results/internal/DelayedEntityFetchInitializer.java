/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.function.Function;

import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.sql.results.spi.EntityInitializer;
import org.hibernate.sql.results.spi.EntityMappingNode;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.RowProcessingState;

/**
 * @author Steve Ebersole
 */
public class DelayedEntityFetchInitializer implements EntityInitializer {
	private final EntityValuedNavigable fetchedNavigable;
	private final FetchParentAccess parentAccess;
	private final Function<Object, Object> fkValueExtractor;

	private Object entityInstance;

	protected DelayedEntityFetchInitializer(
			EntityValuedNavigable fetchedNavigable,
			FetchParentAccess parentAccess,
			Function<Object,Object> fkValueExtractor) {
		this.fetchedNavigable = fetchedNavigable;
		this.parentAccess = parentAccess;
		this.fkValueExtractor = fkValueExtractor;
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
		if ( fetchedNavigable.getEntityDescriptor().hasProxy() ) {
			entityInstance = fetchedNavigable.getEntityDescriptor().createProxy(
					fkValueExtractor.apply( parentAccess.getFetchParentInstance() ),
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
