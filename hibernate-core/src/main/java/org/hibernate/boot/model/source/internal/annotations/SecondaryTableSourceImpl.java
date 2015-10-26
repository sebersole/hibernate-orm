/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.model.CustomSql;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.Column;
import org.hibernate.boot.model.source.spi.ColumnSource;
import org.hibernate.boot.model.source.spi.SecondaryTableSource;
import org.hibernate.boot.model.source.spi.TableSpecificationSource;
import org.hibernate.engine.FetchStyle;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;

/**
 * @author Steve Ebersole
 */
public class SecondaryTableSourceImpl implements SecondaryTableSource {
	private final TableSourceImpl joinTable;
	private final List<ColumnSource> columnSources;
	private final JoinColumnResolutionDelegate fkColumnResolutionDelegate;

	public SecondaryTableSourceImpl(
			TableSourceImpl joinTable,
			List<? extends Column> joinColumns) {
		this.joinTable = joinTable;

		// todo : following normal annotation idiom for source, we probably want to move this stuff up to EntityClass...
		columnSources = new ArrayList<ColumnSource>();
		final List<String> targetColumnNames = new ArrayList<String>();
		boolean hadNamedTargetColumnReferences = false;
		for ( Column joinColumn : joinColumns ) {
			columnSources.add(
					new ColumnSourceImpl(
							joinColumn
					)
			);
			targetColumnNames.add( joinColumn.getReferencedColumnName() );
			if ( joinColumn.getReferencedColumnName() != null ) {
				hadNamedTargetColumnReferences = true;
			}
		}

		this.fkColumnResolutionDelegate = ! hadNamedTargetColumnReferences
				? null
				: new JoinColumnResolutionDelegateImpl( targetColumnNames );
	}

	@Override
	public TableSpecificationSource getTableSource() {
		return joinTable;
	}

	@Override
	public List<ColumnSource> getPrimaryKeyColumnSources() {
		return columnSources;
	}

	@Override
	public String getLogicalTableNameForContainedColumns() {
		return joinTable.getExplicitTableName();
	}

	@Override
	public String getComment() {
		return null;
	}

	@Override
	public FetchStyle getFetchStyle() {
		return null;
	}

	@Override
	public boolean isInverse() {
		return false;
	}

	@Override
	public boolean isOptional() {
		return true;
	}

	@Override
	public boolean isCascadeDeleteEnabled() {
		return false;
	}

	@Override
	public CustomSql getCustomSqlDelete() {
		return null;
	}

	@Override
	public CustomSql getCustomSqlInsert() {
		return null;
	}

	@Override
	public CustomSql getCustomSqlUpdate() {
		return null;
	}

	@Override
	public ForeignKeyInformation getForeignKeyInformation() {
		return null;
	}

	@Override
	public JoinColumnResolutionDelegate getForeignKeyTargetColumnResolutionDelegate() {
		return fkColumnResolutionDelegate;
	}

	private static class JoinColumnResolutionDelegateImpl implements JoinColumnResolutionDelegate {
		private final List<String> targetColumnNames;

		private JoinColumnResolutionDelegateImpl(List<String> targetColumnNames) {
			this.targetColumnNames = targetColumnNames;
		}

		@Override
		public List<? extends Selectable> getJoinColumns(JoinColumnResolutionContext context) {
			List<Selectable> columns = new ArrayList<Selectable>();
			for ( String name : targetColumnNames ) {
				// the nulls represent table, schema and catalog name which are ignored anyway...
				columns.add( context.resolveColumn( name, null, null, null ) );
			}
			return columns;
		}

		@Override
		public Table getReferencedTable(JoinColumnResolutionContext context) {
			return context.resolveTable( null, null, null );
		}

		@Override
		public String getReferencedAttributeName() {
			return null;
		}

	}
}
