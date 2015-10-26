/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.spi.PluralAttributeIndexNature;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeIndexDetailsSequential implements PluralAttributeIndexDetails {
	private final PluralAttribute pluralAttribute;

	private final Column orderColumn;
	private final int base;

	public PluralAttributeIndexDetailsSequential(PluralAttribute pluralAttribute, MemberDescriptor backingMember) {
		this.pluralAttribute = pluralAttribute;

		this.orderColumn = determineOrderColumn();
		this.base = determineIndexBase();
	}

	private Column determineOrderColumn() {
		final AnnotationInstance orderColumnAnnotation = pluralAttribute.memberAnnotationMap().get( JpaDotNames.ORDER_COLUMN );
		if ( orderColumnAnnotation == null ) {
			return null;
		}
		return new Column( orderColumnAnnotation );
	}

	private int determineIndexBase() {
		final AnnotationInstance listIndexBase = pluralAttribute.memberAnnotationMap().get(
				HibernateDotNames.LIST_INDEX_BASE
		);
		if ( listIndexBase == null ) {
			return 0;
		}

		final AnnotationValue baseValue = listIndexBase.value();
		if ( baseValue == null ) {
			return 0;
		}

		return baseValue.asInt();
	}

	public Column getOrderColumn() {
		return orderColumn;
	}

	public int getBase() {
		return base;
	}

	@Override
	public ClassInfo getJavaType() {
		return null;
	}

	@Override
	public PluralAttributeIndexNature getIndexNature() {
		return PluralAttributeIndexNature.SEQUENTIAL;
	}
}
