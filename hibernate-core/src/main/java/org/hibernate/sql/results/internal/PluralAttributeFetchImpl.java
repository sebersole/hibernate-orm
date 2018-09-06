/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.PluralAttributeFetch;
import org.hibernate.sql.results.spi.PluralAttributeInitializer;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeFetchImpl extends AbstractPluralAttributeMappingNode implements PluralAttributeFetch {
	private final FetchParent fetchParent;
	private final FetchStrategy fetchStrategy;
	private final PluralPersistentAttribute pluralAttribute;
	private final LockMode lockMode;

	public PluralAttributeFetchImpl(
			FetchParent fetchParent,
			PluralPersistentAttribute pluralAttribute,
			String resultVariable,
			FetchStrategy fetchStrategy,
			LockMode lockMode,
			DomainResult keyResult,
			DomainResult identifierResult,
			DomainResult indexResult,
			DomainResult elementResult) {
		super( pluralAttribute, resultVariable, keyResult, identifierResult, indexResult, elementResult );
		this.fetchParent = fetchParent;
		this.fetchStrategy = fetchStrategy;
		this.pluralAttribute = pluralAttribute;
		this.lockMode = lockMode;
	}

	@Override
	public DomainResultAssembler createAssembler(
			FetchParentAccess parentAccess,
			Consumer<Initializer> collector,
			AssemblerCreationContext context,
			AssemblerCreationState creationState) {
		// todo (6.0) : something like:
		//		this allows creation of an initializer specific to the collection nature.

		final PluralAttributeInitializer initializer = pluralAttribute.getPersistentCollectionDescriptor().createInitializer(
				parentAccess,
				this,
				collector,
				context,
				creationState
		);

		collector.accept( initializer );

		return new PluralAttributeAssemblerImpl( initializer );
	}

	@Override
	public PluralPersistentAttribute getFetchedNavigable() {
		return getNavigable();
	}

	@Override
	public FetchParent getFetchParent() {
		return fetchParent;
	}

	@Override
	public FetchStrategy getFetchStrategy() {
		return fetchStrategy;
	}

	@Override
	public boolean isNullable() {
		return true;
	}
}
