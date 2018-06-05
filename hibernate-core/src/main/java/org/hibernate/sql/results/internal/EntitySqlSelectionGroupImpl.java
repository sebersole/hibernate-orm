/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.metamodel.model.domain.spi.DiscriminatorDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityHierarchy;
import org.hibernate.metamodel.model.domain.spi.EntityIdentifier;
import org.hibernate.metamodel.model.domain.spi.RowIdDescriptor;
import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.metamodel.model.domain.spi.TenantDiscrimination;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.results.spi.EntitySqlSelectionGroup;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.SqlSelection;

/**
 * @author Steve Ebersole
 */
public class EntitySqlSelectionGroupImpl extends AbstractSqlSelectionGroup implements EntitySqlSelectionGroup {

	public static EntitySqlSelectionGroup buildSqlSelectionGroup(
			EntityDescriptor<?> entityDescriptor,
			ColumnReferenceQualifier qualifier,
			QueryResultCreationContext creationContext) {
		return new Builder( entityDescriptor ).create( qualifier, creationContext );
	}


	private final List<SqlSelection> idSqlSelectionGroup;
	private final SqlSelection discriminatorSqlSelection;
	private final SqlSelection tenantDiscriminatorSqlSelection;
	private final SqlSelection rowIdSqlSelection;

	private EntitySqlSelectionGroupImpl(
			SqlSelection rowIdSqlSelection,
			List<SqlSelection> idSqlSelectionGroup,
			SqlSelection discriminatorSqlSelection,
			SqlSelection tenantDiscriminatorSqlSelection,
			Map<StateArrayContributor<?>, List<SqlSelection>> sqlSelectionsByContributor) {
		super( sqlSelectionsByContributor );
		this.rowIdSqlSelection = rowIdSqlSelection;
		this.idSqlSelectionGroup = idSqlSelectionGroup;
		this.discriminatorSqlSelection = discriminatorSqlSelection;
		this.tenantDiscriminatorSqlSelection = tenantDiscriminatorSqlSelection;
	}

	@Override
	public List<SqlSelection> getIdSqlSelections() {
		return idSqlSelectionGroup;
	}

	@Override
	public SqlSelection getDiscriminatorSqlSelection() {
		return discriminatorSqlSelection;
	}

	@Override
	public SqlSelection getTenantDiscriminatorSqlSelection() {
		return tenantDiscriminatorSqlSelection;
	}

	@Override
	public SqlSelection getRowIdSqlSelection() {
		return rowIdSqlSelection;
	}

	@SuppressWarnings({"UnusedReturnValue", "unchecked", "WeakerAccess"})
	public static class Builder {
		private final EntityDescriptor<?> entityDescriptor;

		private List<SqlSelection> idSqlSelections;
		private SqlSelection discriminatorSqlSelection;
		private SqlSelection tenantDiscriminatorSqlSelection;
		private SqlSelection rowIdSqlSelection;
		private Map<StateArrayContributor<?>, List<SqlSelection>> sqlSelectionsByContributor;

		public Builder(EntityDescriptor<?> entityDescriptor) {
			this.entityDescriptor = entityDescriptor;
		}

