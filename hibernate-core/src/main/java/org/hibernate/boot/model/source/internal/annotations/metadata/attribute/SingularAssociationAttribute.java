/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.ArrayList;
import java.util.Set;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;

import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.AssociationHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.FetchCharacteristics;
import org.hibernate.boot.model.source.spi.FetchCharacteristicsSingularAssociation;
import org.hibernate.mapping.PropertyGeneration;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

/**
 * @author Steve Ebersole
 * @author Gail Badner
 */
public class SingularAssociationAttribute
		extends AbstractSingularAttribute
		implements FetchableAttribute, AssociationAttribute {

	private final ToOneNature toOneNature;
	private final String target;
	private String mappedByAttributeName;
	private final boolean isInverse;
	private final Set<CascadeType> jpaCascadeTypes;
	private final Set<org.hibernate.annotations.CascadeType> hibernateCascadeTypes;
	private final boolean isOrphanRemoval;
	private final boolean ignoreNotFound;
	private final boolean isOptional;

	private final FetchCharacteristicsSingularAssociation fetchCharacteristics;
	private final boolean isLazy;

	private final AnnotationInstance joinTableAnnotation;
	private ArrayList<Column> joinColumnValues = new ArrayList<Column>();

	private ArrayList<Column> inverseJoinColumnValues = new ArrayList<Column>();

	private final AnnotationInstance mapsIdAnnotation;
	private final boolean hasPrimaryKeyJoinColumn;

	public SingularAssociationAttribute(
			ManagedTypeMetadata container,
			String attributeName,
			AttributePath attributePath,
			AttributeRole attributeRole,
			MemberDescriptor backingMember,
			AccessType accessType,
			String accessorStrategy) {
		super(
				container,
				attributeName,
				attributePath,
				attributeRole,
				backingMember,
				AttributeNature.TO_ONE,
				accessType,
				accessorStrategy
		);

		this.toOneNature = determineToOneNature();

		final AnnotationInstance associationAnnotation = memberAnnotationMap().get( toOneNature.annotationDotName() );

		final ClassInfo targetClassInfo = AssociationHelper.determineTarget(
				backingMember,
				associationAnnotation,
				backingMember.type(),
				getContext()
		);
		this.target = targetClassInfo.name().toString();

		this.mappedByAttributeName = AssociationHelper.determineMappedByAttributeName( associationAnnotation );

		this.fetchCharacteristics = AssociationHelper.determineFetchCharacteristicsSingularAssociation(
				backingMember,
				getContext()
		);
		this.isLazy = AssociationHelper.determineWhetherIsLazy(
				associationAnnotation,
				memberAnnotationMap().get( HibernateDotNames.LAZY_TO_ONE ),
				backingMember,
				fetchCharacteristics.getFetchStyle(),
				false,
				getContext()
		);

		this.isOptional = AssociationHelper.determineOptionality( associationAnnotation );

		this.jpaCascadeTypes = AssociationHelper.determineCascadeTypes( associationAnnotation );
		this.hibernateCascadeTypes = AssociationHelper.determineHibernateCascadeTypes( backingMember, getContext() );
		this.isOrphanRemoval = AssociationHelper.determineOrphanRemoval( associationAnnotation );
		this.ignoreNotFound = AssociationHelper.determineWhetherToIgnoreNotFound( backingMember, getContext() );

		this.mapsIdAnnotation = memberAnnotationMap().get( JpaDotNames.MAPS_ID );

		if ( this.mappedByAttributeName == null ) {
			// todo : not at all a fan of this mess...
			AssociationHelper.processJoinColumnAnnotations(
					backingMember,
					joinColumnValues,
					getContext()
			);
			AssociationHelper.processJoinTableAnnotations(
					backingMember,
					joinColumnValues,
					inverseJoinColumnValues,
					getContext()
			);
			this.joinTableAnnotation = AssociationHelper.extractExplicitJoinTable(
					backingMember,
					getContext()
			);

			isInverse = false;
		}
		else {
			this.joinTableAnnotation = null;
			isInverse = true;
		}
		joinColumnValues.trimToSize();
		inverseJoinColumnValues.trimToSize();

		this.hasPrimaryKeyJoinColumn = memberAnnotationMap().containsKey( JpaDotNames.PRIMARY_KEY_JOIN_COLUMN )
				|| memberAnnotationMap().containsKey( JpaDotNames.PRIMARY_KEY_JOIN_COLUMNS );
	}

	private ToOneNature determineToOneNature() {
		if ( memberAnnotationMap().containsKey( JpaDotNames.MANY_TO_MANY ) ) {
			return ToOneNature.MANY_TO_ONE;
		}

		if ( memberAnnotationMap().containsKey( JpaDotNames.ONE_TO_ONE ) ) {
			// todo : if there is a @JoinTable, treat this as a many-to-one
			return ToOneNature.ONE_TO_ONE;
		}

		throw getContext().makeMappingException(
				"Could not determine ToOneNature of TO_ONE attribute : " +
						getBackingMember().attributeName() +
						"[" + getBackingMember().toString() + "]"
		);
	}

	public ToOneNature getToOneNature() {
		return toOneNature;
	}

	public String getTargetTypeName() {
		return target;
	}

	@Override
	public FetchCharacteristics getFetchCharacteristics() {
		return fetchCharacteristics;
	}

	@Override
	public boolean isLazy() {
		return isLazy;
	}

	@Override
	public String getMappedByAttributeName() {
		return mappedByAttributeName;
	}

	public void setMappedByAttributeName(String mappedByAttributeName) {
		this.mappedByAttributeName = mappedByAttributeName;
	}

	@Override
	public boolean isInverse() {
		return isInverse;
	}

	@Override
	public Set<CascadeType> getJpaCascadeTypes() {
		return jpaCascadeTypes;
	}

	@Override
	public Set<org.hibernate.annotations.CascadeType> getHibernateCascadeTypes() {
		return hibernateCascadeTypes;
	}

	@Override
	public boolean isOrphanRemoval() {
		return isOrphanRemoval;
	}

	@Override
	public boolean isIgnoreNotFound() {
		return ignoreNotFound;
	}

	public boolean hasPrimaryKeyJoinColumn() {
		return hasPrimaryKeyJoinColumn;
	}

	@Override
	public boolean isOptional() {
		return isOptional;
	}

	@Override
	public boolean isInsertable() {
		// todo : this is configurable right?
		return true;
	}

	@Override
	public boolean isUpdatable() {
		// todo : this is configurable right?
		return true;
	}

	@Override
	public boolean isIncludeInOptimisticLocking() {
		if ( hasOptimisticLockAnnotation() ) {
			return super.isIncludeInOptimisticLocking();
		}
		else {
			// uh, saywhatnow?
			return isInsertable();
		}
	}

	@Override
	public PropertyGeneration getPropertyGeneration() {
		return PropertyGeneration.NEVER;
	}

	public AnnotationInstance getJoinTableAnnotation() {
		return joinTableAnnotation;
	}

	public ArrayList<Column> getJoinColumnValues() {
		return joinColumnValues;
	}

	public ArrayList<Column> getInverseJoinColumnValues() {
		return inverseJoinColumnValues;
	}

	public AnnotationInstance getMapsIdAnnotation() {
		return mapsIdAnnotation;
	}

	public enum ToOneNature {
		ONE_TO_ONE( JpaDotNames.ONE_TO_ONE ),
		MANY_TO_ONE (JpaDotNames.MANY_TO_ONE );

		private final DotName annotationDotName;

		ToOneNature(DotName annotationDotName) {
			this.annotationDotName = annotationDotName;
		}

		public DotName annotationDotName() {
			return annotationDotName;
		}
	}
}