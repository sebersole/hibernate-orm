/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.persister.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cache.spi.access.EntityDataAccess;
import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.DynamicFilterAliasGenerator;
import org.hibernate.internal.FilterAliasGenerator;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.jdbc.Expectation;
import org.hibernate.jdbc.Expectations;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Formula;
import org.hibernate.mapping.Join;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.MappedSuperclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Subclass;
import org.hibernate.mapping.Table;
import org.hibernate.mapping.Value;
import org.hibernate.metamodel.mapping.EntityDiscriminatorMapping;
import org.hibernate.metamodel.mapping.EntityIdentifierMapping;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.EntityVersionMapping;
import org.hibernate.metamodel.mapping.internal.BasicEntityIdentifierMappingImpl;
import org.hibernate.metamodel.mapping.internal.CaseStatementDiscriminatorMappingImpl;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationHelper;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;
import org.hibernate.metamodel.spi.RuntimeModelCreationContext;
import org.hibernate.persister.spi.PersisterCreationContext;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;
import org.hibernate.spi.NavigablePath;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.tree.from.NamedTableReference;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.ast.tree.from.TableReferenceJoin;
import org.hibernate.sql.group.builder.MutationSqlGroupBuilder;
import org.hibernate.sql.group.builder.TableInsertBuilder;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.entity.internal.EntityResultJoinedSubclassImpl;
import org.hibernate.type.BasicType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import org.jboss.logging.Logger;

import static java.util.Collections.emptyMap;

/**
 * An {@link EntityPersister} implementing the normalized
 * {@link jakarta.persistence.InheritanceType#JOINED} inheritance
 * mapping strategy for an entity and its inheritance hierarchy.
 * <p>
 * This is implemented as a separate table for each subclass,
 * with only declared attributes persisted as columns of that table.
 * Thus, each instance of a subclass has its state stored across
 * rows of multiple tables.
 *
 * @author Gavin King
 */
public class JoinedSubclassEntityPersister extends AbstractEntityPersister {
	private static final Logger log = Logger.getLogger( JoinedSubclassEntityPersister.class );

	private static final String IMPLICIT_DISCRIMINATOR_ALIAS = "clazz_";

	// the class hierarchy structure
	private final int tableSpan;
	private final boolean hasDuplicateTables;
	private final String[] tableNames;
	private final String[] naturalOrderTableNames;
	private final String[][] tableKeyColumns;
	private final String[][] tableKeyColumnReaders;
	private final String[][] tableKeyColumnReaderTemplates;
	private final String[][] naturalOrderTableKeyColumns;
	private final boolean[] naturalOrderCascadeDeleteEnabled;

	private final String[] spaces;

//	private final String[] subclassClosure;

	private final String[] subclassTableNameClosure;
	private final String[][] subclassTableKeyColumnClosure;
	private final boolean[] isClassOrSuperclassTable;

	// properties of this class, including inherited properties
	private final int[] naturalOrderPropertyTableNumbers;
//	private final int[] propertyTableNumbers;

	// the closure of all properties in the entire hierarchy including
	// subclasses and superclasses of this class
	private final int[] subclassPropertyTableNumberClosure;

	// the closure of all columns used by the entire hierarchy including
	// subclasses and superclasses of this class
	private final int[] subclassColumnTableNumberClosure;
//	private final int[] subclassFormulaTableNumberClosure;
	private final String[] subclassColumnClosure;

	private final boolean[] subclassTableSequentialSelect;
//	private final boolean[] subclassTableIsLazyClosure;
	private final boolean[] isInverseSubclassTable;
	private final boolean[] isNullableSubclassTable;

	// subclass discrimination works by assigning particular
	// values to certain combinations of not-null primary key
	// values in the outer join using an SQL CASE
	private final Map<Object,String> subclassesByDiscriminatorValue = new HashMap<>();
	private final String[] discriminatorValues;
	private final String[] notNullColumnNames;
	private final int[] notNullColumnTableNumbers;

	private final String[] constraintOrderedTableNames;
	private final String[][] constraintOrderedKeyColumnNames;

	private final Object discriminatorValue;
	private final String discriminatorSQLString;
	private final BasicType<?> discriminatorType;
	private final String explicitDiscriminatorColumnName;
	private final String discriminatorAlias;

	// Span of the tables directly mapped by this entity and super-classes, if any
	private final int coreTableSpan;
	// only contains values for SecondaryTables, ie. not tables part of the "coreTableSpan"
	private final boolean[] isNullableTable;
	private final boolean[] isInverseTable;

	private final Map<String, Object> discriminatorValuesByTableName;
	private final Map<String, String> discriminatorColumnNameByTableName;
	private final Map<String, String> subclassNameByTableName;

	//INITIALIZATION:

	@Deprecated(since = "6.0")
	public JoinedSubclassEntityPersister(
			final PersistentClass persistentClass,
			final EntityDataAccess cacheAccessStrategy,
			final NaturalIdDataAccess naturalIdRegionAccessStrategy,
			final PersisterCreationContext creationContext) throws HibernateException {
		this( persistentClass,cacheAccessStrategy,naturalIdRegionAccessStrategy,
				(RuntimeModelCreationContext) creationContext );
	}

