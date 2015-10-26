/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.persistence.InheritanceType;

import org.hibernate.AnnotationException;
import org.hibernate.MappingException;
import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.model.CustomSql;
import org.hibernate.boot.model.TruthValue;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.Column;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PrimaryKeyJoinColumn;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.spi.ConstraintSource;
import org.hibernate.boot.model.source.spi.EntityNamingSource;
import org.hibernate.boot.model.source.spi.EntitySource;
import org.hibernate.boot.model.source.spi.FilterSource;
import org.hibernate.boot.model.source.spi.SecondaryTableSource;
import org.hibernate.boot.model.source.spi.TableSpecificationSource;
import org.hibernate.boot.spi.InFlightMetadataCollector;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;

/**
 * Common base class for adapting Entity classes to the metamodel source structure.
 * <p/>
 * NOTE : defined as abstract because we classify entity mappings more concretely as:<ul>
 *     <li>the root of an entity hierarchy</li>
 *     <li>an entity subclass in an entity hierarchy</li>
 * </ul>
 *
 * @see MappedSuperclassSourceImpl
 *
 * @author Hardy Ferentschik
 * @author Gail Badner
 * @author Steve Ebersole
 */
public abstract class EntitySourceImpl extends IdentifiableTypeSourceAdapter implements EntitySource {
	private final String jpaEntityName;
	private final FilterSource[] filterSources;
	private final TableSpecificationSource primaryTable;
	private final Map<String,SecondaryTableSource> secondaryTableSourceMap;
	private final EntityBindingContext bindingContext;

	/**
	 * This is the form for building the root entity.  FWIW, the `rootEntity`
	 * argument is not really needed here
	 *
	 * @param entityTypeMetadata The root entity
	 * @param hierarchy The hierarchy the entity is the root of
	 * @param rootEntity Whether the entity is a root (it always is here).
	 */
	public EntitySourceImpl(
			EntityTypeMetadata entityTypeMetadata,
			EntityHierarchySourceImpl hierarchy,
			boolean rootEntity) {
		super( entityTypeMetadata, hierarchy, rootEntity );

		this.jpaEntityName = interpretJpaEntityName( entityTypeMetadata );

		this.bindingContext = entityTypeMetadata.getLocalBindingContext();

		addImports();
		this.filterSources = buildFilterSources();
		this.primaryTable = resolvePrimaryTable();
		this.secondaryTableSourceMap = buildSecondaryTableMap();
	}

	private String interpretJpaEntityName(ManagedTypeMetadata managedTypeMetadata) {
		if ( EntityTypeMetadata.class.isInstance( managedTypeMetadata ) ) {
			final EntityTypeMetadata entityTypeMetadata = (EntityTypeMetadata) managedTypeMetadata;
			if ( StringHelper.isNotEmpty( entityTypeMetadata.getExplicitEntityName() ) ) {
				return entityTypeMetadata.getExplicitEntityName();
			}
		}

		return StringHelper.unqualify( managedTypeMetadata.getName() );
	}

	/**
	 * Here is the form for persistent subclasses.
	 */
	protected EntitySourceImpl(
			EntityTypeMetadata managedTypeMetadata,
			EntityHierarchySourceImpl hierarchy,
			IdentifiableTypeSourceAdapter superTypeSource) {
		super( managedTypeMetadata, hierarchy, superTypeSource );

		this.jpaEntityName = interpretJpaEntityName( managedTypeMetadata );

		this.bindingContext = managedTypeMetadata.getLocalBindingContext();

		addImports();
		this.filterSources = buildFilterSources();
		this.primaryTable = resolvePrimaryTable();
		this.secondaryTableSourceMap = buildSecondaryTableMap();
	}

