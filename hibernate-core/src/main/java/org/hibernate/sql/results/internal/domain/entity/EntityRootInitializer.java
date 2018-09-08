/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal.domain.entity;

import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.sql.results.spi.AssemblerCreationContext;
import org.hibernate.sql.results.spi.AssemblerCreationState;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.EntityMappingNode;
import org.hibernate.sql.results.spi.Initializer;

/**
 * InitializerEntity for root
 * @author Steve Ebersole
 */
public class EntityRootInitializer extends AbstractEntityInitializer {
	public EntityRootInitializer(
			EntityMappingNode resultDescriptor,
			LockMode lockMode,
			DomainResult identifierResult,
			DomainResult discriminatorResult,
			DomainResult versionResult,
			Consumer<Initializer> initializerConsumer,
			AssemblerCreationContext creationContext,
			AssemblerCreationState creationState) {
		super(
				resultDescriptor,
				lockMode,
				identifierResult,
				discriminatorResult,
				versionResult,
				initializerConsumer,
				creationContext,
				creationState
		);
	}

	@Override
	protected boolean isEntityReturn() {
		return true;
	}
}
