/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.ToOne;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.MappingModelCreationException;
import org.hibernate.metamodel.mapping.MappingType;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.sql.SqmToSqlAstConverter;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.SqlAliasBaseGenerator;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.expression.SqlTuple;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableGroupJoin;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Composite-valued ToOneKey representing the target side of the association.
 *
 * @implSpec Decided to split into referring/target specific impls to avoid
 * run-time if-checks in the method impls
 *
 * @author Steve Ebersole
 */
public class ToOneKeyCompositeTarget extends AbstractToOneKey implements ToOneKey, KeyModelPartComposite {
	private final EmbeddableMappingType embeddable;

	public ToOneKeyCompositeTarget(
			ToOne bootValue,
			ToOneAttributeMapping toOneAttribute,
			MappingModelCreationProcess creationProcess) {
		super( toOneAttribute );

		// for a to-one to be the target side we should a one-to-one or logical one-to-one
		if ( toOneAttribute.getCardinality() == ToOneAttributeMapping.Cardinality.MANY_TO_ONE ) {
			throw new MappingModelCreationException( "Target-side of a to-one association must be a real or logical one-to-one" );
		}

		// todo (6.0) : figure out handling for logical one-to-one as the target for a bi-directional association
		//		- for now, not implemented

		if ( toOneAttribute.getCardinality() == ToOneAttributeMapping.Cardinality.LOGICAL_ONE_TO_ONE ) {
			throw new NotYetImplementedFor6Exception( getClass() );
		}

		// NOTE : many of these checks extend from the (temp) logical one-to-one statement above
		assert bootValue instanceof OneToOne;
		assert ! ( (OneToOne) bootValue ).isConstrained();

		final String mappedByProperty = ( (OneToOne) bootValue ).getMappedByProperty();
		assert mappedByProperty != null;

		final EntityMappingType associatedEntityType = toOneAttribute.getAssociatedEntityMappingType();

		creationProcess.registerSubPartGroupInitializationListener(
				toOneAttribute.getAssociatedEntityMappingType(),
				MappingModelCreationProcess.SubPartGroup.NORMAL,
				() -> {
					final ModelPart referencedPart = associatedEntityType.findAttributeMapping( mappedByProperty );
					assert referencedPart instanceof ToOneAttributeMapping;
					final ToOneAttributeMapping mappedByAttr = (ToOneAttributeMapping) referencedPart;
					mappedByAttr.getKeyModelPart().registerForeignKeyInitializationListener( this::setForeignKey );
				}
		);

		embeddable = null;
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	@Override
	public int getNumberOfFetchables() {
		// the composite itself
		return 1;
	}

	@Override
	public ForeignKeyComposite getForeignKeyDescriptor() {
		return (ForeignKeyComposite) super.getForeignKeyDescriptor();
	}

	@Override
	public String getContainingTableExpression() {
		return getForeignKeyDescriptor().getTargetSide().getTableName();
	}

	@Override
	public EmbeddableMappingType getEmbeddableTypeDescriptor() {
		return embeddable;
	}

	@Override
	public List<String> getMappedColumnExpressions() {
		return getForeignKeyDescriptor().getTargetSide().getColumnNames();
	}

	@Override
	public SingularAttributeMapping getParentInjectionAttributeMapping() {
		return null;
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
	public Expression toSqlExpression(
			TableGroup tableGroup,
			Clause clause,
			SqmToSqlAstConverter walker,
			SqlAstCreationState sqlAstCreationState) {
		final TypeConfiguration typeConfiguration = sqlAstCreationState.getCreationContext()
				.getSessionFactory()
				.getTypeConfiguration();
		final List<ColumnReference> columnReferences = new ArrayList<>( getJdbcTypeCount( typeConfiguration ) );

		final TableReference tableReference = tableGroup.getTableReference( getContainingTableExpression() );
		assert tableReference != null;

		final SqlExpressionResolver expressionResolver = sqlAstCreationState.getSqlExpressionResolver();

		visitColumns(
				(containingTableExpression, columnExpression, jdbcMapping) -> {
					final ColumnReference expression = (ColumnReference) expressionResolver.resolveSqlExpression(
							SqlExpressionResolver.createColumnReferenceKey( tableReference, columnExpression ),
							processingState -> new ColumnReference(
									tableReference,
									columnExpression,
									jdbcMapping
							)
					);

					columnReferences.add( expression );
				}
		);

		return new SqlTuple( columnReferences, embeddable );
	}

	@Override
	public JavaTypeDescriptor<?> getJavaTypeDescriptor() {
		return embeddable.getJavaTypeDescriptor();
	}

	@Override
	public MappingType getPartMappingType() {
		return embeddable;
	}

	@Override
	public void visitColumns(ColumnConsumer consumer) {
		final int count = getMappedColumnExpressions().size();

		final List<JdbcMapping> jdbcMappings = getForeignKeyDescriptor().getTargetSide().getJdbcMappings();
		assert jdbcMappings.size() == count;

		for ( int i = 0; i < count; i++ ) {
			final String columnName = getMappedColumnExpressions().get( i );
			final JdbcMapping jdbcMapping = jdbcMappings.get( i );
			consumer.accept( getContainingTableExpression(), columnName, jdbcMapping );
		}
	}

	@Override
	public List<JdbcMapping> getJdbcMappings(TypeConfiguration typeConfiguration) {
		return getForeignKeyDescriptor().getTargetSide().getJdbcMappings();
	}

	@Override
	public int getJdbcTypeCount(TypeConfiguration typeConfiguration) {
		return getForeignKeyDescriptor().getTargetSide().getColumnNames().size();
	}

	@Override
	public void visitJdbcTypes(
			Consumer<JdbcMapping> action,
			Clause clause,
			TypeConfiguration typeConfiguration) {
		visitJdbcTypes( action, typeConfiguration );
	}

	@Override
	public void visitJdbcTypes(Consumer<JdbcMapping> action, TypeConfiguration typeConfiguration) {
		getForeignKeyDescriptor().getTargetSide().getJdbcMappings().forEach( action );
	}

	@Override
	public Object disassemble(Object value, SharedSessionContractImplementor session) {
		return embeddable.disassemble( value, session );
	}

	@Override
	public void visitDisassembledJdbcValues(
			Object value,
			Clause clause,
			JdbcValuesConsumer valuesConsumer,
			SharedSessionContractImplementor session) {
		embeddable.visitDisassembledJdbcValues( value, clause, valuesConsumer, session );
	}

	@Override
	public void visitJdbcValues(
			Object value,
			Clause clause,
			JdbcValuesConsumer valuesConsumer,
			SharedSessionContractImplementor session) {
		embeddable.visitJdbcValues( value, clause, valuesConsumer, session );
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
		throw new NotYetImplementedFor6Exception( getClass() );
	}

	@Override
	public String getSqlAliasStem() {
		return getAttributeMapping().getAttributeName();
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
		throw new NotYetImplementedFor6Exception( getClass() );
//		final SessionFactoryImplementor sessionFactory = creationState.getSqlAstCreationState()
//				.getCreationContext()
//				.getSessionFactory();
//		final SqlExpressionResolver sqlExpressionResolver = creationState.getSqlAstCreationState()
//				.getSqlExpressionResolver();
//		final FromClauseAccess fromClauseAccess = creationState.getSqlAstCreationState().getFromClauseAccess();
//
//		final TableGroup tableGroup = fromClauseAccess.getTableGroup( fetchParent.getNavigablePath() );
//		final TableReference tableReference = tableGroup.getTableReference( getContainingTableExpression() );
//
//		final Expression columnExpr = sqlExpressionResolver.resolveSqlExpression(
//				SqlExpressionResolver.createColumnReferenceKey( tableReference, getMappedColumnExpression() ),
//				processingState -> new ColumnReference(
//						tableReference,
//						getMappedColumnExpression(),
//						getJdbcMapping()
//				)
//		);
//		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
//				columnExpr,
//				getJavaTypeDescriptor(),
//				sessionFactory.getTypeConfiguration()
//		);
//
//		return new BasicFetch(
//				sqlSelection.getValuesArrayPosition(),
//				fetchParent,
//				fetchablePath,
//				this,
//				getAttributeMapping().isNullable(),
//				null,
//				fetchTiming,
//				creationState
//		);
	}

	@Override
	public <T> DomainResult<T> createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			String resultVariable,
			DomainResultCreationState creationState) {
		throw new NotYetImplementedFor6Exception( getClass() );
	}
}