	public JoinedSubclassEntityPersister(
			final PersistentClass persistentClass,
			final EntityDataAccess cacheAccessStrategy,
			final NaturalIdDataAccess naturalIdRegionAccessStrategy,
			final RuntimeModelCreationContext creationContext) throws HibernateException {

		super( persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, creationContext );

		final SessionFactoryImplementor factory = creationContext.getSessionFactory();
		final JdbcServices jdbcServices = factory.getServiceRegistry().getService( JdbcServices.class );
		final Dialect dialect = jdbcServices.getJdbcEnvironment().getDialect();
		final SqmFunctionRegistry sqmFunctionRegistry = factory.getQueryEngine().getSqmFunctionRegistry();


		// DISCRIMINATOR

		if ( persistentClass.isPolymorphic() ) {
			final Value discriminatorMapping = persistentClass.getDiscriminator();
			if ( discriminatorMapping != null ) {
				log.debug( "Encountered explicit discriminator mapping for joined inheritance" );

				final Selectable selectable = discriminatorMapping.getSelectables().get(0);
				if ( selectable instanceof Formula ) {
					throw new MappingException( "Discriminator formulas on joined inheritance hierarchies not supported at this time" );
				}
				else {
					final Column column = (Column) selectable;
					explicitDiscriminatorColumnName = column.getQuotedName( dialect );
					discriminatorAlias = column.getAlias( dialect, persistentClass.getRootTable() );
				}
				discriminatorType = DiscriminatorHelper.getDiscriminatorType( persistentClass );
				discriminatorValue = DiscriminatorHelper.getDiscriminatorValue( persistentClass );
				discriminatorSQLString = DiscriminatorHelper.getDiscriminatorSQLValue( persistentClass, dialect, factory );
			}
			else {
				explicitDiscriminatorColumnName = null;
				discriminatorAlias = IMPLICIT_DISCRIMINATOR_ALIAS;
				discriminatorType = factory.getTypeConfiguration()
						.getBasicTypeRegistry()
						.resolve( StandardBasicTypes.INTEGER );
				try {
					discriminatorValue = persistentClass.getSubclassId();
					discriminatorSQLString = discriminatorValue.toString();
				}
				catch ( Exception e ) {
					throw new MappingException( "Could not format discriminator value to SQL string", e );
				}
			}
		}
		else {
			explicitDiscriminatorColumnName = null;
			discriminatorAlias = IMPLICIT_DISCRIMINATOR_ALIAS;
			discriminatorType = factory.getTypeConfiguration()
					.getBasicTypeRegistry()
					.resolve( StandardBasicTypes.INTEGER );
			discriminatorValue = null;
			discriminatorSQLString = null;
		}

		if ( optimisticLockStyle().isAllOrDirty() ) {
			throw new MappingException( "optimistic-lock=all|dirty not supported for joined-subclass mappings [" + getEntityName() + "]" );
		}

		//MULTITABLES

		final int idColumnSpan = getIdentifierColumnSpan();

		ArrayList<String> tableNames = new ArrayList<>();
		ArrayList<String[]> keyColumns = new ArrayList<>();
		ArrayList<String[]> keyColumnReaders = new ArrayList<>();
		ArrayList<String[]> keyColumnReaderTemplates = new ArrayList<>();
		ArrayList<Boolean> cascadeDeletes = new ArrayList<>();
		List<Table> tableClosure = persistentClass.getTableClosure();
		List<KeyValue> keyClosure = persistentClass.getKeyClosure();
		for ( int i = 0; i < tableClosure.size() && i < keyClosure.size(); i++ ) {
			tableNames.add( determineTableName( tableClosure.get(i) ) );

			final KeyValue key = keyClosure.get(i);
			String[] keyCols = new String[idColumnSpan];
			String[] keyColReaders = new String[idColumnSpan];
			String[] keyColReaderTemplates = new String[idColumnSpan];
			List<Column> columns = key.getColumns();
			for ( int k = 0; k < idColumnSpan; k++ ) {
				Column column = columns.get(k);
				keyCols[k] = column.getQuotedName( dialect );
				keyColReaders[k] = column.getReadExpr( dialect );
				keyColReaderTemplates[k] = column.getTemplate(
						dialect,
						factory.getTypeConfiguration(),
						sqmFunctionRegistry
				);
			}
			keyColumns.add( keyCols );
			keyColumnReaders.add( keyColReaders );
			keyColumnReaderTemplates.add( keyColReaderTemplates );
			cascadeDeletes.add( key.isCascadeDeleteEnabled() && dialect.supportsCascadeDelete() );
		}

		//Span of the tableNames directly mapped by this entity and super-classes, if any
		coreTableSpan = tableNames.size();
		tableSpan = persistentClass.getJoinClosureSpan() + coreTableSpan;

		isNullableTable = new boolean[tableSpan];
		isInverseTable = new boolean[tableSpan];

		List<Join> joinClosure = persistentClass.getJoinClosure();
		for ( int i = 0; i < joinClosure.size(); i++ ) {
			Join join = joinClosure.get(i);
			isNullableTable[i] = join.isOptional();
			isInverseTable[i] = join.isInverse();

			tableNames.add( determineTableName( join.getTable() ) );

			KeyValue key = join.getKey();
			int joinIdColumnSpan = key.getColumnSpan();

			String[] keyCols = new String[joinIdColumnSpan];
			String[] keyColReaders = new String[joinIdColumnSpan];
			String[] keyColReaderTemplates = new String[joinIdColumnSpan];

			List<Column> columns = key.getColumns();
			for ( int k = 0; k < joinIdColumnSpan; k++ ) {
				Column column = columns.get(k);
				keyCols[k] = column.getQuotedName( dialect );
				keyColReaders[k] = column.getReadExpr( dialect );
				keyColReaderTemplates[k] = column.getTemplate(
						dialect,
						factory.getTypeConfiguration(),
						sqmFunctionRegistry
				);
			}
			keyColumns.add( keyCols );
			keyColumnReaders.add( keyColReaders );
			keyColumnReaderTemplates.add( keyColReaderTemplates );
			cascadeDeletes.add( key.isCascadeDeleteEnabled() && dialect.supportsCascadeDelete() );
		}

		hasDuplicateTables = new HashSet<>( tableNames ).size() == tableNames.size();
		naturalOrderTableNames = ArrayHelper.toStringArray( tableNames );
		naturalOrderTableKeyColumns = ArrayHelper.to2DStringArray( keyColumns );
		String[][] naturalOrderTableKeyColumnReaders = ArrayHelper.to2DStringArray(keyColumnReaders);
		String[][] naturalOrderTableKeyColumnReaderTemplates = ArrayHelper.to2DStringArray(keyColumnReaderTemplates);
		naturalOrderCascadeDeleteEnabled = ArrayHelper.toBooleanArray( cascadeDeletes );

		ArrayList<String> subclassTableNames = new ArrayList<>();
		ArrayList<Boolean> isConcretes = new ArrayList<>();
		ArrayList<Boolean> isDeferreds = new ArrayList<>();
//		ArrayList<Boolean> isLazies = new ArrayList<>();
		ArrayList<Boolean> isInverses = new ArrayList<>();
		ArrayList<Boolean> isNullables = new ArrayList<>();

		keyColumns = new ArrayList<>();
		for ( Table table : persistentClass.getSubclassTableClosure() ) {
			isConcretes.add( persistentClass.isClassOrSuperclassTable( table ) );
			isDeferreds.add( Boolean.FALSE );
//			isLazies.add( Boolean.FALSE );
			isInverses.add( Boolean.FALSE );
			isNullables.add( Boolean.FALSE );
			final String tableName = determineTableName( table );
			subclassTableNames.add( tableName );
			String[] key = new String[idColumnSpan];
			List<Column> columns = table.getPrimaryKey().getColumns();
			for ( int k = 0; k < idColumnSpan; k++ ) {
				key[k] = columns.get(k).getQuotedName( dialect );
			}
			keyColumns.add( key );
		}

		//Add joins
		for ( Join join : persistentClass.getSubclassJoinClosure() ) {
			final Table joinTable = join.getTable();

			isConcretes.add( persistentClass.isClassOrSuperclassTable( joinTable ) );
			isDeferreds.add( join.isSequentialSelect() );
			isInverses.add( join.isInverse() );
			isNullables.add( join.isOptional() );
//			isLazies.add( join.isLazy() );

			String joinTableName = determineTableName( joinTable );
			subclassTableNames.add( joinTableName );
			String[] key = new String[idColumnSpan];
			List<Column> columns = joinTable.getPrimaryKey().getColumns();
			for ( int k = 0; k < idColumnSpan; k++ ) {
				key[k] = columns.get(k).getQuotedName( dialect );
			}
			keyColumns.add( key );
		}

		String[] naturalOrderSubclassTableNameClosure = ArrayHelper.toStringArray( subclassTableNames );
		String[][] naturalOrderSubclassTableKeyColumnClosure = ArrayHelper.to2DStringArray( keyColumns );
		isClassOrSuperclassTable = ArrayHelper.toBooleanArray( isConcretes );
		subclassTableSequentialSelect = ArrayHelper.toBooleanArray( isDeferreds );
//		subclassTableIsLazyClosure = ArrayHelper.toBooleanArray( isLazies );
		isInverseSubclassTable = ArrayHelper.toBooleanArray( isInverses );
		isNullableSubclassTable = ArrayHelper.toBooleanArray( isNullables );

		constraintOrderedTableNames = new String[naturalOrderSubclassTableNameClosure.length];
		constraintOrderedKeyColumnNames = new String[naturalOrderSubclassTableNameClosure.length][];
		int currentPosition = 0;
		for ( int i = naturalOrderSubclassTableNameClosure.length - 1; i >= 0; i--, currentPosition++ ) {
			constraintOrderedTableNames[currentPosition] = naturalOrderSubclassTableNameClosure[i];
			constraintOrderedKeyColumnNames[currentPosition] = naturalOrderSubclassTableKeyColumnClosure[i];
		}

		/*
		 * Suppose an entity Client extends Person, mapped to the tableNames CLIENT and PERSON respectively.
		 * For the Client entity:
		 * naturalOrderTableNames -> PERSON, CLIENT; this reflects the sequence in which the tableNames are
		 * added to the meta-data when the annotated entities are processed.
		 * However, in some instances, for example when generating joins, the CLIENT table needs to be
		 * the first table as it will the driving table.
		 * tableNames -> CLIENT, PERSON
		 */

		this.tableNames = reverse( naturalOrderTableNames, coreTableSpan );
		tableKeyColumns = reverse( naturalOrderTableKeyColumns, coreTableSpan );
		tableKeyColumnReaders = reverse( naturalOrderTableKeyColumnReaders, coreTableSpan );
		tableKeyColumnReaderTemplates = reverse( naturalOrderTableKeyColumnReaderTemplates, coreTableSpan );
		subclassTableNameClosure = reverse( naturalOrderSubclassTableNameClosure, coreTableSpan );
		subclassTableKeyColumnClosure = reverse( naturalOrderSubclassTableKeyColumnClosure, coreTableSpan );

		spaces = ArrayHelper.join(
				this.tableNames,
				ArrayHelper.toStringArray( persistentClass.getSynchronizedTables() )
		);

		// Custom sql
		customSQLInsert = new String[tableSpan];
		customSQLUpdate = new String[tableSpan];
		customSQLDelete = new String[tableSpan];
		insertCallable = new boolean[tableSpan];
		updateCallable = new boolean[tableSpan];
		deleteCallable = new boolean[tableSpan];

		insertExpectations = new Expectation[tableSpan];
		updateExpectations = new Expectation[tableSpan];
		deleteExpectations = new Expectation[tableSpan];

		PersistentClass pc = persistentClass;
		int jk = coreTableSpan - 1;
		while ( pc != null ) {
			isNullableTable[jk] = false;
			isInverseTable[jk] = false;

			customSQLInsert[jk] = pc.getCustomSQLInsert();
			insertCallable[jk] = customSQLInsert[jk] != null && pc.isCustomInsertCallable();
			insertExpectations[jk] = Expectations.appropriateExpectation(
					pc.getCustomSQLInsertCheckStyle() == null
							? ExecuteUpdateResultCheckStyle.determineDefault( customSQLInsert[jk], insertCallable[jk] )
							: pc.getCustomSQLInsertCheckStyle()
			);

			customSQLUpdate[jk] = pc.getCustomSQLUpdate();
			updateCallable[jk] = customSQLUpdate[jk] != null && pc.isCustomUpdateCallable();
			updateExpectations[jk] = Expectations.appropriateExpectation(
					pc.getCustomSQLUpdateCheckStyle() == null
							? ExecuteUpdateResultCheckStyle.determineDefault( customSQLUpdate[jk], updateCallable[jk] )
							: pc.getCustomSQLUpdateCheckStyle()
			);

			customSQLDelete[jk] = pc.getCustomSQLDelete();
			deleteCallable[jk] = customSQLDelete[jk] != null && pc.isCustomDeleteCallable();
			deleteExpectations[jk] = Expectations.appropriateExpectation(
					pc.getCustomSQLDeleteCheckStyle() == null
					? ExecuteUpdateResultCheckStyle.determineDefault( customSQLDelete[jk], deleteCallable[jk] )
					: pc.getCustomSQLDeleteCheckStyle()
			);

			jk--;
			pc = pc.getSuperclass();
		}

		if ( jk != -1 ) {
			throw new AssertionFailure( "Tablespan does not match height of joined-subclass hierarchy." );
		}

		int j = coreTableSpan;
		for ( Join join : persistentClass.getJoinClosure() ) {
			isInverseTable[j] = join.isInverse();
			isNullableTable[j] = join.isOptional();

			customSQLInsert[j] = join.getCustomSQLInsert();
			insertCallable[j] = customSQLInsert[j] != null && join.isCustomInsertCallable();
			insertExpectations[j] = Expectations.appropriateExpectation(
					join.getCustomSQLInsertCheckStyle() == null
							? ExecuteUpdateResultCheckStyle.determineDefault( customSQLInsert[j], insertCallable[j] )
							: join.getCustomSQLInsertCheckStyle()
			);

			customSQLUpdate[j] = join.getCustomSQLUpdate();
			updateCallable[j] = customSQLUpdate[j] != null && join.isCustomUpdateCallable();
			updateExpectations[j] = Expectations.appropriateExpectation(
					join.getCustomSQLUpdateCheckStyle() == null
							? ExecuteUpdateResultCheckStyle.determineDefault( customSQLUpdate[j], updateCallable[j] )
							: join.getCustomSQLUpdateCheckStyle()
			);

			customSQLDelete[j] = join.getCustomSQLDelete();
			deleteCallable[j] = customSQLDelete[j] != null && join.isCustomDeleteCallable();
			deleteExpectations[j] = Expectations.appropriateExpectation(
					join.getCustomSQLDeleteCheckStyle() == null
							? ExecuteUpdateResultCheckStyle.determineDefault( customSQLDelete[j], deleteCallable[j] )
							: join.getCustomSQLDeleteCheckStyle()
			);

			j++;
		}

		// PROPERTIES
		int hydrateSpan = getPropertySpan();
		naturalOrderPropertyTableNumbers = new int[hydrateSpan];
//		propertyTableNumbers = new int[hydrateSpan];
		List<Property> propertyClosure = persistentClass.getPropertyClosure();
		for ( int i = 0; i < propertyClosure.size(); i++ ) {
			String tableName = propertyClosure.get(i).getValue().getTable().getQualifiedName(
					factory.getSqlStringGenerationContext()
			);
//			propertyTableNumbers[i] = getTableId( tableName, this.tableNames );
			naturalOrderPropertyTableNumbers[i] = getTableId( tableName, naturalOrderTableNames );
		}

		// subclass closure properties

		//TODO: code duplication with SingleTableEntityPersister

		ArrayList<Integer> columnTableNumbers = new ArrayList<>();
//		ArrayList<Integer> formulaTableNumbers = new ArrayList<>();
		ArrayList<Integer> propTableNumbers = new ArrayList<>();
		ArrayList<String> columns = new ArrayList<>();

		for ( Property property : persistentClass.getSubclassPropertyClosure() ) {
			String tableName = property.getValue().getTable().getQualifiedName(
					factory.getSqlStringGenerationContext()
			);
			Integer tableNumber = getTableId( tableName, subclassTableNameClosure );
			propTableNumbers.add( tableNumber );

			for ( Selectable selectable : property.getSelectables() ) {
				if ( !selectable.isFormula() ) {
					columnTableNumbers.add( tableNumber );
					Column column = (Column) selectable;
					columns.add( column.getQuotedName( dialect ) );
				}
//				else {
//					formulaTableNumbers.add( tableNumber );
//				}
			}
		}

		subclassColumnTableNumberClosure = ArrayHelper.toIntArray( columnTableNumbers );
		subclassPropertyTableNumberClosure = ArrayHelper.toIntArray( propTableNumbers );
//		subclassFormulaTableNumberClosure = ArrayHelper.toIntArray( formulaTableNumbers );
		subclassColumnClosure = ArrayHelper.toStringArray( columns );

		// SUBCLASSES

		int subclassSpan = persistentClass.getSubclassSpan() + 1;
//		subclassClosure = new String[subclassSpan];
		int subclassSpanMinusOne = subclassSpan - 1;
//		subclassClosure[subclassSpanMinusOne] = getEntityName();
		if ( !persistentClass.isPolymorphic() ) {
			subclassNameByTableName = emptyMap();
			discriminatorValuesByTableName = emptyMap();
			discriminatorColumnNameByTableName = emptyMap();
			discriminatorValues = null;
			notNullColumnTableNumbers = null;
			notNullColumnNames = null;
		}
		else {
			subclassesByDiscriminatorValue.put( discriminatorValue, getEntityName() );

			discriminatorValuesByTableName = CollectionHelper.linkedMapOfSize( subclassSpan + 1 );
			discriminatorColumnNameByTableName = CollectionHelper.linkedMapOfSize( subclassSpan + 1 );
			subclassNameByTableName = CollectionHelper.mapOfSize( subclassSpan + 1 );

			Table table = persistentClass.getTable();
			discriminatorValues = new String[subclassSpan];
			initDiscriminatorProperties( dialect, subclassSpanMinusOne, table, discriminatorValue );

			notNullColumnTableNumbers = new int[subclassSpan];
			final int id = getTableId(
					table.getQualifiedName( factory.getSqlStringGenerationContext() ),
					subclassTableNameClosure
			);
			notNullColumnTableNumbers[subclassSpanMinusOne] = id;
			notNullColumnNames = new String[subclassSpan];
			notNullColumnNames[subclassSpanMinusOne] = subclassTableKeyColumnClosure[id][0]; //( (Column) model.getTable().getPrimaryKey().getColumnIterator().next() ).getName();

			List<Subclass> subclasses = persistentClass.getSubclasses();
			for ( int k = 0; k < subclasses.size(); k++ ) {
				Subclass subclass = subclasses.get(k);
//				subclassClosure[k] = subclass.getEntityName();
				final Table subclassTable = subclass.getTable();
				subclassNameByTableName.put( subclassTable.getName(), subclass.getEntityName() );
				if ( persistentClass.isPolymorphic() ) {
					final Object discriminatorValue = explicitDiscriminatorColumnName != null
							? DiscriminatorHelper.getDiscriminatorValue( subclass )
							// we now use subclass ids that are consistent across all
							// persisters for a class hierarchy, so that the use of
							// "foo.class = Bar" works in HQL
							: subclass.getSubclassId();
					initDiscriminatorProperties( dialect, k, subclassTable, discriminatorValue );
					subclassesByDiscriminatorValue.put( discriminatorValue, subclass.getEntityName() );
					int tableId = getTableId(
							subclassTable.getQualifiedName( factory.getSqlStringGenerationContext() ),
							subclassTableNameClosure
					);
					notNullColumnTableNumbers[k] = tableId;
					notNullColumnNames[k] = subclassTableKeyColumnClosure[tableId][0]; //( (Column) sc.getTable().getPrimaryKey().getColumnIterator().next() ).getName();
				}
			}
		}

		subclassNamesBySubclassTable = buildSubclassNamesBySubclassTableMapping( persistentClass, factory );

		initSubclassPropertyAliasesMap( persistentClass );

		postConstruct( creationContext.getMetadata() );

	}

