/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.metamodel.spi.EmbeddableRepresentationStrategy;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.sql.SqmToSqlAstConverter;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.SqlAliasBaseGenerator;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.SqlTuple;
import org.hibernate.sql.ast.tree.from.CompositeTableGroup;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableGroupJoin;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.embeddable.internal.EmbeddableFetchImpl;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class VirtualToOneKeyComposite
		extends AbstractToOneKey
		implements ToOneKey, KeyModelPartComposite, FetchOptions {
	private final EmbeddableMappingType embeddable;

	private final String tableName;
	private final List<String> columnNames;
	private final List<JdbcMapping> jdbcMappings;

	private final FetchStyle fetchStyle;
	private final FetchTiming fetchTiming;
	private final boolean nullable;

	public VirtualToOneKeyComposite(
			ToOneAttributeMapping attributeMapping,
			EmbeddableRepresentationStrategy representationStrategy,
			String tableName,
			List<String> columnNames,
			List<JdbcMapping> jdbcMappings,
			FetchStyle fetchStyle,
			FetchTiming fetchTiming,
			boolean nullable,
			KeyModelPart owningSide,
			MappingModelCreationProcess creationProcess) {
		super( attributeMapping );
		this.tableName = tableName;
		this.columnNames = columnNames;
		this.jdbcMappings = jdbcMappings;
		this.fetchStyle = fetchStyle;
		this.fetchTiming = fetchTiming;
		this.nullable = nullable;

		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
		final List<AttributeMapping> attributesCopy = new ArrayList<>();

		// todo (6.0) : populate this `attributesCopy` list

		this.embeddable = new EmbeddableMappingType(
				representationStrategy,
				embeddableMappingType -> attributesCopy,
				embeddableMappingType -> this,
				sessionFactory
		);

		owningSide.registerForeignKeyInitializationListener( this::setForeignKey );
	}

	@Override
	public ForeignKeyComposite getForeignKeyDescriptor() {
		return (ForeignKeyComposite) super.getForeignKeyDescriptor();
	}

	@Override
	public EmbeddableMappingType getEmbeddableTypeDescriptor() {
		return embeddable;
	}

	@Override
	public String getContainingTableExpression() {
		return tableName;
	}

	@Override
	public List<String> getMappedColumnExpressions() {
		return columnNames;
	}

	@Override
	public <T> DomainResult<T> createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			String resultVariable,
			DomainResultCreationState creationState) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	@Override
	public void applySqlSelections(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			DomainResultCreationState creationState) {
		applySqlSelections( navigablePath, tableGroup, creationState, (sqlSelection, jdbcMapping) -> {} );
	}

	@Override
	public void applySqlSelections(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			DomainResultCreationState creationState,
			BiConsumer<SqlSelection, JdbcMapping> selectionConsumer) {
		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
		final SessionFactoryImplementor sessionFactory = sqlAstCreationState.getCreationContext().getSessionFactory();
		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();

		final TableReference tableReference = tableGroup.getTableReference( tableName );

		visitColumns(
				(containingTableExpression, columnExpression, jdbcMapping) -> {
					assert containingTableExpression.equals( tableName );

					final Expression expression = sqlExpressionResolver.resolveSqlExpression(
							SqlExpressionResolver.createColumnReferenceKey( tableReference, columnExpression ),
							processingState -> new ColumnReference(
									tableReference,
									columnExpression,
									jdbcMapping,
									sessionFactory
							)
					);

					final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
							expression,
							getJavaTypeDescriptor(),
							sessionFactory.getTypeConfiguration()
					);

					selectionConsumer.accept( sqlSelection, jdbcMapping );
				}
		);
	}

	@Override
	public void visitColumns(ColumnConsumer consumer) {
		for ( int i = 0; i < columnNames.size(); i++ ) {
			consumer.accept( tableName, columnNames.get( i ), jdbcMappings.get( i ) );
		}
	}

	@Override
	public Expression toSqlExpression(
			TableGroup tableGroup,
			Clause clause,
			SqmToSqlAstConverter walker,
			SqlAstCreationState sqlAstCreationState) {
		final SessionFactoryImplementor sessionFactory = sqlAstCreationState.getCreationContext().getSessionFactory();
		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();

		final TableReference tableReference = tableGroup.getTableReference( tableName );

		final List<Expression> subExpressions = new ArrayList<>( columnNames.size() );

		visitColumns(
				(containingTableExpression, columnExpression, jdbcMapping) -> {
					assert containingTableExpression.equals( tableName );

					final Expression expression = sqlExpressionResolver.resolveSqlExpression(
							SqlExpressionResolver.createColumnReferenceKey( tableReference, columnExpression ),
							processingState -> new ColumnReference(
									tableReference,
									columnExpression,
									jdbcMapping,
									sessionFactory
							)
					);

					subExpressions.add( expression );
				}
		);

		return new SqlTuple( subExpressions, this );
	}

	@Override
	public TableGroupJoin createTableGroupJoin(
			NavigablePath navigablePath,
			TableGroup lhs,
			String explicitSourceAlias,
			SqlAstJoinType sqlAstJoinType,
			LockMode lockMode,
			SqlAliasBaseGenerator aliasBaseGenerator,
			SqlExpressionResolver sqlExpressionResolver,
			SqlAstCreationContext creationContext) {
		final CompositeTableGroup compositeTableGroup = new CompositeTableGroup( navigablePath, this, lhs );
		return new TableGroupJoin(
				navigablePath,
				sqlAstJoinType,
				compositeTableGroup
		);
	}

	@Override
	public String getSqlAliasStem() {
		return getAttributeMapping().getAttributeName();
	}

	@Override
	public FetchOptions getMappedFetchOptions() {
		return this;
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
		return new EmbeddableFetchImpl(
				fetchablePath,
				this,
				fetchParent,
				fetchTiming,
				selected,
				nullable,
				creationState
		);
	}

	@Override
	public int getNumberOfFetchables() {
		return embeddable.getNumberOfFetchables();
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return embeddable.getJavaTypeDescriptor();
	}

	@Override
	public EntityMappingType findContainingEntityMapping() {
		return embeddable.findContainingEntityMapping();
	}

	@Override
	public FetchStyle getStyle() {
		return fetchStyle;
	}

	@Override
	public FetchTiming getTiming() {
		return fetchTiming;
	}

	@Override
	public ModelPart findSubPart(String name, EntityMappingType treatTargetType) {
		return embeddable.findSubPart( name, treatTargetType );
	}

	@Override
	public void visitSubParts(Consumer<ModelPart> consumer, EntityMappingType treatTargetType) {
		embeddable.visitSubParts( consumer, treatTargetType );
	}

	@Override
	public EmbeddableMappingType getPartMappingType() {
		return embeddable;
	}

	@Override
	public SingularAttributeMapping getParentInjectionAttributeMapping() {
		return null;
	}
}