	private void addImports() {
		try {
			final InFlightMetadataCollector metadataImplementor = getEntityClass().getLocalBindingContext()
					.getMetadataCollector();
			metadataImplementor.addImport( getJpaEntityName(), getEntityName() );
			if ( !getEntityName().equals( getJpaEntityName() ) ) {
				metadataImplementor.addImport( getEntityName(), getEntityName() );
			}
		}
		catch ( MappingException e ) {
			throw new AnnotationException( "Use of the same entity name twice: " + getJpaEntityName(), e );
		}
	}

	private FilterSource[] buildFilterSources() {
		AnnotationInstance filtersAnnotation = findAnnotationInstance( HibernateDotNames.FILTERS );
		List<FilterSourceImpl> filterSourceList = new ArrayList<FilterSourceImpl>();

		if ( filtersAnnotation != null ) {
			AnnotationInstance[] annotationInstances = filtersAnnotation.value().asNestedArray();
			for ( AnnotationInstance filterAnnotation : annotationInstances ) {
				FilterSourceImpl filterSource = new FilterSourceImpl( filterAnnotation );
				filterSourceList.add( filterSource );
			}

		}

		AnnotationInstance filterAnnotation = findAnnotationInstance( HibernateDotNames.FILTER );

		if ( filterAnnotation != null ) {
			FilterSourceImpl filterSource = new FilterSourceImpl( filterAnnotation );
			filterSourceList.add( filterSource );
		}
		if ( filterSourceList.isEmpty() ) {
			return null;
		}
		else {
			return filterSourceList.toArray( new FilterSourceImpl[filterSourceList.size()] );
		}
	}

	private AnnotationInstance findAnnotationInstance(DotName annotationType) {
		return getEntityClass().findTypeAnnotation( annotationType );
	}

	protected boolean isRootEntity() {
		return false;
	}

	protected boolean definesItsOwnTable() {
		return !InheritanceType.SINGLE_TABLE.equals( getHierarchy().getHierarchyInheritanceType() )
				|| isRootEntity();
	}

	private TableSpecificationSource resolvePrimaryTable() {
		if ( !definesItsOwnTable() ) {
			return null;
		}

		// see if we have an inline view
		final AnnotationInstance subselectAnnotation = getEntityClass().findLocalTypeAnnotation( HibernateDotNames.SUB_SELECT );
		if ( subselectAnnotation != null ) {
			return new InLineViewSourceImpl( getEntityClass(), subselectAnnotation );
		}
		else {
			AnnotationInstance tableAnnotation = getEntityClass().findLocalTypeAnnotation( JpaDotNames.TABLE );
			return buildPrimaryTable( tableAnnotation, bindingContext );
		}
	}

	protected TableSpecificationSource buildPrimaryTable(
			AnnotationInstance tableAnnotation,
			EntityBindingContext bindingContext) {
		return TableSourceImpl.build( tableAnnotation, bindingContext );
	}

	public EntityTypeMetadata getEntityClass() {
		return (EntityTypeMetadata) getManagedTypeMetadata();
	}

	@Override
	public Origin getOrigin() {
		return getEntityClass().getLocalBindingContext().getOrigin();
	}

	public EntityBindingContext getLocalBindingContext() {
		return getEntityClass().getLocalBindingContext();
	}

	public String getEntityName() {
		return getClassName();
	}

	public String getClassName() {
		return getEntityClass().getName();
	}

	public String getJpaEntityName() {
		return jpaEntityName;
	}

	@Override
	public TableSpecificationSource getPrimaryTable() {
		return primaryTable;
	}

	@Override
	public Boolean isAbstract() {
		return getEntityClass().isAbstract();
	}

	@Override
	public boolean isLazy() {
		return getEntityClass().isLazy();
	}

	@Override
	public String getProxy() {
		return getEntityClass().getProxy();
	}

	@Override
	public int getBatchSize() {
		return getEntityClass().getBatchSize();
	}

	@Override
	public boolean isDynamicInsert() {
		return getEntityClass().isDynamicInsert();
	}