	private void initDiscriminatorProperties(Dialect dialect, int k, Table table, Object discriminatorValue) {
		final String tableName = determineTableName( table );
		final String columnName = table.getPrimaryKey().getColumn( 0 ).getQuotedName( dialect );
		discriminatorValuesByTableName.put( tableName, discriminatorValue );
		discriminatorColumnNameByTableName.put( tableName, columnName );
		discriminatorValues[k] = discriminatorValue.toString();
	}

	/**
	 * Used to hold the name of subclasses that each "subclass table" is part of.  For example, given a hierarchy like:
	 * {@code JoinedEntity <- JoinedEntitySubclass <- JoinedEntitySubSubclass}..
	 * <p/>
	 * For the persister for JoinedEntity, we'd have:
	 * <pre>
	 *	 subclassClosure[0] = "JoinedEntitySubSubclass"
	 *	 subclassClosure[1] = "JoinedEntitySubclass"
	 *	 subclassClosure[2] = "JoinedEntity"
	 *
	 *	 subclassTableNameClosure[0] = "T_JoinedEntity"
	 *	 subclassTableNameClosure[1] = "T_JoinedEntitySubclass"
	 *	 subclassTableNameClosure[2] = "T_JoinedEntitySubSubclass"
	 *
	 *	 subclassNameClosureBySubclassTable[0] = ["JoinedEntitySubSubclass", "JoinedEntitySubclass"]
	 *	 subclassNameClosureBySubclassTable[1] = ["JoinedEntitySubSubclass"]
	 * </pre>
	 * Note that there are only 2 entries in subclassNameClosureBySubclassTable.  That is because there are really only
	 * 2 tables here that make up the subclass mapping, the others make up the class/superclass table mappings.  We
	 * do not need to account for those here.  The "offset" is defined by the value of {@link #getTableSpan()}.
	 * Therefore the corresponding row in subclassNameClosureBySubclassTable for a given row in subclassTableNameClosure
	 * is calculated as {@code subclassTableNameClosureIndex - getTableSpan()}.
	 * <p/>
	 * As we consider each subclass table we can look into this array based on the subclass table's index and see
	 * which subclasses would require it to be included.  E.g., given {@code TREAT( x AS JoinedEntitySubSubclass )},
	 * when trying to decide whether to include join to "T_JoinedEntitySubclass" (subclassTableNameClosureIndex = 1),
	 * we'd look at {@code subclassNameClosureBySubclassTable[0]} and see if the TREAT-AS subclass name is included in
	 * its values.  Since {@code subclassNameClosureBySubclassTable[1]} includes "JoinedEntitySubSubclass", we'd
	 * consider it included.
	 * <p/>
	 * {@link #subclassTableNameClosure} also accounts for secondary tables and we properly handle those as we
	 * build the subclassNamesBySubclassTable array and they are therefore properly handled when we use it
	 */
	private final String[][] subclassNamesBySubclassTable;

