/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.impl;

import java.util.Locale;

import org.hibernate.boot.model.source.internal.annotations.EmbeddableSource;
import org.hibernate.boot.model.source.internal.annotations.HibernateTypeSource;
import org.hibernate.boot.model.source.internal.annotations.SingularAttributeSourceEmbedded;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.EmbeddedAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.util.AttributeSourceBuildingHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.boot.model.source.spi.SingularAttributeNature;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.tuple.GenerationTiming;

/**
 * Annotation backed implementation of {@code EmbeddedAttributeSource}.
 *
 * @author Steve Ebersole
 * @author Hardy Ferentschik
 * @author Brett Meyer
 */
public class SingularAttributeSourceEmbeddedImpl implements SingularAttributeSourceEmbedded {
	private final EmbeddedAttribute attribute;

	private final EmbeddableSource embeddableSource;

	public SingularAttributeSourceEmbeddedImpl(
			EmbeddedAttribute attribute,
			boolean partOfIdentifier,
			boolean partOfPersistentCollection) {

		final AttributeSourceBuildingHelper.AttributeBuilder attributeBuilder;
		if ( partOfIdentifier ) {
			attributeBuilder = AttributeSourceBuildingHelper.IdentifierPathAttributeBuilder.INSTANCE;
		}
		else if ( partOfPersistentCollection ) {
			attributeBuilder = AttributeSourceBuildingHelper.PluralAttributesDisallowedAttributeBuilder.INSTANCE;
		}
		else {
			attributeBuilder = AttributeSourceBuildingHelper.StandardAttributeBuilder.INSTANCE;
		}

		this.embeddableSource = new EmbeddableSourceImpl(
				attribute.getEmbeddableTypeMetadata(),
				attributeBuilder
		);

		this.attribute = attribute;
	}

	@Override
	public EmbeddableSource getEmbeddableSource() {
		return embeddableSource;
	}

	@Override
	public PersistentAttribute getAnnotatedAttribute() {
		return attribute;
	}

	@Override
	public boolean isVirtualAttribute() {
		return false;
	}

	@Override
	public SingularAttributeNature getSingularAttributeNature() {
		return SingularAttributeNature.COMPOSITE;
	}

	@Override
	public GenerationTiming getGenerationTiming() {
		return null;
	}

	@Override
	public Boolean isInsertable() {
		return null;
	}

	@Override
	public Boolean isUpdatable() {
		return null;
	}

	@Override
	public boolean isBytecodeLazy() {
		return false;
	}

	@Override
	public boolean isSingular() {
		return true;
	}

	@Override
	public String getName() {
		return attribute.getName();
	}

	@Override
	public AttributePath getAttributePath() {
		return attribute.getPath();
	}

	@Override
	public AttributeRole getAttributeRole() {
		return attribute.getRole();
	}


	@Override
	public String getPropertyAccessorName() {
		// todo : would really rather have binder decipher this...
		return StringHelper.isEmpty( attribute.getAccessorStrategy() )
				? attribute.getAccessType().name().toLowerCase( Locale.ENGLISH )
				: attribute.getAccessorStrategy();
	}

	@Override
	public HibernateTypeSource getTypeInformation() {
		// probably need to check for @Target in EmbeddableTypeMetadata (HF)
		return null;
	}

	@Override
	public NaturalIdMutability getNaturalIdMutability() {
		return attribute.getNaturalIdMutability();
	}

	@Override
	public boolean isIncludedInOptimisticLocking() {
		return true;
	}

	@Override
	public String toString() {
		return "EmbeddedAttributeSourceImpl{role=" + attribute.getRole().getFullPath()
				+ ", embeddable=" + embeddableSource.getTypeDescriptor().getName().toString() + "}";
	}
}
