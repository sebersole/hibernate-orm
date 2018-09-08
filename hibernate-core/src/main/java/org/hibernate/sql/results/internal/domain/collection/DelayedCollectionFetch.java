/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.collection;

import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.PluralAttributeFetch;

/**
 * @author Steve Ebersole
 */
public class DelayedCollectionFetch extends PluralAttributeFetchImpl implements PluralAttributeFetch {
	public DelayedCollectionFetch(
			FetchParent fetchParent,
			PluralPersistentAttribute pluralAttribute,
			String resultVariable,
			FetchStrategy fetchStrategy,
			LockMode lockMode,
			DomainResult keyResult) {
		super(
				fetchParent,
				pluralAttribute,
				resultVariable,
				fetchStrategy,
				lockMode,
				keyResult,
				null,
				null,
				null
		);

		assert fetchStrategy.getTiming() == FetchTiming.DELAYED;
	}

	@Override
	public FetchParent getFetchParent() {
		return null;
	}

	@Override
	public PluralPersistentAttribute getFetchedNavigable() {
		return null;
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return null;
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public DomainResultAssembler createAssembler(
			FetchParentAccess parentAccess,
			Consumer<Initializer> collector,
			AssemblerCreationContext creationContext,
			AssemblerCreationState creationState) {
		return null;
	}

	@Override
	public PersistentCollectionDescriptor getCollectionDescriptor() {
		return null;
	}

	@Override
	public DomainResult getKeyResult() {
		return null;
	}

	@Override
	public DomainResult getIdentifierResult() {
		return null;
	}

	@Override
	public DomainResult getIndexResult() {
		return null;
	}

	@Override
	public DomainResult getElementResult() {
		return null;
	}
}