	/**
	 * Essentially we are building a mapping that we can later use to determine whether a given "subclass table"
	 * should be included in joins when JPA TREAT-AS is used.
	 *
	 * @return subclassNamesBySubclassTable
	 */
	private String[][] buildSubclassNamesBySubclassTableMapping(
			PersistentClass persistentClass,
			SessionFactoryImplementor factory) {
		// this value represents the number of subclasses (and not the class itself)
		final int numberOfSubclassTables = subclassTableNameClosure.length - coreTableSpan;
		if ( numberOfSubclassTables == 0 ) {
			return new String[0][];
		}

		final String[][] mapping = new String[numberOfSubclassTables][];
		processPersistentClassHierarchy( persistentClass, true, factory, mapping );
		return mapping;
	}

	private Set<String> processPersistentClassHierarchy(
			PersistentClass persistentClass,
			boolean isBase,
			SessionFactoryImplementor factory,
			String[][] mapping) {

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// collect all the class names that indicate that the "main table" of the given PersistentClass should be
		// included when one of the collected class names is used in TREAT
		final Set<String> classNames = new HashSet<>();

		for ( Subclass subclass : persistentClass.getDirectSubclasses() ) {
			final Set<String> subclassSubclassNames = processPersistentClassHierarchy(
					subclass,
					false,
					factory,
					mapping
			);
			classNames.addAll( subclassSubclassNames );
		}

		classNames.add( persistentClass.getEntityName() );

		if ( ! isBase ) {
			MappedSuperclass msc = persistentClass.getSuperMappedSuperclass();
			while ( msc != null ) {
				classNames.add( msc.getMappedClass().getName() );
				msc = msc.getSuperMappedSuperclass();
			}

			associateSubclassNamesToSubclassTableIndexes( persistentClass, classNames, mapping, factory );
		}

		return classNames;
	}

