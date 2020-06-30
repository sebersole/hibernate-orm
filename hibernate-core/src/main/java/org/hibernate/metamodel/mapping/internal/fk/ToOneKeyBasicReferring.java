/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.jdbc.env.spi.QualifiedObjectNameFormatter;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.ToOne;
import org.hibernate.metamodel.mapping.BasicValuedModelPart;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.MappingModelCreationException;
import org.hibernate.metamodel.mapping.MappingType;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.basic.BasicFetch;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Basic-valued ToOneKey representing the referring side of the association.
 *
 * @implSpec Decided to split into referring/target specific impls to avoid
 * run-time if-checks in the method impls
 *
 * @author Steve Ebersole
 */
public class ToOneKeyBasicReferring extends AbstractToOneKey implements ToOneKey, KeyModelPartBasic {
	public ToOneKeyBasicReferring(
			ToOne bootValue,
			ToOneAttributeMapping toOneAttribute,
			MappingModelCreationProcess creationProcess) {
		super( toOneAttribute );

		if ( bootValue.getColumnSpan() > 1 ) {
			throw new MappingModelCreationException(
					"Multiple columns found for basic-valued to-one key : " + toOneAttribute.getNavigableRole()
							+ " (" + bootValue + ")"
			);
		}

		super.postConstruct(
				// referring-side will never have a mapped-by
				() -> null,
				(fkConsumer) -> {
					final EntityMappingType associatedEntityType = toOneAttribute.getAssociatedEntityMappingType();
					final String referencedPropertyName = bootValue.getReferencedPropertyName();

					if ( referencedPropertyName == null ) {
						generateFromAssociatedIdentifier(
								associatedEntityType,
								bootValue,
								this,
								fkConsumer,
								creationProcess
						);
					}
					else {
						generateFromReferencedAttribute(
								associatedEntityType,
								bootValue,
								this,
								referencedPropertyName,
								fkConsumer,
								creationProcess
						);
					}
				},
				creationProcess
		);
	}

	private static void generateFromAssociatedIdentifier(
			EntityMappingType associatedEntityType,
			ToOne bootValue,
			ToOneKey thisSide,
			Consumer<ForeignKey> fkConsumer,
			MappingModelCreationProcess creationProcess) {
		creationProcess.registerSubPartGroupInitializationListener(
				associatedEntityType,
				MappingModelCreationProcess.SubPartGroup.ROOT,
				() -> {
					final BasicValuedModelPart identifier = (BasicValuedModelPart) associatedEntityType.getIdentifierMapping();

					final ForeignKey foreignKey = generateForeignKey(
							bootValue,
							thisSide,
							identifier,
							creationProcess
					);

					fkConsumer.accept( foreignKey );
				}
		);
	}

	private static void generateFromReferencedAttribute(
			EntityMappingType associatedEntityType,
			ToOne bootValue,
			ToOneKey thisSide,
			String referencedPropertyName,
			Consumer<ForeignKey> fkConsumer,
			MappingModelCreationProcess creationProcess) {
		creationProcess.registerSubPartGroupInitializationListener(
				associatedEntityType,
				MappingModelCreationProcess.SubPartGroup.NORMAL,
				() -> {
					final ModelPart referencedPart = associatedEntityType.findAttributeMapping( referencedPropertyName );

					final ForeignKey foreignKey = generateForeignKey(
							bootValue,
							thisSide,
							referencedPart,
							creationProcess
					);

					fkConsumer.accept( foreignKey );
				}
		);
	}

