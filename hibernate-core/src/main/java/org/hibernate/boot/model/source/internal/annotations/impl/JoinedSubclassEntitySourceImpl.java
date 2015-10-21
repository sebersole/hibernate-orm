/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.boot.model.source.internal.annotations.ColumnSource;
import org.hibernate.boot.model.source.internal.annotations.JoinedSubclassEntitySource;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PrimaryKeyJoinColumn;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;

/**
 * @author Strong Liu
 * @author Steve Ebersole
 */
public class JoinedSubclassEntitySourceImpl extends SubclassEntitySourceImpl implements JoinedSubclassEntitySource {
	private final List<ColumnSource> columnSources;
	private final JoinColumnResolutionDelegate fkColumnResolutionDelegate;

	public JoinedSubclassEntitySourceImpl(
			EntityTypeMetadata metadata,
			EntityHierarchySourceImpl hierarchy,
			IdentifiableTypeSourceAdapter superTypeSource) {
		super( metadata, hierarchy, superTypeSource );

		// todo : following normal annotation idiom for source, we probably want to move this stuff up to EntityClass...
		// todo : actually following the new paradigm we really want to move the interpretation of the join specific annotations here

		boolean hadNamedTargetColumnReferences = false;
		this.columnSources = new ArrayList<ColumnSource>();
		final List<String> targetColumnNames = new ArrayList<String>();
		if ( CollectionHelper.isNotEmpty( metadata.getJoinedSubclassPrimaryKeyJoinColumnSources() ) ) {
			for ( PrimaryKeyJoinColumn primaryKeyJoinColumnSource : metadata.getJoinedSubclassPrimaryKeyJoinColumnSources() ) {
				columnSources.add(
						new ColumnSourceImpl( primaryKeyJoinColumnSource )
				);
				targetColumnNames.add( primaryKeyJoinColumnSource.getReferencedColumnName() );
				if ( primaryKeyJoinColumnSource.getReferencedColumnName() != null ) {
					hadNamedTargetColumnReferences = true;
				}
			}
		}

		this.fkColumnResolutionDelegate = !hadNamedTargetColumnReferences
				? null
				: new JoinColumnResolutionDelegateImpl( targetColumnNames );
	}

	@Override
	public boolean isCascadeDeleteEnabled() {
		return getEntityClass().getOnDeleteAction() != null && getEntityClass().getOnDeleteAction() == OnDeleteAction.CASCADE;
	}

	@Override
	public ForeignKeyInformation getForeignKeyInformation() {
		return getEntityClass().getForeignKeyInformation();
	}

	@Override
	public JoinColumnResolutionDelegate getForeignKeyTargetColumnResolutionDelegate() {
		return fkColumnResolutionDelegate;
	}

	@Override
	public List<ColumnSource> getPrimaryKeyColumnSources() {
		return columnSources;
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
