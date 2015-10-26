/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.spi.PluralAttributeIndexNature;

import org.jboss.jandex.ClassInfo;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexDetailsMapKeyEntityAttribute extends AbstractPluralAttributeIndexDetailsMapKey {
	private final String referencedAttributeName;

	public PluralAttributeIndexDetailsMapKeyEntityAttribute(
			PluralAttribute pluralAttribute,
			MemberDescriptor backingMember,
			ClassInfo resolvedMapKeyType,
			String referencedAttributeName) {
		super( pluralAttribute, backingMember, resolvedMapKeyType );
		this.referencedAttributeName = referencedAttributeName;
	}

	public String getReferencedAttributeName() {
		return referencedAttributeName;
	}

	@Override
	public PluralAttributeIndexNature getIndexNature() {
		// we don't know until we can resolve the referenced entity attribute
		return null;
	}
}
