/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;


import org.hibernate.boot.model.MemberDescriptor;

import org.jboss.jandex.ClassInfo;

/**
 * PluralAttributeIndexDetails implementation for describing the key of a Map
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPluralAttributeIndexDetailsMapKey implements PluralAttributeIndexDetails {
	private final PluralAttribute pluralAttribute;
	private final ClassInfo resolvedMapKeyType;

	public AbstractPluralAttributeIndexDetailsMapKey(
			PluralAttribute pluralAttribute,
			MemberDescriptor backingMember,
			ClassInfo resolvedMapKeyType) {
		this.pluralAttribute = pluralAttribute;
		this.resolvedMapKeyType = resolvedMapKeyType;
	}

	@Override
	public ClassInfo getJavaType() {
		return resolvedMapKeyType;
	}
}
