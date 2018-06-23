/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.spi;

import java.util.function.Consumer;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.metamodel.model.relational.spi.ForeignKey;
import org.hibernate.sql.JdbcValueCollector;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class CollectionKey implements Writeable<Object,Object> {
	private final AbstractPersistentCollectionDescriptor collectionDescriptor;
	private final JavaTypeDescriptor javaTypeDescriptor;

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

	@Override
	public Object unresolve(Object value, SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public void visitColumns(Consumer<Column> consumer) {
		for ( ForeignKey.ColumnMappings.ColumnMapping columnMapping : getJoinForeignKey().getColumnMappings().getColumnMappings() ) {
			consumer.accept( columnMapping.getTargetColumn() );
		}
	}

	@Override
	public void dehydrate(Object value, JdbcValueCollector jdbcValueCollector, SharedSessionContractImplementor session) {
		throw new NotYetImplementedFor6Exception();
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
		if ( collectionValue.getKey().getJavaTypeMapping() != null ) {
			return collectionValue.getKey().getJavaTypeMapping().resolveJavaTypeDescriptor();
		}
		return null;
	}
}
