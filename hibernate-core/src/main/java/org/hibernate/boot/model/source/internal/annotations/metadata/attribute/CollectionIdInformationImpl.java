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

import java.util.List;
import java.util.Map;

import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.source.internal.annotations.HibernateTypeSource;
import org.hibernate.boot.model.source.internal.annotations.impl.HibernateTypeSourceImpl;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.CollectionHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

/**
 * @author Steve Ebersole
 */
public class CollectionIdInformationImpl implements CollectionIdInformation {

	public static CollectionIdInformation make(PluralAttribute pluralAttribute) {
		final AnnotationInstance collectionId = pluralAttribute.memberAnnotationMap().get( HibernateDotNames.COLLECTION_ID );
		if ( collectionId == null ) {
			return null;
		}

		final EntityBindingContext context = pluralAttribute.getContext();

		final IdentifierGeneratorDefinition generator = new IdentifierGeneratorDefinition(
				null,
				collectionId.value( "generator" ).asString(),
				null
		);

		final AnnotationInstance type = context.getTypedValueExtractor( AnnotationInstance.class ).extract(
				collectionId,
				"type"
		);
		final AnnotationValue typeType = type.value( "type" );
		final String typeName = typeType == null ? null : typeType.asString();
		if ( StringHelper.isEmpty( typeName ) ) {
			throw context.makeMappingException(
					"Plural attribute [" + pluralAttribute.getBackingMember()
							.toString() + "] specified @CollectionId.type incorrectly, " +
							"type name was missing"
			);
		}
		final AnnotationInstance[] columnAnnotations = context.getTypedValueExtractor(
				AnnotationInstance[].class
		).extract(
				collectionId,
				"columns"
		);
		final List<Column> idColumns = CollectionHelper.arrayList( columnAnnotations.length );
		for ( AnnotationInstance columnAnnotation : columnAnnotations ) {
			idColumns.add( new Column( columnAnnotation ) );
		}

		return new CollectionIdInformationImpl(
				idColumns,
				generator,
				typeName,
				AnnotationBindingHelper.extractTypeParameters( type, context )
		);

	}

	private final List<Column> columns;
	private final IdentifierGeneratorDefinition generatorDefinition;
	private final HibernateTypeSourceImpl hibernateTypeSource;

	private CollectionIdInformationImpl(
			List<Column> columns,
			IdentifierGeneratorDefinition generatorDefinition,
			String explicitTypeName,
			Map<String, String> typeParameters) {
		this.columns = columns;
		this.generatorDefinition = generatorDefinition;
		this.hibernateTypeSource = new HibernateTypeSourceImpl( explicitTypeName, typeParameters, null );
	}

	@Override
	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public IdentifierGeneratorDefinition getGeneratorDefinition() {
		return generatorDefinition;
	}

	@Override
	public HibernateTypeSource getExplicitTypeSource() {
		return hibernateTypeSource;
	}
}
