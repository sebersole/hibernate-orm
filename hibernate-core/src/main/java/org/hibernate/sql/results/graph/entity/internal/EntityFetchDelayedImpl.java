/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.graph.entity.internal;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.results.graph.AssemblerCreationState;
import org.hibernate.sql.results.graph.DomainResultAssembler;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.FetchParentAccess;
import org.hibernate.sql.results.graph.entity.EntityInitializer;

/**
 * @author Andrea Boriero
 * @author Steve Ebersole
 */
public class EntityFetchDelayedImpl extends AbstractNonJoinedEntityFetch {
	private final LockMode lockMode;
	private final boolean nullable;

	private final Fetch keyFetch;

	public EntityFetchDelayedImpl(
			FetchParent fetchParent,
			ToOneAttributeMapping fetchedAttribute,
			LockMode lockMode,
			boolean nullable,
			NavigablePath navigablePath,
			Fetch keyFetch) {
		super( navigablePath, fetchedAttribute, fetchParent );
		this.lockMode = lockMode;
		this.nullable = nullable;
		this.keyFetch = keyFetch;
	}

	@Override
	public FetchTiming getTiming() {
		return FetchTiming.DELAYED;
	}

	@Override
	public boolean hasTableGroup() {
		return false;
	}

	@Override
	public Fetch getKeyFetch() {
		return keyFetch;
	}

	@Override
	public DomainResultAssembler createAssembler(
			FetchParentAccess parentAccess,
			AssemblerCreationState creationState) {
		final EntityInitializer entityInitializer = (EntityInitializer) creationState.resolveInitializer(
				getNavigablePath(),
				() -> new EntityFetchDelayedInitializer(
						getNavigablePath(),
						getEntityValuedModelPart().getEntityMappingType().getEntityPersister(),
						keyFetch.createAssembler( parentAccess, creationState )
				)
		);

		return new EntityAssembler( getFetchedMapping().getJavaTypeDescriptor(), entityInitializer );
	}
}
