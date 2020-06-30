/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.hibernate.LockMode;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.Selectable;
import org.hibernate.metamodel.mapping.BasicValuedModelPart;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.MappingType;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.basic.BasicFetch;
import org.hibernate.sql.results.graph.basic.BasicResult;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class CollectionKeyBasic
		extends AbstractCollectionKey
		implements CollectionKey, KeyModelPartBasic, FetchOptions {
	private final String tableName;
	private final String columnExpression;
	private final JdbcMapping jdbcMapping;

	public CollectionKeyBasic(
			PluralAttributeMapping attributeMapping,
			Collection bootCollectionDescriptor,
			Supplier<JdbcMapping> jdbcMappingSupplier,
			MappingModelCreationProcess creationProcess) {
		super( attributeMapping );

		final Dialect dialect = creationProcess.getCreationContext().getSessionFactory().getDialect();

		this.tableName = bootCollectionDescriptor.getCollectionTable()
				.getQualifiedTableName()
				.getTableName()
				.render( dialect );

		final KeyValue bootCollectionKey = bootCollectionDescriptor.getKey();

		final Iterator<Selectable> columnIterator = bootCollectionKey.getColumnIterator();
		assert columnIterator.hasNext();
		this.columnExpression = columnIterator.next().getText( dialect );
		assert ! columnIterator.hasNext();

		this.jdbcMapping = jdbcMappingSupplier.get();

		postConstruct(
				(fkConsumer) -> {
					// this side defines the FK - most likely a unidirectional association
					final CollectionPersister collectionDescriptor = attributeMapping.getCollectionDescriptor();

					final BasicValuedModelPart keyTargetPart;
					final String lhsPropertyName = collectionDescriptor.getCollectionType().getLHSPropertyName();
					if ( lhsPropertyName == null ) {
						// points to the identifier as the target
						keyTargetPart = (BasicValuedModelPart) collectionDescriptor.getOwnerEntityPersister().getIdentifierMapping();
					}
					else {
						// we have a "property-ref" - points to the named property as the target
						keyTargetPart = (BasicValuedModelPart) collectionDescriptor.getOwnerEntityPersister().findAttributeMapping( lhsPropertyName );
					}

					fkConsumer.accept(
							new ForeignKeyBasic(
									tableName,
									columnExpression,
									this,
									keyTargetPart.getContainingTableExpression(),
									keyTargetPart.getMappedColumnExpression(),
									keyTargetPart,
									jdbcMapping
							)
					);
				},
				creationProcess
		);
	}

	@Override
	public String getContainingTableExpression() {
		return tableName;
	}

	@Override
	public String getMappedColumnExpression() {
		return columnExpression;
	}

	@Override
	public JdbcMapping getJdbcMapping() {
		return jdbcMapping;
	}

	@Override
	public MappingType getMappedTypeDescriptor() {
		return getJdbcMapping();
	}

	@Override
	public ForeignKeyBasic getForeignKeyDescriptor() {
		return (ForeignKeyBasic) super.getForeignKeyDescriptor();
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
		final FromClauseAccess fromClauseAccess = sqlAstCreationState.getFromClauseAccess();
		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();
		final SessionFactoryImplementor sessionFactory = sqlAstCreationState.getCreationContext().getSessionFactory();

		final TableGroup tableGroup = fromClauseAccess.getTableGroup( fetchParent.getNavigablePath() );
		final TableReference tableReference = tableGroup.getTableReference( tableName );

		final Expression keyColumnReference = sqlExpressionResolver.resolveSqlExpression(
				SqlExpressionResolver.createColumnReferenceKey( tableReference, getMappedColumnExpression() ),
				processingState -> new ColumnReference(
						tableReference,
						columnExpression,
						getJdbcMapping(),
						sessionFactory
				)
		);

		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
				keyColumnReference,
				getJavaTypeDescriptor(),
				sessionFactory.getTypeConfiguration()
		);

		return new BasicFetch<>(
				sqlSelection.getValuesArrayPosition(),
				fetchParent,
				fetchablePath,
				this,
				true,
				null,
				FetchTiming.IMMEDIATE,
				creationState
		);
	}

	@Override
	public <T> DomainResult<T> createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			String resultVariable,
			DomainResultCreationState creationState) {
		final SqlExpressionResolver sqlExpressionResolver = creationState.getSqlAstCreationState().getSqlExpressionResolver();

		final Expression expression;
		final TableReference referringTableReference = tableGroup.getTableReference( tableName );
		if ( referringTableReference != null ) {
			expression = sqlExpressionResolver.resolveSqlExpression(
					SqlExpressionResolver.createColumnReferenceKey( referringTableReference, columnExpression ),
					processingState -> new ColumnReference( referringTableReference, columnExpression, jdbcMapping )
			);
		}
		else {
			// see if the TableGroup is defined in terms of the owning side...
			final TableReference targetTableReference = tableGroup.getTableReference( getForeignKeyDescriptor().getTargetSide().getTableName() );
			final String targetColumn = getForeignKeyDescriptor().getTargetSide().getColumn();
			expression = sqlExpressionResolver.resolveSqlExpression(
					SqlExpressionResolver.createColumnReferenceKey( targetTableReference, targetColumn ),
					processingState -> new ColumnReference( targetTableReference, targetColumn, jdbcMapping )
			);
		}

		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
				expression,
				getJavaTypeDescriptor(),
				creationState.getSqlAstCreationState().getCreationContext().getSessionFactory().getTypeConfiguration()
		);

		return new BasicResult(
				sqlSelection.getValuesArrayPosition(),
				resultVariable,
				getJavaTypeDescriptor(),
				navigablePath
		);
	}

	@Override
	public void visitColumns(ColumnConsumer consumer) {
		consumer.accept( tableName, columnExpression, jdbcMapping );
	}

	@Override
	public List<JdbcMapping> getJdbcMappings(TypeConfiguration typeConfiguration) {
		return Collections.singletonList( jdbcMapping );
	}

	@Override
	public MappingType getPartMappingType() {
		return jdbcMapping;
	}

	@Override
	public void visitJdbcTypes(Consumer<JdbcMapping> action, Clause clause, TypeConfiguration typeConfiguration) {
		visitJdbcTypes( action, typeConfiguration );
	}

	@Override
	public void visitJdbcTypes(Consumer<JdbcMapping> action, TypeConfiguration typeConfiguration) {
		action.accept( jdbcMapping );
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
		valuesConsumer.consume( value, jdbcMapping );
	}

	@Override
	public void visitJdbcValues(
			Object value,
			Clause clause,
			JdbcValuesConsumer valuesConsumer,
			SharedSessionContractImplementor session) {
		valuesConsumer.consume( value, jdbcMapping );
	}
}