	@Override
	public boolean isDynamicUpdate() {
		return getEntityClass().isDynamicUpdate();
	}

	@Override
	public boolean isSelectBeforeUpdate() {
		return getEntityClass().isSelectBeforeUpdate();
	}

	@Override
	public String getTuplizerImplementationName() {
		return getEntityClass().getCustomTuplizerClassName();
	}

	@Override
	public String getCustomPersisterClassName() {
		return getEntityClass().getCustomPersister();
	}

	@Override
	public String getCustomLoaderName() {
		return getEntityClass().getCustomLoaderQueryName();
	}

	@Override
	public CustomSql getCustomSqlInsert() {
		return getEntityClass().getCustomInsert();
	}

	@Override
	public CustomSql getCustomSqlUpdate() {
		return getEntityClass().getCustomUpdate();
	}

	@Override
	public CustomSql getCustomSqlDelete() {
		return getEntityClass().getCustomDelete();
	}

	@Override
	public String[] getSynchronizedTableNames() {
		return getEntityClass().getSynchronizedTableNames();
	}

	@Override
	public FilterSource[] getFilterSources() {
		return filterSources;
	}

	@Override
	public String getDiscriminatorMatchValue() {
		return getEntityClass().getDiscriminatorMatchValue();
	}

	@Override
	public Collection<ConstraintSource> getConstraints() {
		Set<ConstraintSource> constraintSources = new HashSet<ConstraintSource>();

		// primary table
		{
			final AnnotationInstance table = getEntityClass().findTypeAnnotation( JpaDotNames.TABLE );
			if ( table != null ) {
				addUniqueConstraints( constraintSources, table, null );
				addIndexConstraints( constraintSources, table, null );
			}
		}

		// secondary table
		{
			final AnnotationInstance secondaryTable = getEntityClass().findTypeAnnotation( JpaDotNames.SECONDARY_TABLE );
			if ( secondaryTable != null ) {
				String tableName = getLocalBindingContext().getTypedValueExtractor( String.class )
						.extract( secondaryTable, "name" );
				addUniqueConstraints( constraintSources, secondaryTable, tableName );
				addIndexConstraints( constraintSources, secondaryTable, tableName );

			}
		}


		// secondary tables
		{
			final AnnotationInstance secondaryTables = getEntityClass().findTypeAnnotation( JpaDotNames.SECONDARY_TABLES );
			if ( secondaryTables != null ) {
				final AnnotationInstance[] secondaryTableArray = getLocalBindingContext().getTypedValueExtractor(
						AnnotationInstance[].class
				)
						.extract( secondaryTables, "value" );
				for ( AnnotationInstance secondaryTable : secondaryTableArray ) {
					String tableName = getLocalBindingContext().getTypedValueExtractor( String.class )
							.extract( secondaryTable, "name" );
					addUniqueConstraints( constraintSources, secondaryTable, tableName );
					addIndexConstraints( constraintSources, secondaryTable, tableName );
				}
			}
		}

		// collection tables
		{
			final Collection<AnnotationInstance> collectionTables = getEntityClass().getClassInfo().annotations().get(
					JpaDotNames.COLLECTION_TABLE
			);

			if ( collectionTables != null ) {
				for ( AnnotationInstance collectionTable : collectionTables ) {
					String tableName = getLocalBindingContext().getTypedValueExtractor( String.class )
							.extract( collectionTable, "name" );
					addUniqueConstraints( constraintSources, collectionTable, tableName );
					addIndexConstraints( constraintSources, collectionTable, tableName );
				}
			}
		}

		// join tables
		{
			final Collection<AnnotationInstance> joinTables = getEntityClass().getClassInfo().annotations().get(
					JpaDotNames.JOIN_TABLE
			);
			if ( joinTables != null ) {
				for (AnnotationInstance joinTable : joinTables) {
					String tableName = getLocalBindingContext().getTypedValueExtractor( String.class )
							.extract( joinTable, "name" );
					addUniqueConstraints( constraintSources, joinTable, tableName );
					addIndexConstraints( constraintSources, joinTable, tableName );
				}
			}
		}

		// table generators
		{
			final Collection<AnnotationInstance> tableGenerators = getEntityClass().getClassInfo().annotations().get(
					JpaDotNames.TABLE_GENERATOR
			);
			if ( tableGenerators != null ) {
				for (AnnotationInstance tableGenerator : tableGenerators) {
					String tableName = getLocalBindingContext().getTypedValueExtractor( String.class )
							.extract( tableGenerator, "table" );
					addUniqueConstraints( constraintSources, tableGenerator, tableName );
					addIndexConstraints( constraintSources, tableGenerator, tableName );
				}
			}
		}

		return constraintSources;
	}