	private static ForeignKeyBasic generateForeignKey(
			ToOne bootValue,
			ToOneKey thisSideGeneric,
			ModelPart referencedPartGeneric,
			MappingModelCreationProcess creationProcess) {
		assert thisSideGeneric instanceof BasicValuedModelPart;
		assert referencedPartGeneric instanceof BasicValuedModelPart;

		final BasicValuedModelPart thisSide = (BasicValuedModelPart) thisSideGeneric;
		final BasicValuedModelPart referencedPart = (BasicValuedModelPart) referencedPartGeneric;

		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
		final QualifiedObjectNameFormatter nameFormatter = sessionFactory.getJdbcServices()
				.getJdbcEnvironment()
				.getQualifiedObjectNameFormatter();
		final Dialect dialect = sessionFactory.getDialect();

		final String referringTable = nameFormatter.format(
				bootValue.getTable().getQualifiedTableName(),
				dialect
		);
		final String referringColumn = bootValue.getConstraintColumns().get( 0 ).getText( dialect );

		final String targetTable = referencedPart.getContainingTableExpression();
		final String targetColumn = referencedPart.getMappedColumnExpression();

		return new ForeignKeyBasic(
				referringTable,
				referringColumn,
				thisSide,
				targetTable,
				targetColumn,
				referencedPart,
				referencedPart.getJdbcMapping()
		);
	}

	@Override
	public ForeignKeyBasic getForeignKeyDescriptor() {
		return (ForeignKeyBasic) super.getForeignKeyDescriptor();
	}

	@Override
	public ForeignKeyDirection getDirection() {
		return super.getDirection();
	}

	@Override
	public String getContainingTableExpression() {
		return getForeignKeyDescriptor().getReferringSide().getTableName();
	}

	@Override
	public String getMappedColumnExpression() {
		return getForeignKeyDescriptor().getReferringSide().getColumn();
	}

	@Override
	public JdbcMapping getJdbcMapping() {
		return getForeignKeyDescriptor().getJdbcMapping();
	}

	@Override
	public MappingType getMappedTypeDescriptor() {
		return getJdbcMapping();
	}

	@Override
	public JavaTypeDescriptor<?> getJavaTypeDescriptor() {
		return getJdbcMapping().getJavaTypeDescriptor();
	}

	@Override
	public MappingType getPartMappingType() {
		return getJdbcMapping();
	}

	@Override
	public void visitColumns(ColumnConsumer consumer) {
		consumer.accept( getContainingTableExpression(), getMappedColumnExpression(), getJdbcMapping() );
	}

	@Override
	public List<JdbcMapping> getJdbcMappings(TypeConfiguration typeConfiguration) {
		return Collections.singletonList( getJdbcMapping() );
	}

	@Override
	public int getJdbcTypeCount(TypeConfiguration typeConfiguration) {
		return 1;
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
		action.accept( getJdbcMapping() );
	}

	@Override
	public Object disassemble(Object value, SharedSessionContractImplementor session) {
		return value;
	}

	@Override
	public void visitDisassembledJdbcValues(
			Object value,
			Clause clause,
			JdbcValuesConsumer valuesConsumer,
			SharedSessionContractImplementor session) {
		valuesConsumer.consume( value, getJdbcMapping() );
	}

	@Override
	public void visitJdbcValues(
			Object value,
			Clause clause,
			JdbcValuesConsumer valuesConsumer,
			SharedSessionContractImplementor session) {
		valuesConsumer.consume( value, getJdbcMapping() );
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
		final SessionFactoryImplementor sessionFactory = creationState.getSqlAstCreationState()
				.getCreationContext()
				.getSessionFactory();
		final SqlExpressionResolver sqlExpressionResolver = creationState.getSqlAstCreationState()
				.getSqlExpressionResolver();
		final FromClauseAccess fromClauseAccess = creationState.getSqlAstCreationState().getFromClauseAccess();

		final TableGroup tableGroup = fromClauseAccess.getTableGroup( fetchParent.getNavigablePath() );
		final TableReference tableReference = tableGroup.getTableReference( getContainingTableExpression() );

		final Expression columnExpr = sqlExpressionResolver.resolveSqlExpression(
				SqlExpressionResolver.createColumnReferenceKey( tableReference, getMappedColumnExpression() ),
				processingState -> new ColumnReference(
						tableReference,
						getMappedColumnExpression(),
						getJdbcMapping()
				)
		);
		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
				columnExpr,
				getJavaTypeDescriptor(),
				sessionFactory.getTypeConfiguration()
		);

		return new BasicFetch(
				sqlSelection.getValuesArrayPosition(),
				fetchParent,
				fetchablePath,
				this,
				getAttributeMapping().isNullable(),
				null,
				fetchTiming,
				creationState
		);
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