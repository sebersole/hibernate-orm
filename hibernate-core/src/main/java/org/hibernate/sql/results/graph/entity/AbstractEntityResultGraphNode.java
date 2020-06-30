/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.graph.entity;

import org.hibernate.LockMode;
import org.hibernate.metamodel.mapping.EntityDiscriminatorMapping;
import org.hibernate.metamodel.mapping.EntityIdentifierMapping;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.EntityRowIdMapping;
import org.hibernate.metamodel.mapping.EntityValuedModelPart;
import org.hibernate.metamodel.mapping.EntityVersionMapping;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.results.graph.AbstractFetchParent;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * AbstractFetchParent sub-class for entity-valued graph nodes
 *
 * @author Steve Ebersole
 */
public abstract class AbstractEntityResultGraphNode extends AbstractFetchParent implements EntityResultGraphNode {
	private final EntityValuedModelPart referencedModelPart;

	private final Fetch identifierFetch;
	private final DomainResult discriminatorResult;
	private final DomainResult versionResult;
	private final DomainResult<Object> rowIdResult;
	private final LockMode lockMode;

	private final EntityMappingType targetType;

	public AbstractEntityResultGraphNode(
			EntityValuedModelPart referencedModelPart,
			LockMode lockMode,
			NavigablePath navigablePath,
			DomainResultCreationState creationState) {
		this( referencedModelPart, lockMode, navigablePath, null, creationState );
	}

	@SuppressWarnings("WeakerAccess")
	public AbstractEntityResultGraphNode(
			EntityValuedModelPart referencedModelPart,
			LockMode lockMode,
			NavigablePath navigablePath,
			EntityMappingType targetType,
			DomainResultCreationState creationState) {
		super( referencedModelPart.getEntityMappingType(), navigablePath );
		this.referencedModelPart = referencedModelPart;
		this.lockMode = lockMode;
		this.targetType = targetType;

		final EntityMappingType entityDescriptor = referencedModelPart.getEntityMappingType();

		final TableGroup entityTableGroup = creationState.getSqlAstCreationState().getFromClauseAccess().findTableGroup( navigablePath );

		final EntityIdentifierMapping identifierMapping = entityDescriptor.getIdentifierMapping();

		if ( navigablePath.getParent() == null && !creationState.forceIdentifierSelection() ) {
			identifierFetch = null;
			creationState.buildKeyFetch( this );
		}
		else {
			identifierFetch = creationState.buildKeyFetch( this );
		}

		final EntityDiscriminatorMapping discriminatorMapping = getDiscriminatorMapping( entityDescriptor, entityTableGroup );
		if ( discriminatorMapping != null ) {
			discriminatorResult = discriminatorMapping.createDomainResult(
					navigablePath.append( EntityDiscriminatorMapping.ROLE_NAME ),
					entityTableGroup,
					null,
					creationState
			);
		}
		else {
			discriminatorResult = null;
		}

		final EntityVersionMapping versionDescriptor = entityDescriptor.getVersionMapping();
		if ( versionDescriptor == null ) {
			versionResult = null;
		}
		else {
			versionResult = versionDescriptor.createDomainResult(
					navigablePath.append( versionDescriptor.getFetchableName() ),
					entityTableGroup,
					null,
					creationState
			);
		}

		final EntityRowIdMapping rowIdMapping = entityDescriptor.getRowIdMapping();
		if ( rowIdMapping == null ) {
			rowIdResult = null;
		}
		else {
			rowIdResult = rowIdMapping.createDomainResult(
					navigablePath.append( rowIdMapping.getRowIdName() ),
					entityTableGroup,
					AbstractEntityPersister.ROWID_ALIAS,
					creationState
			);
		}
	}

	protected EntityDiscriminatorMapping getDiscriminatorMapping(
			EntityMappingType entityDescriptor,
			TableGroup entityTableGroup) {
		return entityDescriptor.getDiscriminatorMapping();
	}

	@Override
	public EntityMappingType getReferencedMappingContainer() {
		return getEntityValuedModelPart().getEntityMappingType();
	}

	@Override
	public EntityValuedModelPart getEntityValuedModelPart() {
		return referencedModelPart;
	}

	@Override
	public JavaTypeDescriptor getResultJavaTypeDescriptor() {
		return getEntityValuedModelPart().getEntityMappingType().getMappedJavaTypeDescriptor();
	}

	public LockMode getLockMode() {
		return lockMode;
	}

	public DomainResult getDiscriminatorResult() {
		return discriminatorResult;
	}

	public DomainResult getVersionResult() {
		return versionResult;
	}

	public DomainResult<Object> getRowIdResult() {
		return rowIdResult;
	}
}
