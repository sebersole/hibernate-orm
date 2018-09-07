/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.engine.FetchStrategy;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.metamodel.model.domain.spi.NavigableContainer;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.EntityFetch;
import org.hibernate.sql.results.spi.EntityInitializer;
import org.hibernate.sql.results.spi.Fetch;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;

/**
 * @author Steve Ebersole
 */
public class DelayedEntityFetch implements EntityFetch {
	// fetch is one-to-one (#other - SimpleEntity)
	// fetchParent is the root EntityWithOneToOne reference

	// we have access to the parent instance
	// we need to be able to ask the fetched-navigable to
	// be able to extract the "key value" from that parent
	// instance

	// so in terms of EntityWithOneToOne... we need to be able to
	// ask EntityWithOneToOne#other (the EntityValuedNavigable)
	// via Fetchable for the key to load Simple later (by UK or PK?)
	// key given the FetchParentAccess or parent instance

	private final FetchParent fetchParent;
	private final EntityValuedNavigable fetchedNavigable;
	private final FetchStrategy fetchStrategy;
	private final DomainResult fkResult;

	public DelayedEntityFetch(
			FetchParent fetchParent,
			EntityValuedNavigable fetchedNavigable,
			FetchStrategy fetchStrategy,
			DomainResult fkResult) {
		this.fetchParent = fetchParent;
		this.fetchedNavigable = fetchedNavigable;
		this.fetchStrategy = fetchStrategy;
		this.fkResult = fkResult;
	}

	@Override
	public EntityDescriptor getEntityDescriptor() {
		return fetchedNavigable.getEntityDescriptor();
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// for the next few methods...

	// todo (6.0) : make sure null DomainResult returns are handled
	//		*should* only happen in delayed cases, so affect should be
	//		limited, but make sure

	@Override
	public DomainResult getIdentifierResult() {
		// todo (6.0) : maybe the identifier result ought to be generated regardless?
		return null;
	}

	@Override
	public DomainResult getDiscriminatorResult() {
		return null;
	}

	@Override
	public DomainResult getVersionResult() {
		return null;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	@Override
	public FetchParent getFetchParent() {
		return fetchParent;
	}

	@Override
	public EntityValuedNavigable getFetchedNavigable() {
		return fetchedNavigable;
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return fetchStrategy;
	}

	@Override
	public boolean isNullable() {
		return fetchedNavigable.isNullable();
	}

	@Override
	public DomainResultAssembler createAssembler(
			FetchParentAccess parentAccess,
			Consumer<Initializer> collector,
			AssemblerCreationContext creationContext,
			AssemblerCreationState creationState) {
		// todo (6.0) : create and register an Initializer that generates proper lazy representation for a specific entity of the given type
		//		generally a proxy.  should handle registering batch / subselect fetch
		final EntityInitializer initializer = new DelayedEntityFetchInitializer(
				fetchedNavigable,
				parentAccess,
				fkResult.createResultAssembler( collector, creationState, creationContext )
		);

		collector.accept( initializer );

		return new EntityAssembler(
				getEntityDescriptor().getJavaTypeDescriptor(),
				initializer
		);
	}

	@Override
	public NavigableContainer getFetchContainer() {
		return fetchedNavigable;
	}

	@Override
	public List<Fetch> getFetches() {
		return Collections.emptyList();
	}
}
