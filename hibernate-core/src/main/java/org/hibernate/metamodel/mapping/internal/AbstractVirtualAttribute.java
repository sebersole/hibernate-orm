/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal;

import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.StateArrayContributorMapping;
import org.hibernate.metamodel.mapping.StateArrayContributorMetadata;
import org.hibernate.metamodel.mapping.StateArrayContributorMetadataAccess;
import org.hibernate.metamodel.mapping.VirtualModelPart;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.MutabilityPlan;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractVirtualAttribute
		implements AttributeMapping, VirtualModelPart, StateArrayContributorMapping {
	private final NavigableRole navigableRole;

	private final FetchOptions fetchOptions;
	private final PropertyAccess propertyAccess;

	private final ManagedMappingType declaringType;
	private final int position;

	public AbstractVirtualAttribute(
			NavigableRole navigableRole,
			FetchOptions fetchOptions,
			PropertyAccess propertyAccess,
			ManagedMappingType declaringType,
			int position) {
		this.navigableRole = navigableRole;
		this.fetchOptions = fetchOptions;
		this.propertyAccess = propertyAccess;
		this.declaringType = declaringType;
		this.position = position;
	}

	protected abstract boolean isNullable();

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public String getAttributeName() {
		return navigableRole.getLocalName();
	}

	@Override
	public ManagedMappingType getDeclaringType() {
		return declaringType;
	}

	@Override
	public int getStateArrayPosition() {
		return position;
	}

	@Override
	public PropertyAccess getPropertyAccess() {
		return propertyAccess;
	}

	@Override
	public FetchOptions getMappedFetchOptions() {
		return fetchOptions;
	}

	@Override
	public StateArrayContributorMetadataAccess getAttributeMetadataAccess() {
		return entityMappingType -> new StateArrayContributorMetadata() {
			@Override
			public PropertyAccess getPropertyAccess() {
				return AbstractVirtualAttribute.this.getPropertyAccess();
			}

			@Override
			public MutabilityPlan getMutabilityPlan() {
				return ImmutableMutabilityPlan.instance();
			}

			@Override
			public boolean isNullable() {
				return AbstractVirtualAttribute.this.isNullable();
			}

			@Override
			public boolean isInsertable() {
				return false;
			}

			@Override
			public boolean isUpdatable() {
				return false;
			}

			@Override
			public boolean isIncludedInDirtyChecking() {
				return false;
			}

			@Override
			public boolean isIncludedInOptimisticLocking() {
				return false;
			}
		};
	}
}
