/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchTiming;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.ToOne;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.MappingModelCreationException;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.entity.EntityFetch;
import org.hibernate.sql.results.graph.entity.internal.EntityFetchDelayedImpl;
import org.hibernate.sql.results.graph.entity.internal.EntityFetchJoinedImpl;
import org.hibernate.sql.results.graph.entity.internal.EntityFetchSelectImpl;
import org.hibernate.type.ForeignKeyDirection;

/**
 * Base support for ToOneKey implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractToOneKey extends AbstractKeyModelPart implements ToOneKey {
	private final ToOneAttributeMapping attributeMapping;

	public AbstractToOneKey(ToOneAttributeMapping attributeMapping) {
		super( attributeMapping.getNavigableRole().append( PART_NAME ) );
		this.attributeMapping = attributeMapping;
	}

	/**
	 * Handle the {@link #getForeignKeyDescriptor()} determination
	 *
	 * @param fkConsumerAccess Access to consumer of generated ForeignKey descriptors
	 */
	protected void postConstruct(
			ToOne bootValue,
			Consumer<Consumer<ForeignKey>> fkConsumerAccess,
			MappingModelCreationProcess creationProcess) {
		final String mappedByProperty = bootValue instanceof OneToOne
				? ( (OneToOne) bootValue ).getMappedByProperty()
				: null;

		if ( mappedByProperty != null ) {
			final EntityMappingType mappedByContainer = attributeMapping.getAssociatedEntityMappingType();

			if ( mappedByContainer == null ) {
				throw new MappingModelCreationException(
						"Could not locate mapped-by container for ToOneKey : " + attributeMapping.getNavigableRole()
								.getFullPath()
				);
			}

			creationProcess.registerSubPartGroupInitializationListener(
					mappedByContainer,
					MappingModelCreationProcess.SubPartGroup.NORMAL,
					() -> {
						final AttributeMapping mappedByPart = mappedByContainer.findAttributeMapping( mappedByProperty );
						if ( !( mappedByPart instanceof ToOneAttributeMapping ) ) {
							throw new MappingModelCreationException(
									"Mapped-by did not reference to-one association : " + attributeMapping.getNavigableRole()
											.getFullPath()
							);
						}

						final ToOneAttributeMapping mappedByAttribute = (ToOneAttributeMapping) mappedByPart;

						super.postConstruct(
								() -> mappedByAttribute,
								fkConsumerAccess,
								creationProcess
						);
					}
			);

			return;
		}

		super.postConstruct(
				null,
				fkConsumerAccess,
				creationProcess
		);
	}

	public ToOneAttributeMapping getAttributeMapping() {
		return attributeMapping;
	}

	@Override
	public ToOneAttributeMapping getMappedModelPart() {
		return getAttributeMapping();
	}

	@Override
	public EntityMappingType findContainingEntityMapping() {
		return getAttributeMapping().findContainingEntityMapping();
	}

	@Override
	public FetchOptions getMappedFetchOptions() {
		return null;
	}

	@Override
	public EntityFetch generateEntityFetch(
			FetchParent fetchParent,
			NavigablePath fetchablePath,
			FetchTiming fetchTiming,
			boolean selected,
			LockMode lockMode,
			String resultVariable,
			DomainResultCreationState creationState) {
		if ( selected ) {
			return new EntityFetchJoinedImpl(
					fetchParent,
					getAttributeMapping(),
					lockMode,
					getAttributeMapping().isNullable(),
					fetchablePath,
					creationState
			);
		}

		// here we have either a subsequent-select or delayed fetch...

		// In either case we need to create a fetch for the key from
		// the other side to use for the load

		final NavigablePath keyPath = fetchablePath.append( PART_NAME );
		final Fetch keyFetch;

		keyFetch = generateKeyFetch( keyPath, fetchParent, creationState );

		if ( fetchTiming == FetchTiming.IMMEDIATE ) {
			// we need a subsequent select fetch
			return new EntityFetchSelectImpl(
					fetchParent,
					getAttributeMapping(),
					lockMode,
					getAttributeMapping().isNullable(),
					fetchablePath,
					keyFetch,
					creationState
			);
		}
		else {
			// we need a delayed
			return new EntityFetchDelayedImpl(
					fetchParent,
					getAttributeMapping(),
					lockMode,
					getAttributeMapping().isNullable(),
					fetchablePath,
					keyFetch
			);
		}
	}

	protected Fetch generateKeyFetch(
			NavigablePath keyPath,
			FetchParent fetchParent,
			DomainResultCreationState creationState) {
		Fetch keyFetch;
		if ( getDirection() == ForeignKeyDirection.REFERRING ) {
			keyFetch = getForeignKeyDescriptor().getTargetSide().getKeyPart().generateFetch(
					fetchParent,
					keyPath,
					FetchTiming.IMMEDIATE,
					getAttributeMapping().isNullable(),
					LockMode.READ,
					null,
					creationState
			);
		}
		else {
			keyFetch = getForeignKeyDescriptor().getReferringSide().getKeyPart().generateFetch(
					fetchParent,
					keyPath,
					FetchTiming.IMMEDIATE,
					getAttributeMapping().isNullable(),
					LockMode.READ,
					null,
					creationState
			);
		}
		return keyFetch;
	}

}
