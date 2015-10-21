/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
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
package org.hibernate.boot.model.source.internal.annotations.impl;

import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.source.internal.annotations.MultiTenancySource;
import org.hibernate.boot.model.source.internal.annotations.RelationalValueSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.Column;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.FormulaValue;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;

import org.jboss.jandex.AnnotationInstance;

/**
 * @author Steve Ebersole
 */
public class MutliTenancySourceImpl implements MultiTenancySource {
	private final RelationalValueSource relationalValueSource;
	private final boolean shared;
	private final boolean bindAsParameter;

	public MutliTenancySourceImpl(EntityTypeMetadata entityTypeMetadata) {
		final AnnotationInstance columnAnnotation = AnnotationBindingHelper.findTypeAnnotation(
				HibernateDotNames.TENANT_COLUMN,
				entityTypeMetadata
		);
		if ( columnAnnotation != null ) {
			final Column column = new Column(  null );
			column.setName(
					entityTypeMetadata.getLocalBindingContext().getTypedValueExtractor( String.class ).extract(
							columnAnnotation,
							"column"
					)
			);
			column.setTable( null ); // primary table
			column.setLength(
					entityTypeMetadata.getLocalBindingContext().getTypedValueExtractor( int.class ).extract(
							columnAnnotation,
							"length"
					)
			);
			column.setPrecision(
					entityTypeMetadata.getLocalBindingContext().getTypedValueExtractor( int.class ).extract(
							columnAnnotation,
							"precision"
					)
			);
			column.setScale(
					entityTypeMetadata.getLocalBindingContext().getTypedValueExtractor( int.class ).extract(
							columnAnnotation,
							"scale"
					)
			);
			// todo : type
			relationalValueSource = new ColumnSourceImpl( column );
		}
		else {
			final AnnotationInstance formulaAnnotation = AnnotationBindingHelper.findTypeAnnotation(
					HibernateDotNames.TENANT_FORMULA,
					entityTypeMetadata
			);
			if ( formulaAnnotation != null ) {
				relationalValueSource = new DerivedValueSourceImpl(
						new FormulaValue(
								null, // primary table
								entityTypeMetadata.getLocalBindingContext().getTypedValueExtractor( String.class ).extract(
										formulaAnnotation,
										"value"
								)
						)
				);
			}
			else {
				relationalValueSource = null;
			}
		}

		final AnnotationInstance multiTenantAnnotation = AnnotationBindingHelper.findTypeAnnotation(
				HibernateDotNames.MULTI_TENANT,
				entityTypeMetadata
		);
		if ( multiTenantAnnotation == null ) {
			shared = true;
			bindAsParameter = true;
		}
		else {
			shared = entityTypeMetadata.getLocalBindingContext().getTypedValueExtractor( boolean.class ).extract(
					multiTenantAnnotation,
					"shared"
			);
			bindAsParameter = entityTypeMetadata.getLocalBindingContext().getTypedValueExtractor( boolean.class ).extract(
					multiTenantAnnotation,
					"useParameterBinding"
			);
		}
	}

	@Override
	public RelationalValueSource getRelationalValueSource() {
		return relationalValueSource;
	}

	@Override
	public boolean isShared() {
		return shared;
	}

	@Override
	public boolean bindAsParameter() {
		return bindAsParameter;
	}
}
