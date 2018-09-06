/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.tree.spi.from;


import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.metamodel.model.domain.spi.PersistentCollectionDescriptor;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.metamodel.model.relational.spi.Table;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.consume.spi.SqlAppender;
import org.hibernate.sql.ast.consume.spi.SqlAstWalker;
import org.hibernate.sql.ast.produce.spi.QualifiableSqlExpressable;
import org.hibernate.sql.ast.tree.spi.expression.ColumnReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.PluralAttributeReference;

/**
 * A TableSpecificationGroup for a collection reference
 *
 * @author Steve Ebersole
 */
public class CollectionTableGroup extends AbstractTableGroup {
	// todo (6.0) : should implement Selectable as well

	private final PersistentCollectionDescriptor descriptor;

	private final LockMode lockMode;
	private final NavigablePath navigablePath;
	private final TableReference collectionTableReference;
	private final List<TableReferenceJoin> tableReferenceJoins;

	private final PluralAttributeReference navigableReference;

	public CollectionTableGroup(
			String uid,
			TableSpace tableSpace,
			PersistentCollectionDescriptor descriptor,
			LockMode lockMode,
			NavigablePath navigablePath,
			TableReference collectionTableReference,
			List<TableReferenceJoin> joins) {
		super( tableSpace, uid );
		this.descriptor = descriptor;
		this.lockMode = lockMode;
		this.navigablePath = navigablePath;
		this.collectionTableReference = collectionTableReference;
		this.tableReferenceJoins = joins;

		this.navigableReference = new PluralAttributeReference(
				// todo (6.0) : need the container (if one) to pass along
				null,
				descriptor.getDescribedAttribute(),
				navigablePath
		);
	}

	public PersistentCollectionDescriptor getDescriptor() {
		return descriptor;
	}

	@Override
	protected TableReference getPrimaryTableReference() {
		return null;
	}

	@Override
	protected List<TableReferenceJoin> getTableReferenceJoins() {
		return null;
	}

	@Override
	public NavigableReference getNavigableReference() {
		return navigableReference;
	}

	@Override
	public TableReference locateTableReference(Table table) {
		if ( table == collectionTableReference.getTable() ) {
			return collectionTableReference;
		}

		if ( tableReferenceJoins != null && !tableReferenceJoins.isEmpty() ) {
			for ( TableReferenceJoin tableReferenceJoin : tableReferenceJoins ) {
				if ( tableReferenceJoin.getJoinedTableReference().getTable() == table ) {
					return tableReferenceJoin.getJoinedTableReference();
				}
			}
		}

		return null;
	}

	@Override
	public void render(SqlAppender sqlAppender, SqlAstWalker walker) {
		// todo (6.0) : need to determine which table (if 2) to render first
		//		(think many-to-many).  does the order of the joins matter given the serial join?

		renderTableReference( collectionTableReference, sqlAppender, walker );

		if ( tableReferenceJoins != null && !tableReferenceJoins.isEmpty() ) {
			for ( TableReferenceJoin tableReferenceJoin : tableReferenceJoins ) {
				renderTableReferenceJoin( tableReferenceJoin, sqlAppender, walker );
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void renderTableReferenceJoin(
			TableReferenceJoin tableReferenceJoin,
			SqlAppender sqlAppender,
			SqlAstWalker walker) {
		sqlAppender.appendSql( " " );
		sqlAppender.appendSql( tableReferenceJoin.getJoinType().getText() );
		sqlAppender.appendSql( " join " );
		renderTableReference( tableReferenceJoin.getJoinedTableReference(), sqlAppender, walker );
		if ( tableReferenceJoin.getJoinPredicate() != null && !tableReferenceJoin.getJoinPredicate().isEmpty() ) {
			sqlAppender.appendSql( " on " );
			tableReferenceJoin.getJoinPredicate().accept( walker );
		}

	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// ColumnReference handling

	private final SortedMap<Column,ColumnReference> columnBindingMap = new TreeMap<>(
			(column1, column2) -> {
				// Sort primarily on table expression
				final int tableSort = column1.getSourceTable().getTableExpression().compareTo( column2.getSourceTable().getTableExpression() );
				if ( tableSort != 0 ) {
					return tableSort;
				}

				// and secondarily on column expression
				return column1.getExpression().compareTo( column2.getExpression() );
			}
	);

	@Override
	public ColumnReference resolveColumnReference(Column column) {
		final ColumnReference existing = columnBindingMap.get( column );
		if ( existing != null ) {
			return existing;
		}

		final TableReference tableBinding = locateTableReference( column.getSourceTable() );
		if ( tableBinding == null ) {
			throw new HibernateException(
					"Problem resolving Column(" + column.toLoggableString() +
							") to ColumnBinding via TableGroup [" + this + "]"
			);
		}
		final ColumnReference columnBinding = new ColumnReference( this, column );
		columnBindingMap.put( column, columnBinding );
		return columnBinding;
	}

	@Override
	public ColumnReference qualify(QualifiableSqlExpressable sqlSelectable) {
		// assume its a ColumnReference - we could also define `columnBindingMap` as keyed by QualifiableSqlExpressable
		assert sqlSelectable instanceof ColumnReference;
		columnBindingMap.get( sqlSelectable );
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public void applyAffectedTableNames(Consumer<String> nameCollector) {
		nameCollector.accept( collectionTableReference.getTable().getTableExpression() );
		for ( TableReferenceJoin tableReferenceJoin : tableReferenceJoins ) {
			nameCollector.accept( tableReferenceJoin.getJoinedTableReference().getTable().getTableExpression() );
		}
	}

	@Override
	public ColumnReference locateColumnReferenceByName(String name) {
		Column column = collectionTableReference.getTable().getColumn( name );

		if ( column == null ) {
			if ( tableReferenceJoins != null && !tableReferenceJoins.isEmpty() ) {
				for ( TableReferenceJoin tableReferenceJoin : tableReferenceJoins ) {
					column = tableReferenceJoin.getJoinedTableReference().getTable().getColumn( name );
					if ( column != null ) {
						break;
					}
				}
			}
		}

		if ( column == null ) {
			return null;
		}

		return resolveColumnReference( column );
	}
}