		public EntityDescriptor<?> getEntityDescriptor() {
			return entityDescriptor;
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Identifier

		protected void applyIdSqlSelections(
				EntityIdentifier identifierDescriptor,
				ColumnReferenceQualifier qualifier,
				QueryResultCreationContext creationContext) {
			applyIdSqlSelections( identifierDescriptor.resolveSqlSelections( qualifier, creationContext ) );
		}

		protected final void applyIdSqlSelections(Collection<SqlSelection> idSqlSelectionGroup) {
			if ( this.idSqlSelections != null ) {
				throw new HibernateException( "Multiple calls to set entity id SqlSelections" );
			}
			this.idSqlSelections = asList( idSqlSelectionGroup );
		}

		private List<SqlSelection> asList(Collection<SqlSelection> selections) {
			if ( selections instanceof List ) {
				return (List<SqlSelection>) selections;
			}
			else {
				return new ArrayList<>( selections );
			}
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Discriminator

		protected void applyDiscriminatorSqlSelection(
				DiscriminatorDescriptor discriminatorDescriptor,
				ColumnReferenceQualifier qualifier,
				QueryResultCreationContext creationContext) {
			applyDiscriminatorSqlSelection( discriminatorDescriptor.resolveSqlSelection( qualifier, creationContext ) );
		}

		protected final void applyDiscriminatorSqlSelection(SqlSelection discriminatorSqlSelection) {
			if ( this.discriminatorSqlSelection != null ) {
				throw new HibernateException( "Multiple calls to set entity discriminator SqlSelection" );
			}
			this.discriminatorSqlSelection = discriminatorSqlSelection;
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Tenant-discriminator

		protected void applyTenantDiscriminatorSqlSelection(
				TenantDiscrimination tenantDiscrimination,
				ColumnReferenceQualifier qualifier,
				QueryResultCreationContext creationContext) {
			applyTenantDiscriminatorSqlSelection(
					creationContext.getSqlSelectionResolver().resolveSqlSelection(
							creationContext.getSqlSelectionResolver().resolveSqlExpression(
									qualifier,
									tenantDiscrimination.getBoundColumn()
							)
					)
			);
		}

		protected final void applyTenantDiscriminatorSqlSelection(SqlSelection tenantDiscriminatorSqlSelection) {
			if ( this.tenantDiscriminatorSqlSelection != null ) {
				throw new HibernateException( "Multiple calls to set entity tenant-discriminator SqlSelection" );
			}
			this.tenantDiscriminatorSqlSelection = tenantDiscriminatorSqlSelection;
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Row-id

		protected void applyRowIdSqlSelection(
				RowIdDescriptor rowIdDescriptor,
				ColumnReferenceQualifier qualifier,
				QueryResultCreationContext creationContext) {
			applyRowIdSqlSelection(
					creationContext.getSqlSelectionResolver().resolveSqlSelection(
							creationContext.getSqlSelectionResolver().resolveSqlExpression(
									qualifier,
									rowIdDescriptor.getBoundColumn()
							)
					)
			);
		}

		protected final void applyRowIdSqlSelection(SqlSelection rowIdSqlSelection) {
			this.rowIdSqlSelection = rowIdSqlSelection;
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// State-array-contributors

		protected void applyContributorSqlSelections(
				StateArrayContributor<?> contributor,
				ColumnReferenceQualifier qualifier,
				QueryResultCreationContext creationContext) {
			applyContributorSqlSelections(
					contributor,
					contributor.resolveSqlSelections(
							qualifier,
							creationContext
					)
			);
		}

		protected final void applyContributorSqlSelections(StateArrayContributor<?> contributor, Collection<SqlSelection> sqlSelections) {
			if ( sqlSelectionsByContributor == null ) {
				sqlSelectionsByContributor = new HashMap<>();
			}
			sqlSelectionsByContributor.put( contributor, asList( sqlSelections ) );
		}

		public EntitySqlSelectionGroupImpl create(ColumnReferenceQualifier qualifier, QueryResultCreationContext creationContext) {
			final EntityHierarchy hierarchy = entityDescriptor.getHierarchy();

			applyIdSqlSelections(
					hierarchy.getIdentifierDescriptor(),
					qualifier,
					creationContext
			);

			if ( hierarchy.getDiscriminatorDescriptor() != null ) {
				applyDiscriminatorSqlSelection(
						hierarchy.getDiscriminatorDescriptor(),
						qualifier,
						creationContext
				);
			}

			if ( hierarchy.getTenantDiscrimination() != null ) {
				applyTenantDiscriminatorSqlSelection(
						hierarchy.getTenantDiscrimination(),
						qualifier,
						creationContext
				);
			}

			if ( hierarchy.getRowIdDescriptor() != null ) {
				applyRowIdSqlSelection(
						hierarchy.getRowIdDescriptor(),
						qualifier,
						creationContext
				);
			}

			for ( StateArrayContributor<?> contributor : entityDescriptor.getStateArrayContributors() ) {
				applyContributorSqlSelections(
						contributor,
						qualifier,
						creationContext
				);
			}

			return new EntitySqlSelectionGroupImpl(
					rowIdSqlSelection,
					idSqlSelections,
					discriminatorSqlSelection,
					tenantDiscriminatorSqlSelection,
					sqlSelectionsByContributor
			);
		}
	}
}
