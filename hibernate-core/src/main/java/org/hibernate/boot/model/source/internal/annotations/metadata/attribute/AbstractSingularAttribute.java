/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.Map;
import javax.persistence.AccessType;

import org.hibernate.AnnotationException;
import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.impl.ConvertConversionInfo;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.ConverterAndOverridesHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public abstract class AbstractSingularAttribute
		extends AbstractPersistentAttribute
		implements SingularAttribute {
	private static final Logger log = Logger.getLogger( AbstractSingularAttribute.class );

	protected AbstractSingularAttribute(
			ManagedTypeMetadata container,
			String attributeName,
			AttributePath attributePath,
			AttributeRole attributeRole,
			MemberDescriptor backingMember,
			AttributeNature attributeNature,
			AccessType accessType,
			String accessorStrategy) {
		super( container, attributeName, attributePath, attributeRole, backingMember,
			   attributeNature, accessType, accessorStrategy );

		final Map<DotName,AnnotationInstance> memberAnnotions = container.getLocalBindingContext().getMemberAnnotationInstances( backingMember );
		if ( memberAnnotions.containsKey( HibernateDotNames.IMMUTABLE ) ) {
			throw new AnnotationException( "@Immutable can be used on entities or collections, not "
					+ attributeRole.getFullPath() );
		}
		
		ConverterAndOverridesHelper.processConverters( this );
		ConverterAndOverridesHelper.processAttributeOverrides( this );
		ConverterAndOverridesHelper.processAssociationOverrides( this );
	}

	protected ConvertConversionInfo validateConversionInfo(ConvertConversionInfo conversionInfo) {
		// NOTE we cant really throw exceptions here atm because we do not know if
		// 		the converter was explicitly requested or if an auto-apply converter
		//		was returned.  So we simply log a warning and circumvent the conversion

		// todo : regarding ^^, on second thought its likely better if this scaffolding just support locally defined converters
		//		then it is ok to throw then exceptions
		//		the idea being that binder would apply auto apply converters if needed

		// disabled is always allowed
		if ( !conversionInfo.isConversionEnabled() ) {
			return conversionInfo;
		}

		// otherwise use of the converter is ok, as long as...
		//		1) the attribute is not an id
		if ( isId() ) {
			log.warnf(
					"AttributeConverter [%s] cannot be applied to given attribute [%s] as it is an Id (section 3.8)",
					conversionInfo.getConverterTypeDescriptor().name(),
					getBackingMember().toString()
			);
			return null;
		}

		//		2) the attribute is not a version
		if ( isVersion() ) {
			log.warnf(
					"AttributeConverter [%s] cannot be applied to given attribute [%s] as it is a Version (section 3.8)",
					conversionInfo.getConverterTypeDescriptor().name(),
					getBackingMember().toString()
			);
			return null;
		}

		//		3) the attribute is not an association
		if ( getAttributeNature() == AttributeNature.TO_ONE ) {
			log.warnf(
					"AttributeConverter [%s] cannot be applied to given attribute [%s] as it is an association (section 3.8)",
					conversionInfo.getConverterTypeDescriptor().name(),
					getBackingMember().toString()
			);
			return null;
		}

		//		4) the attribute is not an embedded
		if ( getAttributeNature() == AttributeNature.EMBEDDED ) {
			log.warnf(
					"AttributeConverter [%s] cannot be applied to given attribute [%s] as it is an Embeddable (section 3.8)",
					conversionInfo.getConverterTypeDescriptor().name(),
					getBackingMember().toString()
			);
			return null;
		}

		//		5) the attribute cannot have explicit "conversion" annotations such as
		//			@Temporal or @Enumerated
		if ( memberAnnotationMap().containsKey( JpaDotNames.TEMPORAL ) ) {
			log.warnf(
					"AttributeConverter [%s] cannot be applied to given attribute [%s] as it is annotated @Temporal (section 3.8)",
					conversionInfo.getConverterTypeDescriptor().name(),
					getBackingMember().toString()
			);
			return null;
		}
		if ( memberAnnotationMap().containsKey( JpaDotNames.ENUMERATED ) ) {
			log.warnf(
					"AttributeConverter [%s] cannot be applied to given attribute [%s] as it is annotated @Enumerated (section 3.8)",
					conversionInfo.getConverterTypeDescriptor().name(),
					getBackingMember().toString()
			);
			return null;
		}


		return conversionInfo;
	}

	@Override
	public ConvertConversionInfo getConversionInfo() {
		return getContainer().locateConversionInfo( getPath() );
	}

	@Override
	public boolean isId() {
		return super.isId();
	}

	@Override
	public boolean isVersion() {
		return super.isVersion();
	}

	@Override
	public NaturalIdMutability getNaturalIdMutability() {
		return super.getNaturalIdMutability();
	}
}
