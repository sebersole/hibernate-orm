/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PluralAttribute;

/**
 * @author Steve Ebersole
 * @author Strong Liu
 */
public abstract class AbstractPluralAttributeElementSourceImpl {
	private final PluralAttributeSourceImpl pluralAttributeSource;
	private final PluralAttribute pluralAttribute;

	public AbstractPluralAttributeElementSourceImpl(PluralAttributeSourceImpl pluralAttributeSource) {
		this.pluralAttributeSource = pluralAttributeSource;
		this.pluralAttribute = pluralAttributeSource.getPluralAttribute();
	}

	protected PluralAttributeSourceImpl getPluralAttributeSource() {
		return pluralAttributeSource;
	}

	protected PluralAttribute getPluralAttribute() {
		return pluralAttribute;
	}
}
