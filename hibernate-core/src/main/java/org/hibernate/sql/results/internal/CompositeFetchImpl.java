/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.function.Consumer;

import org.hibernate.engine.FetchStrategy;
import org.hibernate.metamodel.model.domain.internal.SingularPersistentAttributeEmbedded;
import org.hibernate.metamodel.model.domain.spi.EmbeddedValuedNavigable;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.CompositeFetch;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;

/**
 * @author Steve Ebersole
 */
public class CompositeFetchImpl extends AbstractFetchParent implements CompositeFetch {
	private final FetchParent fetchParent;
	private final SingularPersistentAttributeEmbedded fetchedNavigable;
	private final FetchStrategy fetchStrategy;


	public CompositeFetchImpl(
			FetchParent fetchParent,
			SingularPersistentAttributeEmbedded fetchedNavigable,
			FetchStrategy fetchStrategy,
			DomainResultCreationState creationState) {
		super(
				fetchedNavigable,
				fetchParent.getNavigablePath().append( fetchedNavigable.getNavigableName() )
		);
		this.fetchParent = fetchParent;
		this.fetchedNavigable = fetchedNavigable;
		this.fetchStrategy = fetchStrategy;

		afterInitialize( creationState );
	}


	@Override
	public FetchParent getFetchParent() {
		return fetchParent;
	}

	@Override
	public EmbeddedValuedNavigable getCompositeNavigableDescriptor() {
		return getFetchedNavigable();
	}

	@Override
	public SingularPersistentAttributeEmbedded getFetchedNavigable() {
		return fetchedNavigable;
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return fetchStrategy;
	}

	@Override
	public boolean isNullable() {
		return fetchedNavigable.isOptional();
	}


	@Override
	public DomainResultAssembler createAssembler(
			FetchParentAccess parentAccess,
			Consumer<Initializer> collector,
			AssemblerCreationContext context,
			AssemblerCreationState creationState) {
		final CompositeFetchInitializerImpl initializer = new CompositeFetchInitializerImpl(
				parentAccess,
				this,
				collector,
				context,
				creationState
		);

		collector.accept( initializer );

		return new CompositeAssembler( initializer );
	}
}
