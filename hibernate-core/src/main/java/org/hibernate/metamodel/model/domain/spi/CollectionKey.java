/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.engine.FetchTiming;
import org.hibernate.mapping.Collection;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.internal.ForeignKeyDomainResult;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.metamodel.model.relational.spi.ForeignKey;
import org.hibernate.query.sql.internal.ResolvedScalarDomainResult;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.SqlExpressionResolver;
import org.hibernate.sql.results.spi.DomainResult;
import org.hibernate.sql.results.spi.DomainResultCreationContext;
import org.hibernate.sql.results.spi.DomainResultCreationState;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class CollectionKey {
	private final AbstractPersistentCollectionDescriptor collectionDescriptor;
	private final JavaTypeDescriptor javaTypeDescriptor;

	private Navigable foreignKeyTargetNavigable;
	private ForeignKey joinForeignKey;

	public CollectionKey(
			AbstractPersistentCollectionDescriptor<?, ?, ?> collectionDescriptor,
			Collection bootCollectionValue,
			RuntimeModelCreationContext creationContext) {
		this.collectionDescriptor = collectionDescriptor;
		this.javaTypeDescriptor = resolveJavaTypeDescriptor( bootCollectionValue );
		this.joinForeignKey = creationContext.getDatabaseObjectResolver().resolveForeignKey(
				bootCollectionValue.getForeignKey()
		);
	}

	public ForeignKey getJoinForeignKey() {
		return joinForeignKey;
	}

	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return javaTypeDescriptor;
	}

	public Navigable getForeignKeyTargetNavigable() {
		return foreignKeyTargetNavigable;
	}

	public DomainResult createDomainResult(
			FetchTiming fetchTiming,
			boolean joinFetch,
			DomainResultCreationState creationState,
			DomainResultCreationContext creationContext) {
		// todo (6.0) : the collection is immediate fetch - which side of the FK to use depends on whether it is a joined fetch or a subsequent-select fetch:
		//		1) joined fetch : use the columns from the joined collection table - we need to be able to check for null (no collection elements)
		//		2) subsequent-select : use the columns from the container/owner table - the collection table is not part of the query

		// todo (6.0) previous instead or current?
		//		in conjunction with comment above about which columns...  which qualifier to use
		//		may be as simple as this.

		final ColumnReferenceQualifier referenceQualifier = creationState.getColumnReferenceQualifierStack().getCurrent();
		final SqlExpressionResolver expressionResolver = creationState.getSqlExpressionResolver();
		final List<Column> keyColumns = joinForeignKey.getColumnMappings().getTargetColumns();

		if ( keyColumns.size() == 1 ) {
			return new ResolvedScalarDomainResult(
					expressionResolver.resolveSqlSelection(
							expressionResolver.resolveSqlExpression( referenceQualifier, keyColumns.get( 0 ) ),
							keyColumns.get( 0 ).getJavaTypeDescriptor(),
							creationContext.getSessionFactory().getTypeConfiguration()
					),
					null,
					keyColumns.get( 0 ).getJavaTypeDescriptor()
			);
		}
		else {
			final List<SqlSelection> sqlSelections = new ArrayList<>();

			for ( Column column : keyColumns ) {
				sqlSelections.add(
						expressionResolver.resolveSqlSelection(
								expressionResolver.resolveSqlExpression(
										creationState.getColumnReferenceQualifierStack().getCurrent(),
										column
								),
								column.getJavaTypeDescriptor(),
								creationContext.getSessionFactory().getTypeConfiguration()
						)
				);
			}

			return new ForeignKeyDomainResult(
					getJavaTypeDescriptor(),
					sqlSelections
			);
		}
	}

	//	public ForeignKey.ColumnMappings buildJoinColumnMappings(List<Column> joinTargetColumns) {
//		// NOTE : called from JoinableAttributeContainer#resolveColumnMappings do not "refactor" into #getJoinColumnMappings()
//
//		// NOTE : joinTargetColumns are the owner's columns we join to (target) whereas #resolveJoinSourceColumns()
//		//		returns the collection's key columns (join/fk source).
//		// todo : would much rather carry forward the ForeignKey (in some "resolved" form from the mapping model
//		//		Same for entity-typed attributes (all JoinableAttributes)
//
//		final List<Column> joinSourceColumns = resolveJoinSourceColumns( joinTargetColumns );
//
//		if ( joinSourceColumns.size() != joinTargetColumns.size() ) {
//			throw new HibernateException( "Bad resolution of right-hand and left-hand columns for attribute join : " + collectionDescriptor );
//		}
//
//		final ForeignKey.ColumnMappings joinColumnMappings = CollectionHelper.arrayList( joinSourceColumns.size() );
//		for ( int i = 0; i < joinSourceColumns.size(); i++ ) {
//			joinColumnMappings.add(
//					new ForeignKey.ColumnMapping(
//							joinSourceColumns.get( i ),
//							joinTargetColumns.get( i )
//					)
//			);
//		}
//
//		return joinColumnMappings;
//	}
//
//	private List<Column> resolveJoinSourceColumns(List<Column> joinTargetColumns) {
//		// 	NOTE : If the elements are one-to-many (no collection table) we'd really need to understand
//		//		columns (or formulas) across the entity hierarchy.  For now we assume the persister's
//		// 		root table.  columns are conceivably doable already since @Column names a specific table.
//		//		Maybe we should add same to @Formula
//		//
//		//		on the bright side, atm CollectionPersister does not currently support
//		//		formulas in its key definition
//		final String[] columnNames = ( (Joinable) collectionDescriptor ).getKeyColumnNames();
//		final List<Column> columns = CollectionHelper.arrayList( columnNames.length );
//
//		assert joinTargetColumns.size() == columnNames.length;
//
//		final Table separateCollectionTable = collectionDescriptor.getSeparateCollectionTable();
//		if ( separateCollectionTable != null ) {
//			for ( int i = 0; i < columnNames.length; i++ ) {
//				columns.add(
//						separateCollectionTable.makeColumn(
//								columnNames[i],
//								joinTargetColumns.get( i ).getSqlTypeDescriptor().getJdbcTypeCode()
//						)
//				);
//			}
//		}
//		else {
//			// otherwise we just need to resolve the column names in the element table(s) (as the "collection table")
//			final EntityDescriptor elementPersister = ( (CollectionElementEntity) collectionDescriptor.getElementDescriptor() ).getEntityDescriptor();
//
//			for ( int i = 0; i < columnNames.length; i++ ) {
//				// it is conceivable that the column already exists
//				//		todo : is the same ^^ true for separateCollectionTable?
//				Column column = elementPersister.getPrimaryTable().locateColumn( columnNames[i] );
//				if ( column == null ) {
//					column = elementPersister.getPrimaryTable().makeColumn(
//							columnNames[i],
//							joinTargetColumns.get( i ).getSqlTypeDescriptor().getJdbcTypeCode()
//					);
//				}
//				columns.add( column );
//			}
//		}
//
//		return columns;
//	}

	private static JavaTypeDescriptor resolveJavaTypeDescriptor(Collection collectionValue) {
		if ( collectionValue.getJavaTypeMapping() != null ) {
			return collectionValue.getJavaTypeMapping().getJavaTypeDescriptor();
		}
		return null;
	}
}