	private void associateSubclassNamesToSubclassTableIndexes(
			PersistentClass persistentClass,
			Set<String> classNames,
			String[][] mapping,
			SessionFactoryImplementor factory) {

		final String tableName = persistentClass.getTable().getQualifiedName(
				factory.getSqlStringGenerationContext()
		);

		associateSubclassNamesToSubclassTableIndex( tableName, classNames, mapping );

		for ( Join join : persistentClass.getJoins() ) {
			final String secondaryTableName = join.getTable().getQualifiedName(
					factory.getSqlStringGenerationContext()
			);
			associateSubclassNamesToSubclassTableIndex( secondaryTableName, classNames, mapping );
		}
	}

	private void associateSubclassNamesToSubclassTableIndex(
			String tableName,
			Set<String> classNames,
			String[][] mapping) {
		// find the table's entry in the subclassTableNameClosure array
		boolean found = false;
		for ( int i = 0; i < subclassTableNameClosure.length; i++ ) {
			if ( subclassTableNameClosure[i].equals( tableName ) ) {
				found = true;
				final int index = i - coreTableSpan;
				if ( index < 0 || index >= mapping.length ) {
					throw new IllegalStateException(
							String.format(
									"Encountered 'subclass table index' [%s] was outside expected range ( [%s] < i < [%s] )",
									index,
									0,
									mapping.length
							)
					);
				}
				mapping[index] = ArrayHelper.toStringArray( classNames );
				break;
			}
		}
		if ( !found ) {
			throw new IllegalStateException(
					String.format(
							"Was unable to locate subclass table [%s] in 'subclassTableNameClosure'",
							tableName
					)
			);
		}
	}

