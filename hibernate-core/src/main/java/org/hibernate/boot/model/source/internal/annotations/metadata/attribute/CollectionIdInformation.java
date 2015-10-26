/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.List;
import java.util.Map;

import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.source.internal.annotations.HibernateTypeSourceImpl;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.model.source.spi.HibernateTypeSource;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.CollectionHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

/**
 * Models information from a CollectionId annotation
 *
 * @author Steve Ebersole
 */
public class CollectionIdInformation {

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

		return new CollectionIdInformation(
				idColumns,
				generator,
				typeName,
				AnnotationBindingHelper.extractTypeParameters( type, context )
		);

	}

	private final List<Column> columns;
	private final IdentifierGeneratorDefinition generatorDefinition;
	private final HibernateTypeSourceImpl hibernateTypeSource;

	private CollectionIdInformation(
			List<Column> columns,
			IdentifierGeneratorDefinition generatorDefinition,
			String explicitTypeName,
			Map<String, String> typeParameters) {
		this.columns = columns;
		this.generatorDefinition = generatorDefinition;
		this.hibernateTypeSource = new HibernateTypeSourceImpl( explicitTypeName, typeParameters, null );
	}

	public List<Column> getColumns() {
		return columns;
	}

	public IdentifierGeneratorDefinition getGeneratorDefinition() {
		return generatorDefinition;
	}

	public HibernateTypeSource getExplicitTypeSource() {
		return hibernateTypeSource;
	}
}
