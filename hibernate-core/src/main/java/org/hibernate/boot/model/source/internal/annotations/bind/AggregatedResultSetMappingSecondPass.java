/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.bind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.persistence.SqlResultSetMapping;

import org.hibernate.LockMode;
import org.hibernate.MappingException;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.cfg.QuerySecondPass;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryConstructorReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryRootReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryScalarReturn;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.ToOne;
import org.hibernate.mapping.Value;
import org.hibernate.type.Type;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.logging.Logger;

/**
 * SecondPass implementation that process all {@link SqlResultSetMapping} annotations at one go
 *
 * @author Steve Ebersole
 */
public class AggregatedResultSetMappingSecondPass implements QuerySecondPass {
	private static final Logger log = Logger.getLogger( AggregatedResultSetMappingSecondPass.class );

	private final RootAnnotationBindingContext bindingContext;

	public AggregatedResultSetMappingSecondPass(RootAnnotationBindingContext bindingContext) {
		this.bindingContext = bindingContext;
	}

	@Override
	public void doSecondPass(Map persistentClasses) throws MappingException {
		final List<AnnotationInstance> mappingAnnotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				JpaDotNames.SQL_RESULT_SET_MAPPING,
				JpaDotNames.SQL_RESULT_SET_MAPPINGS
		);
		for ( AnnotationInstance mappingAnnotation : mappingAnnotations ) {
			bindResultSetMappingDefinition( mappingAnnotation );
		}
	}

	private void bindResultSetMappingDefinition(AnnotationInstance mappingAnnotation) {
		final String name = bindingContext.stringExtractor.extract( mappingAnnotation, "name" );
		log.debugf( "Binding result set mapping: %s", name );

		final ResultSetMappingDefinition definition = new ResultSetMappingDefinition( name );

		bindEntityResults( definition, mappingAnnotation );
		bindColumnResults( definition, mappingAnnotation );
		bindConstructorResults( definition, mappingAnnotation );

//		if ( isDefault ) {
			bindingContext.getMetadataCollector().addDefaultResultSetMapping( definition );
//		}
//		else {
//			bindingContext.getMetadataCollector().addResultSetMapping( definition );
//		}
	}

	private void bindColumnResults(ResultSetMappingDefinition definition, AnnotationInstance mappingAnnotation) {
		for ( AnnotationInstance columnAnnotation :
				bindingContext.nestedArrayExtractor.extract( mappingAnnotation, "columns" ) ) {
			definition.addQueryReturn(
					makeNativeSQLQueryScalarReturn( columnAnnotation )
			);
		}
	}

	private NativeSQLQueryScalarReturn makeNativeSQLQueryScalarReturn(AnnotationInstance columnAnnotation) {
		final String columnName = normalizeColumnQuoting( bindingContext.stringExtractor.extract( columnAnnotation, "name" ) );
		final org.jboss.jandex.Type javaType = bindingContext.classTypeExtractor.extract( columnAnnotation, "type" );
		final Type mappingType = javaType == null
				? null
				: bindingContext.getMetadataCollector().getTypeResolver().heuristicType( javaType.name().toString() );
		return new NativeSQLQueryScalarReturn( columnName, mappingType );
	}

	private void bindConstructorResults(ResultSetMappingDefinition definition, AnnotationInstance mappingAnnotation) {
		for ( AnnotationInstance resultClassAnnotation :
				bindingContext.nestedArrayExtractor.extract( mappingAnnotation, "classes" ) ) {
			final List<NativeSQLQueryScalarReturn> columnReturns = new ArrayList<NativeSQLQueryScalarReturn>();

			for ( AnnotationInstance columnAnnotation :
					bindingContext.nestedArrayExtractor.extract( resultClassAnnotation, "columns" ) ) {
				columnReturns.add( makeNativeSQLQueryScalarReturn( columnAnnotation ) );
			}

			final String targetClassName = bindingContext.classTypeExtractor.extract( resultClassAnnotation, "targetClass" ).name().toString();
			final Class targetClass = bindingContext.getBuildingOptions().getServiceRegistry().getService(
					ClassLoaderService.class ).classForName( targetClassName );
			definition.addQueryReturn(
					new NativeSQLQueryConstructorReturn( targetClass, columnReturns )
			);
		}
	}

	private void bindEntityResults(ResultSetMappingDefinition definition, AnnotationInstance mappingAnnotation) {
		int entityAliasIndex = 0;

		final AnnotationInstance[] entityReturnAnnotations = bindingContext.nestedArrayExtractor.extract( mappingAnnotation, "entities" );
		for ( AnnotationInstance entityResultAnnotation : entityReturnAnnotations ) {
			final String entityName = bindingContext.classTypeExtractor.extract( entityResultAnnotation, "name" ).name().toString();
			log.tracef( "Binding @EntityResult [mapping: %s] : %s", definition.getName(), entityName );

			final List<AnnotationInstance> orderedFieldResultAnnotations = new ArrayList<AnnotationInstance>();
			final List<String> propertyNames = new ArrayList<String>();

			final AnnotationInstance[] fieldAnnotations = bindingContext.nestedArrayExtractor.extract( entityResultAnnotation, "fields" );
			for ( AnnotationInstance fieldAnnotation : fieldAnnotations ) {
				final String fieldName = bindingContext.stringExtractor.extract( fieldAnnotation, "name" );
				if ( fieldName.indexOf( '.' ) == -1 ) {
					//regular property
					orderedFieldResultAnnotations.add( fieldAnnotation );
					propertyNames.add( fieldName );
				}
				else {
					/**
					 * Reorder properties
					 * 1. get the parent property
					 * 2. list all the properties following the expected one in the parent property
					 * 3. calculate the lowest index and insert the property
					 */
					final
					PersistentClass pc = bindingContext.getMetadataCollector().getEntityBinding( entityName );
					if ( pc == null ) {
						throw new MappingException(
								String.format(
										Locale.ENGLISH,
										"Could not resolve entity [%s] referenced in SqlResultSetMapping [%s]",
										entityName,
										definition.getName()
								)
						);
					}
					int dotIndex = fieldName.lastIndexOf( '.' );
					String reducedName = fieldName.substring( 0, dotIndex );
					Iterator parentPropItr = getSubPropertyIterator( pc, reducedName );
					List<String> followers = getFollowers( parentPropItr, reducedName, fieldName );

					int index = propertyNames.size();
					for ( String follower : followers ) {
						int currentIndex = getIndexOfFirstMatchingProperty( propertyNames, follower );
						index = currentIndex != -1 && currentIndex < index ? currentIndex : index;
					}
					propertyNames.add( index, fieldName );
					orderedFieldResultAnnotations.add( index, fieldAnnotation );
				}
			}


			Set<String> uniqueReturnProperty = new HashSet<String>();
			Map<String, ArrayList<String>> propertyResultsTmp = new HashMap<String, ArrayList<String>>();

			for ( AnnotationInstance fieldAnnotation : orderedFieldResultAnnotations ) {
				final String name = bindingContext.stringExtractor.extract( fieldAnnotation, "name" );
				if ( "class".equals( name ) ) {
					throw new MappingException(
							"class is not a valid property name to use in a @FieldResult, use @Entity(discriminatorColumn) instead"
					);
				}

				if ( uniqueReturnProperty.contains( name ) ) {
					throw new MappingException(
							"duplicate @FieldResult for property " + name +
									" on @Entity " + entityName + " in " + definition.getName() +
									"[" + entityResultAnnotation.target().toString() + "]"
					);

				}
				uniqueReturnProperty.add( name );

				final String quotingNormalizedColumnName = normalizeColumnQuoting(
						bindingContext.stringExtractor.extract( fieldAnnotation, "column" )
				);

				String key = StringHelper.root( name );
				ArrayList<String> intermediateResults = propertyResultsTmp.get( key );
				if ( intermediateResults == null ) {
					intermediateResults = new ArrayList<String>();
					propertyResultsTmp.put( key, intermediateResults );
				}
				intermediateResults.add( quotingNormalizedColumnName );
			}

			Map<String, String[]> propertyResults = new HashMap<String,String[]>();
			for ( Map.Entry<String, ArrayList<String>> entry : propertyResultsTmp.entrySet() ) {
				propertyResults.put(
						entry.getKey(),
						entry.getValue().toArray( new String[ entry.getValue().size() ] )
				);
			}

			if ( entityResultAnnotation.value( "discriminatorColumn" ) != null ) {
				final String quotingNormalizedName = normalizeColumnQuoting( entityResultAnnotation.value( "discriminatorColumn" ).asString() );
				propertyResults.put( "class", new String[] { quotingNormalizedName } );
			}

			if ( propertyResults.isEmpty() ) {
				propertyResults = java.util.Collections.emptyMap();
			}

			definition.addQueryReturn(
					new NativeSQLQueryRootReturn(
							"alias" + entityAliasIndex++,
							entityName,
							propertyResults,
							LockMode.READ
					)
			);
		}
	}

	private String normalizeColumnQuoting(String name) {
		return bindingContext.getMetadataCollector().getDatabase().toIdentifier( name ).render();
	}

	private List<String> getFollowers(Iterator parentPropIter, String reducedName, String name) {
		boolean hasFollowers = false;
		List<String> followers = new ArrayList<String>();
		while ( parentPropIter.hasNext() ) {
			String currentPropertyName = ( (Property) parentPropIter.next() ).getName();
			String currentName = reducedName + '.' + currentPropertyName;
			if ( hasFollowers ) {
				followers.add( currentName );
			}
			if ( name.equals( currentName ) ) {
				hasFollowers = true;
			}
		}
		return followers;
	}

	private Iterator getSubPropertyIterator(PersistentClass pc, String reducedName) {
		Value value = pc.getRecursiveProperty( reducedName ).getValue();
		Iterator parentPropIter;
		if ( value instanceof Component ) {
			Component comp = (Component) value;
			parentPropIter = comp.getPropertyIterator();
		}
		else if ( value instanceof ToOne ) {
			ToOne toOne = (ToOne) value;
			PersistentClass referencedPc = bindingContext.getMetadataCollector().getEntityBinding( toOne.getReferencedEntityName() );
			if ( toOne.getReferencedPropertyName() != null ) {
				try {
					parentPropIter = ( (Component) referencedPc.getRecursiveProperty(
							toOne.getReferencedPropertyName()
					).getValue() ).getPropertyIterator();
				}
				catch (ClassCastException e) {
					throw new MappingException(
							"dotted notation reference neither a component nor a many/one to one", e
					);
				}
			}
			else {
				try {
					if ( referencedPc.getIdentifierMapper() == null ) {
						parentPropIter = ( (Component) referencedPc.getIdentifierProperty()
								.getValue() ).getPropertyIterator();
					}
					else {
						parentPropIter = referencedPc.getIdentifierMapper().getPropertyIterator();
					}
				}
				catch (ClassCastException e) {
					throw new MappingException(
							"dotted notation reference neither a component nor a many/one to one", e
					);
				}
			}
		}
		else {
			throw new MappingException( "dotted notation reference neither a component nor a many/one to one" );
		}
		return parentPropIter;
	}

	private static int getIndexOfFirstMatchingProperty(List propertyNames, String follower) {
		int propertySize = propertyNames.size();
		for (int propIndex = 0; propIndex < propertySize; propIndex++) {
			if ( ( (String) propertyNames.get( propIndex ) ).startsWith( follower ) ) {
				return propIndex;
			}
		}
		return -1;
	}
}
