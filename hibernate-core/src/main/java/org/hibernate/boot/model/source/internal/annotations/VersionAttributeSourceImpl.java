/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.spi.IdentifierGenerationInformation;
import org.hibernate.boot.model.source.spi.VersionAttributeSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.BasicAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.OverrideAndConverterCollector;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.boot.spi.AttributeConverterDescriptor;

/**
 * @author Steve Ebersole
 */
public class VersionAttributeSourceImpl extends SingularAttributeSourceBasicImpl implements VersionAttributeSource {

	public VersionAttributeSourceImpl(
			BasicAttribute attribute,
			OverrideAndConverterCollector overrideAndConverterCollector) {
		super( attribute, overrideAndConverterCollector );
	}

	public String getUnsavedValue() {
		return null;
	}

	@Override
	public NaturalIdMutability getNaturalIdMutability() {
		// version cannot be part of the natural id (makes no sense)
		return NaturalIdMutability.NOT_NATURAL_ID;
	}

	public boolean isCollectionElement() {
		return false;
	}

	public EntityBindingContext getBuildingContext() {
		return getAnnotatedAttribute().getContext();
	}

	@Override
	public IdentifierGenerationInformation getIdentifierGenerationInformation() {
		return null;
	}

	@Override
	public AttributeConverterDescriptor resolveAttributeConverterDescriptor() {
		return null;
	}
}
