/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.Map;

import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
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
