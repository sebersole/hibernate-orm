/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.function.BiConsumer;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.BasicSingularAttribute;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.MappingType;
import org.hibernate.metamodel.mapping.StateArrayContributorMetadata;
import org.hibernate.metamodel.mapping.StateArrayContributorMetadataAccess;
import org.hibernate.metamodel.mapping.internal.AbstractVirtualAttribute;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.basic.BasicFetch;
import org.hibernate.sql.results.graph.basic.BasicResult;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.MutabilityPlan;

/**
 * Basic attribute defined as part of a composite-key
 *
 * @author Steve Ebersole
 */
public class VirtualBasicAttribute extends AbstractVirtualAttribute implements BasicSingularAttribute {

	private final boolean nullable;
	private final BasicValueConverter<?,?> converter;

	private final String tableName;
	private final String columnName;
	private final JdbcMapping jdbcMapping;

	public VirtualBasicAttribute(
			NavigableRole navigableRole,
			boolean nullable,
			FetchOptions fetchOptions,
			BasicValueConverter<?,?> converter,
			String tableName,
			String columnName,
			JdbcMapping jdbcMapping,
			ManagedMappingType declaringType,
			int position,
			PropertyAccess propertyAccess,
			@SuppressWarnings("unused") MappingModelCreationProcess creationProcess) {
		super( navigableRole, fetchOptions, propertyAccess, declaringType, position );

		this.nullable = nullable;
		this.converter = converter;
		this.tableName = tableName;
		this.columnName = columnName;
		this.jdbcMapping = jdbcMapping;
	}

	@Override
	public JavaTypeDescriptor<?> getJavaTypeDescriptor() {
		return jdbcMapping.getJavaTypeDescriptor();
	}

	@Override
	public MappingType getMappedTypeDescriptor() {
		return jdbcMapping;
	}

	@Override
	public String getContainingTableExpression() {
		return tableName;
	}

	@Override
	public String getMappedColumnExpression() {
		return columnName;
	}

	@Override
	public JdbcMapping getJdbcMapping() {
		return jdbcMapping;
	}

	@Override
	public BasicValueConverter<?,?> getValueConverter() {
		return converter;
	}

	@Override
	public String getFetchableName() {
		return getAttributeName();
	}

	@Override
	public Fetch generateFetch(
			FetchParent fetchParent,
			NavigablePath fetchablePath,
			FetchTiming fetchTiming,
			boolean selected,
			LockMode lockMode,
			String resultVariable,
			DomainResultCreationState creationState) {
		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
		final SessionFactoryImplementor sessionFactory = sqlAstCreationState.getCreationContext().getSessionFactory();

		final FromClauseAccess fromClauseAccess = sqlAstCreationState.getFromClauseAccess();
		final TableGroup tableGroup = fromClauseAccess.getTableGroup( fetchParent.getNavigablePath() );
		final TableReference tableReference = tableGroup.getTableReference( tableName );

		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();
		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
				sqlExpressionResolver.resolveSqlExpression(
						SqlExpressionResolver.createColumnReferenceKey( tableReference, columnName ),
						processingState -> new ColumnReference(
								tableReference,
								columnName,
								jdbcMapping,
								sessionFactory
						)
				),
				getJavaTypeDescriptor(),
				sessionFactory.getTypeConfiguration()
		);

		//noinspection rawtypes
		return new BasicFetch(
				sqlSelection.getValuesArrayPosition(),
				fetchParent,
				fetchablePath,
				this,
				nullable,
				converter,
				fetchTiming,
				creationState
		);
	}

	@Override
	public void applySqlSelections(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			DomainResultCreationState creationState) {
		applySqlSelections(
				navigablePath,
				tableGroup,
				creationState,
				(sqlSelection, jdbcMapping) -> {}
		);
	}

	@Override
	public void applySqlSelections(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			DomainResultCreationState creationState,
			BiConsumer<SqlSelection, JdbcMapping> selectionConsumer) {
		final SqlSelection sqlSelection = generateSqlSelection( tableGroup, creationState );

		selectionConsumer.accept( sqlSelection, jdbcMapping );
	}

	private SqlSelection generateSqlSelection(
			TableGroup tableGroup,
			DomainResultCreationState creationState) {
		final SqlExpressionResolver expressionResolver = creationState.getSqlAstCreationState().getSqlExpressionResolver();
		final TableReference tableReference = tableGroup.resolveTableReference( getContainingTableExpression() );

		return expressionResolver.resolveSqlSelection(
					expressionResolver.resolveSqlExpression(
							SqlExpressionResolver.createColumnReferenceKey(
									tableReference,
									getMappedColumnExpression()
							),
							sqlAstProcessingState -> new ColumnReference(
									tableReference.getIdentificationVariable(),
									getMappedColumnExpression(),
									getJdbcMapping(),
									creationState.getSqlAstCreationState().getCreationContext().getSessionFactory()
							)
					),
					converter == null ?
							getMappedTypeDescriptor().getMappedJavaTypeDescriptor() :
							converter.getRelationalJavaDescriptor(),
					creationState.getSqlAstCreationState().getCreationContext().getDomainModel().getTypeConfiguration()
			);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public <T> DomainResult<T> createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			String resultVariable,
			DomainResultCreationState creationState) {
		final SqlSelection sqlSelection = generateSqlSelection( tableGroup, creationState );

		return new BasicResult(
				sqlSelection.getValuesArrayPosition(),
				resultVariable,
				getJavaTypeDescriptor(),
				getValueConverter(),
				navigablePath
		);
	}

	@Override
	protected boolean isNullable() {
		return nullable;
	}
}
