/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import org.hibernate.LockMode;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityValuedNavigable;
import org.hibernate.metamodel.model.domain.spi.NavigableContainer;
import org.hibernate.metamodel.model.domain.spi.VersionDescriptor;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultCreationContext;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.EntityMappingNode;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractEntityMappingNode extends AbstractFetchParent implements EntityMappingNode {
	private final DomainResult identifierResult;
	private final DomainResult versionResult;
	private final LockMode lockMode;

	public AbstractEntityMappingNode(
			EntityValuedNavigable fetchedNavigable,
			LockMode lockMode,
			NavigablePath navigablePath,
			DomainResultCreationContext creationContext,
			DomainResultCreationState creationState) {
		super( fetchedNavigable, navigablePath );
		this.lockMode = lockMode;

		final EntityDescriptor entityDescriptor = fetchedNavigable.getEntityDescriptor();

		identifierResult = entityDescriptor.getIdentifierDescriptor().createDomainResult(
				null,
				creationContext,
				creationState
		);

		final VersionDescriptor<Object, Object> versionDescriptor = entityDescriptor.getHierarchy().getVersionDescriptor();
		if ( versionDescriptor == null ) {
			versionResult = null;
		}
		else {
			versionResult = versionDescriptor.createDomainResult(
					null,
					creationContext,
					creationState
			);
		}

		// todo (6.0) : handle other special navigables such as row-id, tenant-id, etc

		afterInitialize( creationState );
	}

	@Override
	public EntityValuedNavigable getFetchContainer() {
		return (EntityValuedNavigable) super.getFetchContainer();
	}

	@Override
	public EntityDescriptor getEntityDescriptor() {
		return getFetchContainer().getEntityDescriptor();
	}

	public LockMode getLockMode() {
		return lockMode;
	}

	@Override
	public DomainResult getIdentifierResult() {
		return identifierResult;
	}

	@Override
	public DomainResult getDiscriminatorResult() {
		return null;
	}

	@Override
	public DomainResult getVersionResult() {
		return versionResult;
	}
}