	@Override
	public boolean isNullableTable(int j) {
		return isNullableTable[j];
	}

	@Override
	public boolean hasSkippableTables() {
		// todo (6.x) : cache this?
		return hasAnySkippableTables( isNullableTable, isInverseTable );
	}

	@Override
	public boolean isInverseTable(int j) {
		return isInverseTable[j];
	}

	@Override
	protected boolean isSubclassTableSequentialSelect(int j) {
		return subclassTableSequentialSelect[j] && !isClassOrSuperclassTable[j];
	}

	/*public void postInstantiate() throws MappingException {
		super.postInstantiate();
		//TODO: other lock modes?
		loader = createEntityLoader(LockMode.NONE, CollectionHelper.EMPTY_MAP);
	}*/

	@Override
	public String getSubclassPropertyTableName(int i) {
		return subclassTableNameClosure[subclassPropertyTableNumberClosure[i]];
	}

	@Override
	protected boolean isInverseSubclassTable(int j) {
		return isInverseSubclassTable[j];
	}

	@Override
	protected boolean isNullableSubclassTable(int j) {
		return isNullableSubclassTable[j];
	}

	@Override
	public Type getDiscriminatorType() {
		return discriminatorType;
	}

	@Override
	public Object getDiscriminatorValue() {
		return discriminatorValue;
	}

	@Override
	public String getDiscriminatorSQLValue() {
		return discriminatorSQLString;
	}

	@Override
	public String getDiscriminatorColumnName() {
		return explicitDiscriminatorColumnName == null
				? super.getDiscriminatorColumnName()
				: explicitDiscriminatorColumnName;
	}

	@Override
	public String getDiscriminatorColumnReaders() {
		return getDiscriminatorColumnName();
	}

	@Override
	public String getDiscriminatorColumnReaderTemplate() {
		return getDiscriminatorColumnName();
	}

	@Override
	public String getDiscriminatorAlias() {
		return discriminatorAlias;
	}

	@Override
	public String getSubclassForDiscriminatorValue(Object value) {
		if ( value == null ) {
			return subclassesByDiscriminatorValue.get( DiscriminatorHelper.NULL_DISCRIMINATOR );
		}
		else {
			String result = subclassesByDiscriminatorValue.get( value );
			if ( result == null ) {
				result = subclassesByDiscriminatorValue.get( DiscriminatorHelper.NOT_NULL_DISCRIMINATOR );
			}
			return result;
		}
	}

	@Override
	protected void addDiscriminatorToInsertGroup(MutationSqlGroupBuilder<TableInsertBuilder> insertGroupBuilder) {
		if ( explicitDiscriminatorColumnName != null ) {
			insertGroupBuilder.getTableDetailsBuilder( getRootTableName() ).addValuesColumn( explicitDiscriminatorColumnName, getDiscriminatorSQLValue() );
		}
	}

	@Override
	public Serializable[] getPropertySpaces() {
		return spaces; // don't need subclass tables, because they can't appear in conditions
	}

	@Override
	public boolean hasDuplicateTables() {
		return hasDuplicateTables;
	}

	@Override
	public String getTableName(int j) {
		return naturalOrderTableNames[j];
	}

	@Override
	public String[] getKeyColumns(int j) {
		return naturalOrderTableKeyColumns[j];
	}

	@Override
	public boolean isTableCascadeDeleteEnabled(int j) {
		return naturalOrderCascadeDeleteEnabled[j];
	}

	@Override
	public boolean isPropertyOfTable(int property, int j) {
		return naturalOrderPropertyTableNumbers[property] == j;
	}

	/**
	 * Reverse the first n elements of the incoming array
	 *
	 * @return New array with the first n elements in reversed order
	 */
	private static String[] reverse(String[] objects, int n) {

		int size = objects.length;
		String[] temp = new String[size];

		for ( int i = 0; i < n; i++ ) {
			temp[i] = objects[n - i - 1];
		}

		for ( int i = n; i < size; i++ ) {
			temp[i] = objects[i];
		}

		return temp;
	}

	/**
	 * Reverse the first n elements of the incoming array
	 *
	 * @return New array with the first n elements in reversed order
	 */
	private static String[][] reverse(String[][] objects, int n) {
		int size = objects.length;
		String[][] temp = new String[size][];
		for ( int i = 0; i < n; i++ ) {
			temp[i] = objects[n - i - 1];
		}

		for ( int i = n; i < size; i++ ) {
			temp[i] = objects[i];
		}

		return temp;
	}

	@Override
	public String fromTableFragment(String alias) {
		return getTableName() + ' ' + alias;
	}

	@Override
	public String getTableName() {
		return tableNames[0];
	}

	@Override
	public String generateFilterConditionAlias(String rootAlias) {
		return generateTableAlias( rootAlias, tableSpan - 1 );
	}

	@Override
	public String[] getIdentifierColumnNames() {
		return tableKeyColumns[0];
	}

	@Override
	public String[] getIdentifierColumnReaderTemplates() {
		return tableKeyColumnReaderTemplates[0];
	}

	@Override
	public String getRootTableName() {
		return  naturalOrderTableNames[0];
	}

	@Override
	public String[] getIdentifierColumnReaders() {
		return tableKeyColumnReaders[0];
	}

	@Override
	protected int getSubclassPropertyTableNumber(int i) {
		return subclassPropertyTableNumberClosure[i];
	}

	@Override
	public int getTableSpan() {
		return tableSpan;
	}

	@Override
	protected boolean hasMultipleTables() {
		return true;
	}

	@Override
	protected int[] getPropertyTableNumbers() {
		return naturalOrderPropertyTableNumbers;
	}

	@Override
	protected String[] getSubclassTableKeyColumns(int j) {
		return subclassTableKeyColumnClosure[j];
	}

	@Override
	public String getSubclassTableName(int j) {
		return subclassTableNameClosure[j];
	}

	@Override
	protected String[] getSubclassTableNames() {
		return subclassTableNameClosure;
	}

	@Override
	public int getSubclassTableSpan() {
		return subclassTableNameClosure.length;
	}

	@Override
	protected boolean shouldProcessSuperMapping() {
		return false;
	}

