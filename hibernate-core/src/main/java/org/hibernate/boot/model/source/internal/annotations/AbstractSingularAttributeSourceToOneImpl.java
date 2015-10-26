/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.Set;

import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.SingularAssociationAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.AssociationHelper;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.EnumConversionHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.AttributeSource;
import org.hibernate.boot.model.source.spi.FetchCharacteristicsSingularAssociation;
import org.hibernate.boot.model.source.spi.SingularAttributeNature;
import org.hibernate.boot.model.source.spi.SingularAttributeSourceToOne;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.CascadeStyles;
import org.hibernate.tuple.GenerationTiming;
import org.hibernate.type.ForeignKeyDirection;

public abstract class AbstractSingularAttributeSourceToOneImpl
		extends SingularAttributeSourceImpl
		implements SingularAttributeSourceToOne {
	private final SingularAssociationAttribute associationAttribute;
	private final Set<CascadeStyle> unifiedCascadeStyles;
	private final FetchCharacteristicsSingularAssociation fetchCharacteristics;

//	private final Set<MappedByAssociationSource> ownedAssociationSources = new HashSet<MappedByAssociationSource>();

	public AbstractSingularAttributeSourceToOneImpl(
			SingularAssociationAttribute associationAttribute) {
		super( associationAttribute );
		this.associationAttribute = associationAttribute;
		this.unifiedCascadeStyles = determineCascadeStyles( associationAttribute );
		this.fetchCharacteristics = AssociationHelper.determineFetchCharacteristicsSingularAssociation(
				associationAttribute.getBackingMember(),
				associationAttribute.getContext()
		);
	}

	private static Set<CascadeStyle> determineCascadeStyles(SingularAssociationAttribute associationAttribute) {
		final Set<CascadeStyle> cascadeStyles = EnumConversionHelper.cascadeTypeToCascadeStyleSet(
				associationAttribute.getJpaCascadeTypes(),
				associationAttribute.getHibernateCascadeTypes(),
				associationAttribute.getContext()
		);
		if ( associationAttribute.isOrphanRemoval() ) {
			cascadeStyles.add( CascadeStyles.DELETE_ORPHAN );
		}
		return cascadeStyles;
	}

	protected SingularAssociationAttribute associationAttribute() {
		return associationAttribute;
	}

	@Override
	public AttributeSource getAttributeSource() {
		return this;
	}

	@Override
	public String getReferencedEntityName() {
		return associationAttribute.getTargetTypeName();
	}

	@Override
	public boolean isIgnoreNotFound() {
		return associationAttribute.isIgnoreNotFound();
	}

//	@Override
//	public Set<MappedByAssociationSource> getOwnedAssociationSources() {
//		return ownedAssociationSources;
//	}
//
//	@Override
//	public void addMappedByAssociationSource(MappedByAssociationSource attributeSource) {
//		if ( attributeSource == null ) {
//			throw new IllegalArgumentException( "attributeSource must be non-null." );
//		}
//		ownedAssociationSources.add( attributeSource );
//	}

	@Override
	public boolean isMappedBy() {
		return false;
	}

	@Override
	public Set<CascadeStyle> getCascadeStyles() {
		return unifiedCascadeStyles;
	}

	@Override
	public FetchCharacteristicsSingularAssociation getFetchCharacteristics() {
		return fetchCharacteristics;
	}

	@Override
	public ForeignKeyDirection getForeignKeyDirection() {
		return getSingularAttributeNature() == SingularAttributeNature.ONE_TO_ONE
				&& !associationAttribute.isOptional()
				&& associationAttribute.getMappedByAttributeName() == null
				? ForeignKeyDirection.FROM_PARENT
				: ForeignKeyDirection.TO_PARENT;
	}

	@Override
	public String toString() {
		return "ToOneAttributeSourceImpl{role=" + associationAttribute.getRole().getFullPath() + '}';
	}


	@Override
	public AttributePath getAttributePath() {
		return associationAttribute.getPath();
	}

	@Override
	public AttributeRole getAttributeRole() {
		return associationAttribute.getRole();
	}

	@Override
	public String getReferencedEntityAttributeName() {
		return null;
	}

	@Override
	public GenerationTiming getGenerationTiming() {
		return null;
	}

	@Override
	public Boolean isInsertable() {
		return associationAttribute().isInsertable();
	}

	@Override
	public Boolean isUpdatable() {
		return associationAttribute().isUpdatable();
	}

	@Override
	public boolean isBytecodeLazy() {
		return false;
	}
}