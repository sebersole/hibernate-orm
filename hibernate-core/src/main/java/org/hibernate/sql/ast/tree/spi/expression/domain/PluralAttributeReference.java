/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.expression.domain;

import org.hibernate.metamodel.model.domain.spi.PluralPersistentAttribute;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeReference extends AbstractNavigableReference {
	/**
	 * Ctor for a collection domain result
	 */
	public PluralAttributeReference(
			PluralPersistentAttribute referencedAttribute,
			ColumnReferenceQualifier columnReferenceQualifier,
			NavigablePath navigablePath) {
		super( null, referencedAttribute, navigablePath, columnReferenceQualifier );
	}

	/**
	 * Ctor for fetch reference.  Not that either (but not both)
	 * `containerReference` or `valuesQualifier` may be null
	 */
	public PluralAttributeReference(
			NavigableContainerReference containerReference,
			PluralPersistentAttribute referencedAttribute,
			ColumnReferenceQualifier valuesQualifier,
			NavigablePath navigablePath) {
		super( containerReference, referencedAttribute, navigablePath, valuesQualifier );
	}

	@Override
	public PluralPersistentAttribute getNavigable() {
		return (PluralPersistentAttribute) super.getNavigable();
	}

	public ColumnReferenceQualifier getContainerQualifier() {
		if ( getNavigableContainerReference() == null ) {
			return getColumnReferenceQualifier();
		}

		return super.getColumnReferenceQualifier();
	}
}