	@Override
	public Map<String,SecondaryTableSource> getSecondaryTableMap() {
		return secondaryTableSourceMap;
	}

	private Map<String,SecondaryTableSource> buildSecondaryTableMap() {
		Map<String,SecondaryTableSource> secondaryTableSourcesMap = new HashMap<String, SecondaryTableSource>();

		// todo : should we walk MappedSuperclasses (if any) too?

		//	process a singular @SecondaryTable annotation
		{
			final AnnotationInstance secondaryTable = getEntityClass().findLocalTypeAnnotation( JpaDotNames.SECONDARY_TABLE );
			if ( secondaryTable != null ) {
				final SecondaryTableSource secondaryTableSource = createSecondaryTableSource( secondaryTable, true );
				secondaryTableSourcesMap.put(
						secondaryTableSource.getLogicalTableNameForContainedColumns(),
						secondaryTableSource
				);
			}
		}

		// process any @SecondaryTables grouping
		{
			final AnnotationInstance secondaryTables = getEntityClass().findLocalTypeAnnotation( JpaDotNames.SECONDARY_TABLES );
			if ( secondaryTables != null ) {
				AnnotationInstance[] tableAnnotations = getLocalBindingContext().getTypedValueExtractor( AnnotationInstance[].class )
						.extract( secondaryTables, "value" );
				for ( AnnotationInstance secondaryTable : tableAnnotations ) {
					final SecondaryTableSource secondaryTableSource = createSecondaryTableSource( secondaryTable, true );
					secondaryTableSourcesMap.put(
							secondaryTableSource.getLogicalTableNameForContainedColumns(),
							secondaryTableSource
					);
				}
			}
		}

		for ( PersistentAttribute attribute : getEntityClass().getPersistentAttributeMap().values() ) {
			if ( attribute.getAttributeNature() == PersistentAttribute.AttributeNature.TO_ONE ) {
				AnnotationInstance joinTableAnnotation = attribute.findAnnotation( JpaDotNames.JOIN_TABLE );
				if ( joinTableAnnotation != null ) {
					final SecondaryTableSource secondaryTableSource = createSecondaryTableSource( joinTableAnnotation, false );
					secondaryTableSourcesMap.put(
							secondaryTableSource.getLogicalTableNameForContainedColumns(),
							secondaryTableSource
					);
				}
			}
		}

		return secondaryTableSourcesMap;
	}

	private SecondaryTableSource createSecondaryTableSource(
			AnnotationInstance tableAnnotation,
			boolean isPrimaryKeyJoinColumn) {
		final List<? extends Column> keys = collectSecondaryTableKeys( tableAnnotation, isPrimaryKeyJoinColumn );
		return new SecondaryTableSourceImpl( TableSourceImpl.build( tableAnnotation, bindingContext ), keys );
	}

