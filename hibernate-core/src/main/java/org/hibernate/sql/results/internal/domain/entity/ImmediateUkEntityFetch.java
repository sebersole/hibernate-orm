/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.entity;

import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.hibernate.annotations.NotFoundAction;
import org.hibernate.engine.spi.EntityUniqueKey;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.spi.SingleEntityLoader;
import org.hibernate.metamodel.model.domain.internal.ForeignKeyDomainResult;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResultAssembler;
import org.hibernate.sql.results.spi.EntityInitializer;
import org.hibernate.sql.results.spi.FetchParent;
import org.hibernate.sql.results.spi.FetchParentAccess;
import org.hibernate.sql.results.spi.Initializer;

/**
 * @author Steve Ebersole
 */
public class ImmediateUkEntityFetch extends AbstractImmediateEntityFetch {

	private final ForeignKeyDomainResult keyResult;
	private final BiFunction<Object, SharedSessionContractImplementor,EntityUniqueKey> uniqueKeyGenerator;
	private final NotFoundAction notFoundAction;

	public ImmediateUkEntityFetch(
			FetchParent fetchParent,
			EntityValuedNavigable fetchedNavigable,
			SingleEntityLoader loader,
			ForeignKeyDomainResult keyResult,
			BiFunction<Object, SharedSessionContractImplementor, EntityUniqueKey> uniqueKeyGenerator,
			NotFoundAction notFoundAction) {
		super( fetchParent, fetchedNavigable, loader );
		this.keyResult = keyResult;
		this.uniqueKeyGenerator = uniqueKeyGenerator;
		this.notFoundAction = notFoundAction;
	}

	@Override
	public DomainResultAssembler createAssembler(
			FetchParentAccess parentAccess,
			Consumer<Initializer> collector,
			AssemblerCreationContext creationContext,
			AssemblerCreationState creationState) {
		final EntityInitializer initializer = new ImmediateUkEntityFetchInitializer(
				getFetchedNavigable(),
				loader,
				parentAccess,
				keyResult.createResultAssembler( collector, creationState, creationContext ),
				notFoundAction,
				uniqueKeyGenerator
		);

		collector.accept( initializer );

		return new EntityAssembler( getJavaTypeDescriptor(), initializer );
	}

}
