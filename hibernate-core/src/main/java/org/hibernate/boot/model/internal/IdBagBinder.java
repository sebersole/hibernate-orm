/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.hibernate.MappingException;
import org.hibernate.annotations.CollectionId;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.PropertyData;
import org.hibernate.boot.spi.SecondPass;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.IdentifierBag;
import org.hibernate.mapping.IdentifierCollection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Table;
import org.hibernate.models.spi.AnnotationUsage;
import org.hibernate.resource.beans.spi.ManagedBean;
import org.hibernate.usertype.UserCollectionType;

import static org.hibernate.boot.model.internal.GeneratorBinder.makeIdGenerator;

/**
 * A {@link CollectionBinder} for {@link org.hibernate.collection.spi.PersistentIdentifierBag id bags}
 * whose mapping model type is {@link org.hibernate.mapping.IdentifierBag}.
 *
 * @author Emmanuel Bernard
 */
public class IdBagBinder extends BagBinder {

	public IdBagBinder(
			Supplier<ManagedBean<? extends UserCollectionType>> customTypeBeanResolver,
			MetadataBuildingContext buildingContext) {
		super( customTypeBeanResolver, buildingContext );
	}

	@Override
	protected Collection createCollection(PersistentClass owner) {
		return new IdentifierBag( getCustomTypeBeanResolver(), owner, getBuildingContext() );
	}

	@Override
	protected boolean bindStarToManySecondPass(Map<String, PersistentClass> persistentClasses) {
		boolean result = super.bindStarToManySecondPass( persistentClasses );

		final AnnotationUsage<CollectionId> collectionIdAnn = property.getAnnotationUsage( CollectionId.class );
		if ( collectionIdAnn == null ) {
			throw new MappingException( "idbag mapping missing '@CollectionId' annotation" );
		}

		final PropertyData propertyData = new WrappedInferredData(
				new PropertyInferredData(
						null,
						declaringClass,
						property,
						//default access should not be useful
						null,
						buildingContext
				),
				"id"
		);

		final AnnotatedColumns idColumns = AnnotatedColumn.buildColumnsFromAnnotations(
				List.of( collectionIdAnn.getNestedUsage( "column" ) ),
//				null,
				null,
				Nullability.FORCED_NOT_NULL,
				propertyHolder,
				propertyData,
				Collections.emptyMap(),
				buildingContext
		);

		//we need to make sure all id columns must be not-null.
		for ( AnnotatedColumn idColumn : idColumns.getColumns() ) {
			idColumn.setNullable( false );
		}

		final BasicValueBinder valueBinder =
				new BasicValueBinder( BasicValueBinder.Kind.COLLECTION_ID, buildingContext );

		final Table table = collection.getCollectionTable();
		valueBinder.setTable( table );
		valueBinder.setColumns( idColumns );

		valueBinder.setType(
				property,
				getElementType(),
				null,
				null
		);

		final BasicValue id = valueBinder.make();
		( (IdentifierCollection) collection ).setIdentifier( id );

		final String namedGenerator = collectionIdAnn.getString( "generator" );

		switch (namedGenerator) {
			case "identity": {
				throw new MappingException("IDENTITY generation not supported for CollectionId");
			}
			case "assigned": {
				throw new MappingException("Assigned generation not supported for CollectionId");
			}
			case "native": {
				throw new MappingException("Native generation not supported for CollectionId");
			}
		}

		final String generatorName;
		final String generatorType;

		if ( "sequence".equals( namedGenerator ) ) {
			generatorType = namedGenerator;
			generatorName = "";
		}
		else if ( "increment".equals( namedGenerator ) ) {
			generatorType = namedGenerator;
			generatorName = "";
		}
		else {
			generatorType = namedGenerator;
			generatorName = namedGenerator;
		}

		id.setIdentifierGeneratorStrategy( generatorType );

		if ( buildingContext.getBootstrapContext().getJpaCompliance().isGlobalGeneratorScopeEnabled() ) {
			SecondPass secondPass = new IdGeneratorResolverSecondPass(
					id,
					property,
					generatorType,
					generatorName,
					getBuildingContext()
			);
			buildingContext.getMetadataCollector().addSecondPass( secondPass );
		}
		else {
			makeIdGenerator(
					id,
					property,
					generatorType,
					generatorName,
					getBuildingContext(),
					localGenerators
			);
		}
		return result;
	}
}