	@Override
	protected boolean isClassOrSuperclassTable(int j) {
		return isClassOrSuperclassTable[j];
	}

	@Override
	protected boolean isSubclassTableIndicatedByTreatAsDeclarations(
			int subclassTableNumber,
			Set<String> treatAsDeclarations) {
		if ( treatAsDeclarations == null || treatAsDeclarations.isEmpty() ) {
			return false;
		}

		final String[] inclusionSubclassNameClosure = getSubclassNameClosureBySubclassTable( subclassTableNumber );

		// NOTE : we assume the entire hierarchy is joined-subclass here
		for ( String subclassName : treatAsDeclarations ) {
			for ( String inclusionSubclassName : inclusionSubclassNameClosure ) {
				if ( inclusionSubclassName.equals( subclassName ) ) {
					return true;
				}
			}
		}

		return false;
	}

	private String[] getSubclassNameClosureBySubclassTable(int subclassTableNumber) {
		final int index = subclassTableNumber - getTableSpan();

		if ( index >= subclassNamesBySubclassTable.length ) {
			throw new IllegalArgumentException(
					"Given subclass table number is outside expected range [" + (subclassNamesBySubclassTable.length -1)
							+ "] as defined by subclassTableNameClosure/subclassClosure"
			);
		}

		return subclassNamesBySubclassTable[index];
	}

	@Override
	public String[] getConstraintOrderedTableNameClosure() {
		return constraintOrderedTableNames;
	}

	@Override
	public String[][] getContraintOrderedTableKeyColumnClosure() {
		return constraintOrderedKeyColumnNames;
	}

	@Override
	public String getRootTableAlias(String drivingAlias) {
		return generateTableAlias( drivingAlias, getTableId( getRootTableName(), tableNames ) );
	}

	@Override
	public Declarer getSubclassPropertyDeclarer(String propertyPath) {
		if ( "class".equals( propertyPath ) ) {
			// special case where we need to force include all subclass joins
			return Declarer.SUBCLASS;
		}
		return super.getSubclassPropertyDeclarer( propertyPath );
	}

	@Override
	public int determineTableNumberForColumn(String columnName) {
		// HHH-7630: In case the naturalOrder/identifier column is explicitly given in the ordering, check here.
		for ( int i = 0, max = naturalOrderTableKeyColumns.length; i < max; i++ ) {
			final String[] keyColumns = naturalOrderTableKeyColumns[i];
			if ( ArrayHelper.contains( keyColumns, columnName ) ) {
				return naturalOrderPropertyTableNumbers[i];
			}
		}

		for (int i = 0, max = subclassColumnClosure.length; i < max; i++ ) {
			final boolean quoted = subclassColumnClosure[i].startsWith( "\"" )
					&& subclassColumnClosure[i].endsWith( "\"" );
			if ( quoted ) {
				if ( subclassColumnClosure[i].equals( columnName ) ) {
					return subclassColumnTableNumberClosure[i];
				}
			}
			else {
				if ( subclassColumnClosure[i].equalsIgnoreCase( columnName ) ) {
					return subclassColumnTableNumberClosure[i];
				}
			}
		}
		throw new HibernateException( "Could not locate table which owns column [" + columnName + "] referenced in order-by mapping" );
	}

	@Override
	protected EntityVersionMapping generateVersionMapping(
			Supplier<?> templateInstanceCreator,
			PersistentClass bootEntityDescriptor,
			MappingModelCreationProcess creationProcess) {
		if ( getVersionType() == null ) {
			return null;
		}
		else {
			if ( getTableName().equals( getVersionedTableName() ) ) {
				final int versionPropertyIndex = getVersionProperty();
				final String versionPropertyName = getPropertyNames()[versionPropertyIndex];
				return creationProcess.processSubPart(
						versionPropertyName,
						(role, process) -> generateVersionMapping(
								this,
								templateInstanceCreator,
								bootEntityDescriptor,
								process
						)
				);
			}
			else if ( getSuperMappingType() != null ) {
				return getSuperMappingType().getVersionMapping();
			}
		}
		return null;
	}

	@Override
	protected EntityIdentifierMapping generateIdentifierMapping(
			Supplier<?> templateInstanceCreator,
			PersistentClass bootEntityDescriptor,
			MappingModelCreationProcess creationProcess) {
		final Type idType = getIdentifierType();

		if ( idType instanceof CompositeType ) {
			final CompositeType cidType = (CompositeType) idType;

			// NOTE: the term `isEmbedded` here uses Hibernate's older (pre-JPA) naming for its "non-aggregated"
			// composite-id support.  It unfortunately conflicts with the JPA usage of "embedded".  Here we normalize
			// the legacy naming to the more descriptive encapsulated versus non-encapsulated phrasing

			final boolean encapsulated = ! cidType.isEmbedded();
			if ( encapsulated ) {
				// we have an `@EmbeddedId`
				return MappingModelCreationHelper.buildEncapsulatedCompositeIdentifierMapping(
						this,
						bootEntityDescriptor.getIdentifierProperty(),
						bootEntityDescriptor.getIdentifierProperty().getName(),
						getTableName(),
						tableKeyColumns[0],
						cidType,
						creationProcess
				);
			}

			// otherwise we have a non-encapsulated composite-identifier
			return generateNonEncapsulatedCompositeIdentifierMapping( creationProcess, bootEntityDescriptor );
		}

		final String columnDefinition;
		final Long length;
		final Integer precision;
		final Integer scale;
		if ( bootEntityDescriptor.getIdentifier() == null ) {
			columnDefinition = null;
			length = null;
			precision = null;
			scale = null;
		}
		else {
			Column column = bootEntityDescriptor.getIdentifier().getColumns().get( 0 );
			columnDefinition = column.getSqlType();
			length = column.getLength();
			precision = column.getPrecision();
			scale = column.getScale();
		}
		final Value value = bootEntityDescriptor.getIdentifierProperty().getValue();
		return new BasicEntityIdentifierMappingImpl(
				this,
				templateInstanceCreator,
				bootEntityDescriptor.getIdentifierProperty().getName(),
				getTableName(),
				tableKeyColumns[0][0],
				columnDefinition,
				length,
				precision,
				scale,
				Value.isInsertable( value, 0),
				Value.isUpdateable( value, 0),
				(BasicType<?>) idType,
				creationProcess
		);
	}

	@Override
	protected boolean isPhysicalDiscriminator() {
		return explicitDiscriminatorColumnName != null;
	}

