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
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.ToOne;
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
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Basic-valued ToOneKey representing the target side of the association.
 *
 * @implSpec Decided to split into referring/target specific impls to avoid
 * run-time if-checks in the method impls
 *
 * @author Steve Ebersole
 */
public class ToOneKeyBasicTarget extends AbstractToOneKey implements ToOneKey, KeyModelPartBasic {
	public ToOneKeyBasicTarget(
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
	}

	@Override
	public ForeignKeyBasic getForeignKeyDescriptor() {
		return (ForeignKeyBasic) super.getForeignKeyDescriptor();
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