	private List<? extends Column> collectSecondaryTableKeys(
			final AnnotationInstance tableAnnotation,
			final boolean isPrimaryKeyJoinColumn) {
		final AnnotationInstance[] joinColumnAnnotations = getLocalBindingContext()
				.getTypedValueExtractor( AnnotationInstance[].class )
				.extract( tableAnnotation, isPrimaryKeyJoinColumn ? "pkJoinColumns" : "joinColumns" );

		if ( joinColumnAnnotations == null ) {
			return Collections.emptyList();
		}
		final List<Column> keys = new ArrayList<Column>();
		for ( final AnnotationInstance joinColumnAnnotation : joinColumnAnnotations ) {
			final Column joinColumn;
			if ( isPrimaryKeyJoinColumn ) {
				joinColumn =  new PrimaryKeyJoinColumn( joinColumnAnnotation );
			}
			else {
				joinColumn = new Column( joinColumnAnnotation );
			}
			keys.add( joinColumn );
		}
		return keys;
	}

	private void addUniqueConstraints(Set<ConstraintSource> constraintSources, AnnotationInstance tableAnnotation, String tableName) {
		final AnnotationValue value = tableAnnotation.value( "uniqueConstraints" );
		if ( value == null ) {
			return;
		}

		final AnnotationInstance[] uniqueConstraints = value.asNestedArray();
		for ( final AnnotationInstance unique : uniqueConstraints ) {
			final String name = unique.value( "name" ) == null ? null : unique.value( "name" ).asString();
			final String[] columnNames = unique.value( "columnNames" ).asStringArray();
			final UniqueConstraintSourceImpl uniqueConstraintSource = new UniqueConstraintSourceImpl(
					name,
					tableName,
					Arrays.asList( columnNames )
			);
			constraintSources.add( uniqueConstraintSource );
		}
	}

	private void addIndexConstraints(Set<ConstraintSource> constraintSources, AnnotationInstance tableAnnotation, String tableName) {
		final AnnotationValue value = tableAnnotation.value( "indexes" );
		if ( value == null ) {
			return;
		}

		final AnnotationInstance[] indexConstraints = value.asNestedArray();
		for ( final AnnotationInstance index : indexConstraints ) {
			final String name = index.value( "name" ) == null ? null : index.value( "name" ).asString();
			final String columnList = index.value( "columnList" ).asString();
			final boolean isUnique = index.value( "unique" ) == null ? false : index.value( "unique" ).asBoolean();
			
			// Taken from JPAIndexHolder.
			// TODO: Move elsewhere?
			final StringTokenizer tokenizer = new StringTokenizer( columnList, "," );
			final List<String> tmp = new ArrayList<String>();
			while ( tokenizer.hasMoreElements() ) {
				tmp.add( tokenizer.nextToken().trim() );
			}
			final List<String> columnNames = new ArrayList<String>();
			final List<String> orderings = new ArrayList<String>();
			for ( String indexColumn : tmp ) {
				indexColumn = indexColumn.toLowerCase();
				if ( indexColumn.endsWith( " desc" ) ) {
					columnNames.add( indexColumn.substring( 0, indexColumn.length() - 5 ) );
					orderings.add( "desc" );
				}
				else if ( indexColumn.endsWith( " asc" ) ) {
					columnNames.add( indexColumn.substring( 0, indexColumn.length() - 4 ) );
					orderings.add( "asc" );
				}
				else {
					columnNames.add( indexColumn );
					orderings.add( null );
				}
			}
			
			ConstraintSource constraintSource = new IndexConstraintSourceImpl(
					name,
					tableName,
					columnNames,
					orderings,
					isUnique
			);
			constraintSources.add( constraintSource );
		}
	}

	@Override
	public String getTypeName() {
		return getEntityClass().getName();
	}

	@Override
	public TruthValue quoteIdentifiersLocalToEntity() {
		// not exposed atm
		return TruthValue.UNKNOWN;
	}

	@Override
	public EntityNamingSource getEntityNamingSource() {
		return getEntityClass();
	}

	@Override
	public String toString() {
		return "EntitySourceImpl{entityClass=" + getEntityClass().getName() + "}";
	}
}