	@Override
	protected EntityDiscriminatorMapping generateDiscriminatorMapping(
			PersistentClass bootEntityDescriptor,
			MappingModelCreationProcess modelCreationProcess) {
		EntityMappingType superMappingType = getSuperMappingType();
		if ( superMappingType != null ) {
			return superMappingType.getDiscriminatorMapping();
		}

		if ( hasSubclasses() ) {
			final String formula = getDiscriminatorFormulaTemplate();
			if ( explicitDiscriminatorColumnName != null || formula != null ) {
				// even though this is a joined-hierarchy the user has defined an
				// explicit discriminator column - so we can use the normal
				// discriminator mapping
				return super.generateDiscriminatorMapping( bootEntityDescriptor, modelCreationProcess );
			}

			org.hibernate.persister.entity.DiscriminatorType<?> discriminatorMetadataType = (org.hibernate.persister.entity.DiscriminatorType<?>) getTypeDiscriminatorMetadata().getResolutionType();

			// otherwise, we need to use the case-statement approach
			return new CaseStatementDiscriminatorMappingImpl(
					this,
					subclassTableNameClosure,
					notNullColumnTableNumbers,
					notNullColumnNames,
					discriminatorValues,
					subclassNameByTableName,
					discriminatorMetadataType,
					modelCreationProcess
			);
		}

		return null;
	}

	@Override
	protected EntityIdentifierMapping generateNonEncapsulatedCompositeIdentifierMapping(
			MappingModelCreationProcess creationProcess,
			PersistentClass bootEntityDescriptor) {
		assert declaredAttributeMappings != null;

		return MappingModelCreationHelper.buildNonEncapsulatedCompositeIdentifierMapping(
				this,
				getTableName(),
				tableKeyColumns[0],
				bootEntityDescriptor,
				creationProcess
		);
	}

	@Override
	public FilterAliasGenerator getFilterAliasGenerator(String rootAlias) {
		return new DynamicFilterAliasGenerator(subclassTableNameClosure, rootAlias);
	}

	@Override
	public <T> DomainResult<T> createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			String resultVariable,
			DomainResultCreationState creationState) {
		if ( hasSubclasses() ) {
			final EntityResultJoinedSubclassImpl entityResultJoinedSubclass = new EntityResultJoinedSubclassImpl(
					navigablePath,
					this,
					tableGroup,
					resultVariable,
					creationState
			);
			entityResultJoinedSubclass.afterInitialize( entityResultJoinedSubclass, creationState );
			//noinspection unchecked
			return entityResultJoinedSubclass;
		}
		else {
			return super.createDomainResult( navigablePath, tableGroup, resultVariable, creationState );
		}
	}

	@Override
	public void pruneForSubclasses(TableGroup tableGroup, Set<String> treatedEntityNames) {
		final Set<TableReference> retainedTableReferences = new HashSet<>( treatedEntityNames.size() );
		final Set<String> sharedSuperclassTables = new HashSet<>();
		final MappingMetamodelImplementor metamodel = getFactory().getRuntimeMetamodels().getMappingMetamodel();

		for ( String treatedEntityName : treatedEntityNames ) {
			final JoinedSubclassEntityPersister subPersister =
					(JoinedSubclassEntityPersister) metamodel.findEntityDescriptor( treatedEntityName );
			final String[] subclassTableNames = subPersister.getSubclassTableNames();
			// For every treated entity name, we collect table names that are needed by all treated entity names
			// In mathematical terms, sharedSuperclassTables will be the "intersection" of the table names of all treated entities
			if ( sharedSuperclassTables.isEmpty() ) {
				for ( int i = 0; i < subclassTableNames.length; i++ ) {
					if ( subPersister.isClassOrSuperclassTable[i] ) {
						sharedSuperclassTables.add( subclassTableNames[i] );
					}
				}
			}
			else {
				sharedSuperclassTables.retainAll( Arrays.asList( subclassTableNames ) );
			}
			// Add the table references for all table names of the treated entities as we have to retain these table references.
			// Table references not appearing in this set can later be pruned away
			// todo (6.0): no need to resolve all table references, only the ones needed for cardinality
			for ( String subclassTableName : subclassTableNames ) {
				retainedTableReferences.add(tableGroup.resolveTableReference(null, subclassTableName, false));
			}
		}
		final List<TableReferenceJoin> tableReferenceJoins = tableGroup.getTableReferenceJoins();
		// The optimization is to remove all table reference joins that are not contained in the retainedTableReferences
		// In addition, we switch from a possible LEFT join, to an inner join for all sharedSuperclassTables
		// For now, we can only do this if the table group reports canUseInnerJoins or isRealTableGroup,
		// because the switch for table reference joins to INNER must be cardinality preserving.
		// If canUseInnerJoins is true, this is trivially given, but also if the table group is real
		// i.e. with parenthesis around, as that means the table reference joins will be isolated
		if ( tableGroup.canUseInnerJoins() || tableGroup.isRealTableGroup() ) {
			final TableReferenceJoin[] oldJoins = tableReferenceJoins.toArray( new TableReferenceJoin[0] );
			tableReferenceJoins.clear();
			for ( TableReferenceJoin oldJoin : oldJoins ) {
				final NamedTableReference joinedTableReference = oldJoin.getJoinedTableReference();
				if ( retainedTableReferences.contains( joinedTableReference ) ) {
					if ( oldJoin.getJoinType() != SqlAstJoinType.INNER
							&& sharedSuperclassTables.contains( joinedTableReference.getTableExpression() ) ) {
						tableReferenceJoins.add(
								new TableReferenceJoin(
										true,
										joinedTableReference,
										oldJoin.getPredicate()
								)
						);
					}
					else {
						tableReferenceJoins.add( oldJoin );
					}
				}
			}
		}
		else {
			tableReferenceJoins
					.removeIf( join -> !retainedTableReferences.contains( join.getJoinedTableReference() ) );
		}
	}

	@Override
	public void visitConstraintOrderedTables(ConstraintOrderedTableConsumer consumer) {
		for ( int i = 0; i < constraintOrderedTableNames.length; i++ ) {
			final String tableName = constraintOrderedTableNames[i];
			final int tablePosition = i;

			consumer.consume(
					tableName,
					() -> columnConsumer -> columnConsumer.accept( tableName, constraintOrderedKeyColumnNames[tablePosition] )
			);
		}
	}

}
