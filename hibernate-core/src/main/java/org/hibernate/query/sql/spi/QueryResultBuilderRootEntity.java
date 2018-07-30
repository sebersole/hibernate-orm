/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.query.sql.spi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.QueryException;
import org.hibernate.metamodel.model.domain.spi.DiscriminatorDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityIdentifier;
import org.hibernate.metamodel.model.domain.spi.RowIdDescriptor;
import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.metamodel.model.domain.spi.TenantDiscrimination;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.metamodel.model.relational.spi.Table;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.produce.spi.QualifiableSqlExpressable;
import org.hibernate.sql.ast.tree.spi.expression.ColumnReference;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.ast.tree.spi.from.TableReference;
import org.hibernate.sql.results.internal.AbstractFetchParent;
import org.hibernate.sql.results.internal.EntitySqlSelectionGroupImpl;
import org.hibernate.sql.results.spi.EntityQueryResult;
import org.hibernate.sql.results.spi.InitializerCollector;
import org.hibernate.sql.results.spi.QueryResultAssembler;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.SqlAstCreationContext;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * Builds an entity-based QueryResult for a NativeQuery
 *
 * @author Steve Ebersole
 */
public class QueryResultBuilderRootEntity
		implements NativeQuery.RootReturn, WrappableQueryResultBuilder, ColumnReferenceQualifier {
	private final String tableAlias;
	private final EntityDescriptor entityDescriptor ;
	private LockMode lockMode = LockMode.READ;

	private EntitySqlSelectionGroupOverridableBuilder sqlSelectionGroupBuilder;
	private List<String> idColumnAliases;
	private String discriminatorColumnAlias;

	private Map<String, AttributeMapping> propertyMappings;

	public QueryResultBuilderRootEntity(String tableAlias, EntityDescriptor entityDescriptor ) {
		this.tableAlias = tableAlias;
		this.entityDescriptor = entityDescriptor;
//		sqlSelectionGroupBuilder = new EntitySqlSelectionGroupOverridableBuilder(  );
	}

	@Override
	public JavaTypeDescriptor getResultType() {
		return entityDescriptor.getJavaTypeDescriptor();
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// NativeQuery.RootReturn

	public NativeQuery.RootReturn setLockMode(LockMode lockMode) {
		this.lockMode = lockMode;
		return this;
	}

	@Override
	public NativeQuery.RootReturn addIdColumnAliases(String... aliases) {
		if ( aliases != null ) {
			if ( aliases.length == 1 ) {
				idColumnAliases.add( aliases[0] );
			}
			else {
				idColumnAliases.addAll( Arrays.asList( aliases ) );
			}
		}

		return this;
	}

	public NativeQuery.RootReturn setDiscriminatorAlias(String alias) {
		this.discriminatorColumnAlias = alias;
		return this;
	}

	public NativeQuery.RootReturn addProperty(String propertyName, String columnAlias) {
		addProperty( propertyName ).addColumnAlias( columnAlias );
		return this;
	}

	public NativeQuery.ReturnProperty addProperty(final String propertyName) {
		if ( propertyMappings == null ) {
			propertyMappings = new HashMap<>();
		}

		return new NativeQuery.ReturnProperty() {
			public NativeQuery.ReturnProperty addColumnAlias(String columnAlias) {
				final AttributeMapping registration = propertyMappings.computeIfAbsent(
						propertyName,
						AttributeMapping::new
				);
				registration.addColumnAlias( columnAlias );
				return this;
			}
		};
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// NativeQueryReturnBuilder

	@Override
	public EntityQueryResult buildReturn(QueryResultCreationContext creationContext) {
		return new EntityQueryResultImpl(
				entityDescriptor,
				this,
				// todo (6.0) - is `tableAlias` the correct thing here?
				//		this is supposed to be the "query result variable" associated with this QueryResult
				//		- is that the intention of `tableAlias`?
				tableAlias,
				idColumnAliases,
				discriminatorColumnAlias,
				propertyMappings,
				lockMode,
				creationContext
		);
	}

	@Override
	public String getUniqueIdentifier() {
		return tableAlias;
	}

	@Override
	public TableReference locateTableReference(Table table) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public ColumnReference resolveColumnReference(Column column) {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public Expression qualify(QualifiableSqlExpressable sqlSelectable) {
		throw new NotYetImplementedFor6Exception(  );
	}

	public static class EntityQueryResultImpl extends AbstractFetchParent implements EntityQueryResult {
		private final EntityDescriptor entityDescriptor;
		private final String queryResultVariable;

//		private final EntityRootInitializer initializer;
//		private final EntityQueryResultAssembler assembler;

		public EntityQueryResultImpl(
				EntityDescriptor entityDescriptor,
				ColumnReferenceQualifier qualifier,
				String queryResultVariable,
				List<String> explicitIdColumnAliases,
				String explicitDiscriminatorColumnAlias,
				Map<String, AttributeMapping> explicitAttributeMapping,
				LockMode lockMode,
				QueryResultCreationContext creationContext) {
			super( null, new NavigablePath( entityDescriptor.getEntityName() ) );

			this.entityDescriptor = entityDescriptor;
			this.queryResultVariable = queryResultVariable;



//			this.initializer = new EntityRootInitializer(
//					entityDescriptor,
//					EntitySqlSelectionMappingsOverridableBuilder(
//							entityDescriptor,
//							qualifier,
//							// row-id
//							null,
//							explicitIdColumnAliases,
//							explicitDiscriminatorColumnAlias,
//							// tenant-discriminator
//							null,
//							explicitAttributeMapping,
//							resolutionContext
//					),
//					lockMode,
//					false
//			);
//
//			this.assembler = new EntityQueryResultAssembler(
//					entityDescriptor.getJavaTypeDescriptor(),
//					initializer
//			);
		}

		@Override
		public EntityDescriptor getEntityDescriptor() {
			return entityDescriptor;
		}

		@Override
		public String getResultVariable() {
			return queryResultVariable;
		}

		@Override
		public void registerInitializers(InitializerCollector collector) {
			throw new NotYetImplementedFor6Exception(  );
		}

		@Override
		public QueryResultAssembler getResultAssembler() {
			throw new NotYetImplementedFor6Exception( getClass() );
//			return assembler;
		}


//		@Override
//		public QueryResultAssembler getResultAssembler() {
//			return assembler;
//		}

//		@Override
//		public void registerInitializers(InitializerCollector collector) {
//			collector.addInitializer( initializer );
//			registerFetchInitializers( initializer, collector );
//		}
	}

	// todo (6.0 - need some form of SqlSelection, etc distinctions here to support duplicated columns - including fetches (potential duplicated unqualified column name which need to  be unique).

	private static class EntitySqlSelectionGroupOverridableBuilder extends EntitySqlSelectionGroupImpl.Builder {
		private final List<String> explicitIdColumnAliases;
		private final String explicitDiscriminatorColumnAlias;
		private final String explicitTenantDiscriminatorColumnAlias;
		private final String explicitRowIdColumnAlias;
		private final Map<String, AttributeMapping> explicitAttributeMapping;


		public EntitySqlSelectionGroupOverridableBuilder(
				EntityDescriptor entityDescriptor,
				ColumnReferenceQualifier qualifier,
				String explicitRowIdColumnAlias,
				List<String> explicitIdColumnAliases,
				String explicitDiscriminatorColumnAlias,
				String explicitTenantDiscriminatorColumnAlias,
				Map<String, AttributeMapping> explicitAttributeMapping,
				QueryResultCreationContext creationContext) {
			super( entityDescriptor );
			this.explicitRowIdColumnAlias = explicitRowIdColumnAlias;
			this.explicitIdColumnAliases = explicitIdColumnAliases;
			this.explicitDiscriminatorColumnAlias = explicitDiscriminatorColumnAlias;
			this.explicitTenantDiscriminatorColumnAlias = explicitTenantDiscriminatorColumnAlias;
			this.explicitAttributeMapping = explicitAttributeMapping;
		}

		@Override
		protected void applyIdSqlSelections(
				EntityIdentifier identifierDescriptor,
				ColumnReferenceQualifier qualifier,
				SqlAstCreationContext creationContext) {
			if ( explicitIdColumnAliases == null || explicitIdColumnAliases.isEmpty() ) {
				super.applyIdSqlSelections( identifierDescriptor, qualifier, creationContext );
				return;
			}

			// user explicitly mapped the id column - use that explicit info

			// make sure they gave us the right number of columns...
			if ( explicitIdColumnAliases.size() != identifierDescriptor.getColumns().size() ) {
				throw new QueryException(
						String.format(
								Locale.ROOT,
								"NativeQuery result-set-mapping included explicit id mapping for entity [%s] " +
										"but explicit mapping defined %s columns while the entity id defines %s",
								getEntityDescriptor().getEntityName(),
								explicitIdColumnAliases.size(),
								identifierDescriptor.getColumns().size()
						)
				);
			}

			// todo (6.0) : hook in the explicit mapping bits specified by the user
			throw new NotYetImplementedFor6Exception();
		}

		@Override
		protected void applyContributorSqlSelections(
				StateArrayContributor<?> contributor,
				ColumnReferenceQualifier qualifier,
				SqlAstCreationContext creationContext) {
			// todo (6.0) : hook in the explicit mapping bits specified by the user
			super.applyContributorSqlSelections( contributor, qualifier, creationContext );
		}


		@Override
		protected void applyDiscriminatorSqlSelection(
				DiscriminatorDescriptor discriminatorDescriptor,
				ColumnReferenceQualifier qualifier,
				SqlAstCreationContext creationContext) {
			// todo (6.0) : hook in the explicit mapping bits specified by the user
			super.applyDiscriminatorSqlSelection( discriminatorDescriptor, qualifier, creationContext );
		}

		@Override
		protected void applyTenantDiscriminatorSqlSelection(
				TenantDiscrimination tenantDiscrimination,
				ColumnReferenceQualifier qualifier,
				SqlAstCreationContext creationContext) {
			// todo (6.0) : hook in the explicit mapping bits specified by the user
			super.applyTenantDiscriminatorSqlSelection( tenantDiscrimination, qualifier, creationContext );
		}

		@Override
		protected void applyRowIdSqlSelection(
				RowIdDescriptor rowIdDescriptor,
				ColumnReferenceQualifier qualifier,
				SqlAstCreationContext creationContext) {
			// todo (6.0) : hook in the explicit mapping bits specified by the user
			super.applyRowIdSqlSelection( rowIdDescriptor, qualifier, creationContext );
		}

	}
}
