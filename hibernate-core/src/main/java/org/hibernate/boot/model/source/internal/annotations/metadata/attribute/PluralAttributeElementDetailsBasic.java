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
public class PluralAttributeElementDetailsBasic implements PluralAttributeElementDetails {
	private final ClassInfo javaType;

	public PluralAttributeElementDetailsBasic(
			PluralAttribute pluralAttribute,
			ClassInfo inferredElementType) {
		this.javaType = determineJavaType( pluralAttribute, inferredElementType );
	}

	private static ClassInfo determineJavaType(PluralAttribute pluralAttribute, ClassInfo elementType) {
		if ( elementType != null ) {
			return elementType;
		}

		final Map<DotName,AnnotationInstance> memberAnnotationMap = pluralAttribute.memberAnnotationMap();
		final AnnotationInstance elementCollectionAnnotationInstance = memberAnnotationMap.get( JpaDotNames.ELEMENT_COLLECTION );
		if ( elementCollectionAnnotationInstance == null ) {
			throw pluralAttribute.getContext().makeMappingException(
					"Could not determine element type information for plural attribute ["
							+ pluralAttribute.getBackingMember().toString()
							+ "]; could not locate @ElementCollection annotation"
			);
		}

		DotName dotName = null;
		final AnnotationValue targetClassValue = elementCollectionAnnotationInstance.value( "targetClass" );
		if ( targetClassValue == null ) {
			// the collection is not parameterized and @ElementCollection#targetClass was not specified...
			final AnnotationInstance typeAnnotation = memberAnnotationMap.get( HibernateDotNames.TYPE );
			if ( typeAnnotation != null ) {
				// The user did specify @Type so we will be able to determine the element type,
				// but not until after we construct the Type
				dotName = DotName.createSimple( typeAnnotation.value( "type" ).asString() );
			}
			else {
				throw pluralAttribute.getContext().makeMappingException(
						"Could not determine element type information for plural attribute ["
								+ pluralAttribute.getBackingMember().toString()
								+ "]; Either specify targetClass in @ElementCollection or provide @Type"
				);
			}
		}
		else {
			dotName = targetClassValue.asClass().name();
		}

		return pluralAttribute.getContext().getJandexIndex().getClassByName( dotName );
	}

	@Override
	public ClassInfo getJavaType() {
		return javaType;
	}

	@Override
	public PluralAttributeElementNature getElementNature() {
		return PluralAttributeElementNature.BASIC;
	}

}
