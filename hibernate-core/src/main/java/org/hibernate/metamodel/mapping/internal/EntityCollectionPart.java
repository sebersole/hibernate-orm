/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Value;
import org.hibernate.metamodel.mapping.CollectionPart;
import org.hibernate.metamodel.mapping.EntityAssociationMapping;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.MappingType;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.internal.fk.JoinTableKey;
import org.hibernate.metamodel.mapping.internal.fk.KeyModelPart;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.collection.internal.EntityCollectionPartTableGroup;
import org.hibernate.sql.results.graph.entity.EntityFetch;
import org.hibernate.sql.results.graph.entity.internal.EntityFetchJoinedImpl;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class EntityCollectionPart
		implements CollectionPart, EntityAssociationMapping, FetchOptions {

	private final NavigableRole navigableRole;
	private final CollectionPersister collectionDescriptor;
	private final Nature nature;
	private final EntityMappingType associatedEntityDescriptor;

	private KeyModelPart keyModelPart;

	@SuppressWarnings("WeakerAccess")
	public EntityCollectionPart(
			CollectionPersister collectionDescriptor,
			Nature nature,
			Collection bootCollectionDescriptor,
			Value bootPartDescriptor,
			EntityMappingType associatedEntityDescriptor,
			MappingModelCreationProcess creationProcess) {
		this.navigableRole = collectionDescriptor.getNavigableRole().appendContainer( nature.getName() );
		this.collectionDescriptor = collectionDescriptor;

		this.nature = nature;
		this.associatedEntityDescriptor = associatedEntityDescriptor;

		creationProcess.registerForeignKeyPostInitCallbacks(
				"EntityCollectionPart FK creation : " + collectionDescriptor.getNavigableRole().getFullPath(),
				() -> {
					final JoinTableKey joinTableKey = collectionDescriptor.getAttributeMapping().getJoinTableKey();
					if ( joinTableKey != null ) {
						// many-to-many
						this.keyModelPart = joinTableKey;
					}
					else {
						this.keyModelPart = collectionDescriptor.getAttributeMapping().getCollectionKey();
					}

					return true;
				}
		);
	}

	@Override
	public Nature getNature() {
		return nature;
	}

	@Override
	public MappingType getPartMappingType() {
		return getEntityMappingType();
	}

	@Override
	public EntityMappingType getEntityMappingType() {
		return associatedEntityDescriptor;
	}

	@Override
	public EntityMappingType getAssociatedEntityMappingType() {
		return getEntityMappingType();
	}

	@Override
	public ModelPart getKeyTargetMatchPart() {
		return keyModelPart.getForeignKeyDescriptor().getTargetSide().getKeyPart();
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getEntityMappingType().getJavaTypeDescriptor();
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public String getFetchableName() {
		return nature.getName();
	}

	@Override
	public FetchOptions getMappedFetchOptions() {
		return this;
	}

	@Override
	public Fetch resolveCircularFetch(
			NavigablePath fetchablePath,
			FetchParent fetchParent,
			DomainResultCreationState creationState) {
		return null;
	}

	@Override
	public EntityFetch generateFetch(
			FetchParent fetchParent,
			NavigablePath fetchablePath,
			FetchTiming fetchTiming,
			boolean selected,
			LockMode lockMode,
			String resultVariable,
			DomainResultCreationState creationState) {
//		assert fetchParent.getReferencedMappingContainer() instanceof PluralAttributeMapping;

		final FromClauseAccess fromClauseAccess = creationState.getSqlAstCreationState().getFromClauseAccess();

		// find or create the TableGroup associated with this `fetchablePath`
		fromClauseAccess.resolveTableGroup(
				fetchablePath,
				np -> {
					// We need to create one.  The Result will be able to find it later by path

					// first, find the collection's TableGroup
					final TableGroup collectionTableGroup = fromClauseAccess.getTableGroup( fetchParent.getNavigablePath() );

					assert collectionTableGroup != null;

					// create a "wrapper" around the collection TableGroup adding in the entity's table references
					final EntityCollectionPartTableGroup collectionPartTableGroup = new EntityCollectionPartTableGroup(
							fetchablePath,
							collectionTableGroup,
							this
					);

					return collectionPartTableGroup;
				}
		);

		return new EntityFetchJoinedImpl( fetchParent, this, lockMode, selected, fetchablePath, creationState );
	}

	@Override
	public <T> DomainResult<T> createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			String resultVariable,
			DomainResultCreationState creationState) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	@Override
	public void applySqlSelections(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			DomainResultCreationState creationState) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	@Override
	public EntityMappingType findContainingEntityMapping() {
		return collectionDescriptor.getAttributeMapping().findContainingEntityMapping();
	}

	@Override
	public int getNumberOfFetchables() {
		return associatedEntityDescriptor.getNumberOfFetchables();
	}

	public String getMappedBy() {
		return collectionDescriptor.getMappedByProperty();
	}

	@Override
	public String toString() {
		return "EntityCollectionPart {" + navigableRole + "}";
	}

	@Override
	public FetchStyle getStyle() {
		return FetchStyle.JOIN;
	}

	@Override
	public FetchTiming getTiming() {
		return FetchTiming.IMMEDIATE;
	}

	@Override
	public KeyModelPart getKeyModelPart() {
		return keyModelPart;
	}

	@Override
	public boolean isNullable() {
		return false;
	}
}
