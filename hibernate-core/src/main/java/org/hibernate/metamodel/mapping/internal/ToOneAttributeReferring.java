/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchStrategy;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.ToOne;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.StateArrayContributorMetadataAccess;
import org.hibernate.metamodel.mapping.internal.fk.ToOneKey;
import org.hibernate.metamodel.mapping.internal.fk.ToOneKeyBasicReferring;
import org.hibernate.metamodel.mapping.internal.fk.ToOneKeyCompositeReferring;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.type.BasicType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

/**
 * A referring-side to-one attribute
 *
 * @author Steve Ebersole
 */
public class ToOneAttributeReferring extends AbstractToOneAttribute {
	public ToOneAttributeReferring(
			String name,
			NavigableRole navigableRole,
			Cardinality cardinality,
			int stateArrayPosition,
			ToOne bootValue,
			StateArrayContributorMetadataAccess attributeMetadataAccess,
			FetchStrategy mappedFetchStrategy,
			EntityMappingType associatedEntityType,
			ManagedMappingType declaringType,
			PropertyAccess propertyAccess,
			MappingModelCreationProcess creationProcess) {
		super(
				name,
				navigableRole,
				cardinality,
				stateArrayPosition,
				bootValue,
				attributeMetadataAccess,
				mappedFetchStrategy,
				associatedEntityType,
				declaringType,
				propertyAccess,
				creationProcess
		);
	}

	@Override
	protected ToOneKey generateKey(
			ToOne bootValue,
			ManagedMappingType declaringType,
			MappingModelCreationProcess creationProcess) {
		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();

		final EntityType toOneType = (EntityType) bootValue.getType();
		final Type keyType = toOneType.getIdentifierOrUniqueKeyType( sessionFactory );

		if ( keyType instanceof BasicType ) {
			return new ToOneKeyBasicReferring(
					bootValue,
					this,
					creationProcess
			);
		}

		return new ToOneKeyCompositeReferring(
				bootValue,
				this,
				declaringType,
				creationProcess
		);
	}

	@Override
	protected Fetch generateNonJoinedKeyFetch(
			FetchParent fetchParent,
			NavigablePath fetchablePath,
			FetchTiming fetchTiming,
			boolean selected,
			LockMode lockMode,
			TableGroup parentTableGroup, DomainResultCreationState creationState) {
		if ( getCardinality() == Cardinality.MANY_TO_ONE || getCardinality() == Cardinality.LOGICAL_ONE_TO_ONE ) {
			return getForeignKeyDescriptor().getReferringSide().getKeyPart().generateFetch(
					fetchParent,
					fetchablePath,
					fetchTiming,
					selected,
					lockMode,
					null,
					creationState
			);
		}

		assert getCardinality() == Cardinality.ONE_TO_ONE;

		return getForeignKeyDescriptor().createTargetKeyFetch(
				fetchablePath,
				parentTableGroup,
				fetchParent,
				creationState
		);
	}
}
