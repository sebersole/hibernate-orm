/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.spi.PluralAttributeIndexNature;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexDetailsMapKeyBasic extends AbstractPluralAttributeIndexDetailsMapKey {
	private final AnnotationInstance mapKeyColumnAnnotation;

	public PluralAttributeIndexDetailsMapKeyBasic(
			PluralAttribute pluralAttribute,
			MemberDescriptor backingMember,
			ClassInfo resolvedMapKeyType,
			AnnotationInstance mapKeyColumnAnnotation) {
		super( pluralAttribute, backingMember, resolvedMapKeyType );
		this.mapKeyColumnAnnotation = mapKeyColumnAnnotation;
	}

	public AnnotationInstance getMapKeyColumnAnnotation() {
		return mapKeyColumnAnnotation;
	}

	@Override
	public PluralAttributeIndexNature getIndexNature() {
		return PluralAttributeIndexNature.BASIC;
	}
}
