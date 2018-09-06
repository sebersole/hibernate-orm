/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.DomainResultCreationContext;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.EntityFetch;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.AssemblerCreationContext;

/**
 * @author Steve Ebersole
 */
public class EntityFetchImpl extends AbstractEntityMappingNode implements EntityFetch {
	private final FetchParent fetchParent;
	private final FetchStrategy fetchStrategy;

	public EntityFetchImpl(
			FetchParent fetchParent,
			EntityValuedNavigable fetchedNavigable,
			LockMode lockMode,
			NavigablePath navigablePath,
			FetchStrategy fetchStrategy,
			DomainResultCreationContext creationContext,
			DomainResultCreationState creationState) {
		super( fetchedNavigable, lockMode, navigablePath, creationContext, creationState );
		this.fetchParent = fetchParent;

		this.fetchStrategy = fetchStrategy;
	}

	@Override
	public FetchParent getFetchParent() {
		return fetchParent;
	}

	@Override
	public EntityValuedNavigable getFetchedNavigable() {
		return getFetchContainer();
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return fetchStrategy;
	}

	@Override
	public boolean isNullable() {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public DomainResultAssembler createAssembler(
			FetchParentAccess parentAccess,
			Consumer<Initializer> collector,
			AssemblerCreationContext creationContext,
			AssemblerCreationState creationState) {
		final EntityFetchInitializer initializer = new EntityFetchInitializer(
				parentAccess,
				this,
				getLockMode(),
				collector,
				creationContext,
				creationState
		);

		collector.accept( initializer );

		return new EntityAssembler( getEntityDescriptor().getJavaTypeDescriptor(), initializer );
	}
}
