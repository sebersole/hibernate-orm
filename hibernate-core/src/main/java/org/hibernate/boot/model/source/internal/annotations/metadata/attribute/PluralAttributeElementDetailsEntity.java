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

import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.source.spi.PluralAttributeElementNature;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeElementDetailsEntity implements PluralAttributeElementDetails {
	private final ClassInfo javaType;
	private final PluralAttributeElementNature elementNature;

	public PluralAttributeElementDetailsEntity(
			PluralAttribute pluralAttribute,
			ClassInfo inferredElementType) {
		this.elementNature = decodeElementNature( pluralAttribute );
		this.javaType = determineJavaType( pluralAttribute, inferredElementType );

		if ( this.javaType == null ) {
			throw pluralAttribute.getContext().makeMappingException(
					"Could not determine element type information for plural attribute : "
							+ pluralAttribute.getBackingMember().toString()
			);
		}
	}

	private PluralAttributeElementNature decodeElementNature(PluralAttribute pluralAttribute) {
		final Map<DotName,AnnotationInstance> memberAnnotationMap = pluralAttribute.memberAnnotationMap();

		if ( memberAnnotationMap.containsKey( JpaDotNames.ONE_TO_MANY ) ) {
			return PluralAttributeElementNature.ONE_TO_MANY;
		}

		if ( memberAnnotationMap.containsKey( JpaDotNames.MANY_TO_MANY ) ) {
			return PluralAttributeElementNature.MANY_TO_MANY;
		}

		throw pluralAttribute.getContext().makeMappingException(
				"Unable to determine plural attribute element nature (ONE_TO_MANY versus MANY_TO_MANY) : " + pluralAttribute.getBackingMember().toString()
		);
	}

	private ClassInfo determineJavaType(
			PluralAttribute pluralAttribute,
			ClassInfo inferredElementType) {
		final Map<DotName,AnnotationInstance> memberAnnotationMap = pluralAttribute.memberAnnotationMap();
		final AnnotationInstance targetAnnotation = memberAnnotationMap.get( HibernateDotNames.TARGET );
		if ( targetAnnotation != null ) {
			final AnnotationValue targetValue = targetAnnotation.value();
			if ( targetValue != null ) {
				return pluralAttribute.getContext().getJandexIndex().getClassByName(
						targetValue.asClass().name()
				);
			}
		}

		final AnnotationInstance oneToManyAnnotation = memberAnnotationMap.get( JpaDotNames.ONE_TO_MANY );
		if ( oneToManyAnnotation != null ) {
			final AnnotationValue targetClassValue = oneToManyAnnotation.value( "targetEntity" );
			if ( targetClassValue != null ) {
				return pluralAttribute.getContext().getJandexIndex().getClassByName(
						targetClassValue.asClass().name()
				);
			}
		}

		final AnnotationInstance manyToManyAnnotation = memberAnnotationMap.get( JpaDotNames.MANY_TO_MANY );
		if ( manyToManyAnnotation != null ) {
			final AnnotationValue targetClassValue = manyToManyAnnotation.value( "targetEntity" );
			if ( targetClassValue != null ) {
				return pluralAttribute.getContext().getJandexIndex().getClassByName(
						targetClassValue.asClass().name()
				);
			}
		}

		return inferredElementType;
	}

	@Override
	public ClassInfo getJavaType() {
		return javaType;
	}

	@Override
	public PluralAttributeElementNature getElementNature() {
		return elementNature;
	}
}
