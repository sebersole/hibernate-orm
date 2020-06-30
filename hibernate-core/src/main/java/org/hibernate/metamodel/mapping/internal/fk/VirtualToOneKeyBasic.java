/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.MappingType;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableGroupJoin;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.basic.BasicFetch;
import org.hibernate.sql.results.graph.basic.BasicResult;
import org.hibernate.sql.results.graph.entity.EntityFetch;
import org.hibernate.sql.results.graph.entity.internal.EntityFetchJoinedImpl;

/**
 * Acts as the KeyModelPart for a basic association key
 *
 * @author Steve Ebersole
 */
public class VirtualToOneKeyBasic
		extends AbstractToOneKey
		implements ToOneKey, KeyModelPartBasic, FetchOptions {
	private final String tableName;
	private final String columnName;
	private final JdbcMapping jdbcMapping;

	private final FetchStyle fetchStyle;
	private final FetchTiming fetchTiming;
	private final boolean nullable;

	public VirtualToOneKeyBasic(
			ToOneAttributeMapping attributeMapping,
			String tableName,
			String columnName,
			JdbcMapping jdbcMapping,
			FetchStyle fetchStyle,
			FetchTiming fetchTiming,
			boolean nullable,
			KeyModelPartBasic owningSide,
			@SuppressWarnings("unused") MappingModelCreationProcess creationProcess) {
		super( attributeMapping );
		this.tableName = tableName;
		this.columnName = columnName;
		this.jdbcMapping = jdbcMapping;
		this.fetchStyle = fetchStyle;
		this.fetchTiming = fetchTiming;
		this.nullable = nullable;

		// NOTE:
		// 		1) many-to-one 			-> virtual == referring
		//		2) one-to-one 			-> virtual == target
		//		3) logical-one-to-one 	-> virtual == target

		final ToOneAttributeMapping.Cardinality cardinality = attributeMapping.getCardinality();
		if ( cardinality == ToOneAttributeMapping.Cardinality.MANY_TO_ONE ) {
			setForeignKey(
					new ForeignKeyBasic(
							tableName,
							columnName,
							this,
							owningSide.getContainingTableExpression(),
							owningSide.getMappedColumnExpression(),
							owningSide,
							jdbcMapping
					)
			);
		}
		else {
			setForeignKey(
					new ForeignKeyBasic(
							owningSide.getContainingTableExpression(),
							owningSide.getMappedColumnExpression(),
							owningSide,
							tableName,
							columnName,
							this,
							jdbcMapping
					)
			);
		}


		/*

		Example #1 - uni-dir many-to-one

		Order.(cust_id) -> Customer.(id)

			- referring side is `Order.(cust_id)`.  This is a "virtual" mapping.  think of it as
				if a real `Order#customer` attribute existed
			- target side is `Customer.(id)` represented by the physical `Customer#orders` mapping


		ergo - virtual is referring

		@Entity
		class Order {
			...
		}

		@Entity
		class Customer {
			...
			@OneToMany
			List<Order> orders;
		}

		----------------------------------------------------------------

		Example #2 - uni-dir one-to-one

		Person.(detail_id) -> PersonDetail.(id)

			- referring side is `Person.(detail_id)`, which is non-virtual
			- target side is `PersonDetail.(id)`, which is virtual

		ergo - target side is virtual

		@Entity
		class Person {
			...
			@OneToOne
			PersonDetails details;
		}

		@Entity
		class PersonDetails {
			...
		}


		----------------------------------------------------------------

		Example #3 - uni-directional logical one-to-one

		Person.(detail_id) -> PersonDetail.(id)

			- referring side is `Person.(detail_id)`, which is non-virtual
			- target side is `PersonDetail.(id)`, which is virtual


		@Entity
		class Person {
			...
			@ManyToOne
			@JoinColumn( unique = true )
			PersonDetails details;
		}

		@Entity
		class PersonDetails {
			...
		}

		ergo - target side is virtual


		Summary
		---------------------
		1) many-to-one 			-> virtual == referring
		2) one-to-one 			-> virtual == target
		3) logical-one-to-one 	-> virtual == target

		 */
	}

	@Override
	public ForeignKeyBasic getForeignKeyDescriptor() {
		return (ForeignKeyBasic) super.getForeignKeyDescriptor();
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
	public MappingType getMappedTypeDescriptor() {
		return jdbcMapping;
	}

	@Override
	public FetchOptions getMappedFetchOptions() {
		return this;
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
		final TableGroup tableGroup = fromClauseAccess.getTableGroup( fetchParent.getNavigablePath() );
		final SqlSelection sqlSelection = generateSqlSelection( tableGroup, creationState );

		//noinspection rawtypes
		return new BasicFetch(
				sqlSelection.getValuesArrayPosition(),
				fetchParent,
				fetchablePath,
				this,
				nullable,
				null,
				fetchTiming,
				creationState
		);
	}

	@Override
	protected Fetch generateKeyFetch(
			NavigablePath keyPath,
			FetchParent fetchParent,
			DomainResultCreationState creationState) {
		return generateFetch(
				fetchParent,
				keyPath,
				FetchTiming.IMMEDIATE,
				getAttributeMapping().isNullable(),
				LockMode.READ,
				null,
				creationState
		);
	}

	private SqlSelection generateSqlSelection(TableGroup tableGroup, DomainResultCreationState creationState) {
		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();

		final TableReference tableReference = tableGroup.getTableReference( tableName );
		assert tableReference != null : "Could not resolve FK table `" + tableName + "` as part of TableGroup : " + tableGroup;

		return generateSqlSelection( tableReference, sqlAstCreationState );
	}

	private SqlSelection generateSqlSelection(TableReference tableReference, SqlAstCreationState sqlAstCreationState) {
		final SessionFactoryImplementor sessionFactory = sqlAstCreationState.getCreationContext().getSessionFactory();
		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();

		final Expression columnRef = sqlExpressionResolver.resolveSqlExpression(
				SqlExpressionResolver.createColumnReferenceKey( tableReference, columnName ),
				processingState -> new ColumnReference(
						tableReference,
						columnName,
						jdbcMapping,
						sessionFactory
				)
		);

		return sqlExpressionResolver.resolveSqlSelection(
				columnRef,
				getJavaTypeDescriptor(),
				sessionFactory.getTypeConfiguration()
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
				getJavaTypeDescriptor()
		);
	}

	@Override
	public void applySqlSelections(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			DomainResultCreationState creationState) {
		applySqlSelections( navigablePath, tableGroup, creationState, ((sqlSelection, jdbcMapping1) -> {}) );
	}

	@Override
	public void applySqlSelections(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			DomainResultCreationState creationState,
			BiConsumer<SqlSelection, JdbcMapping> selectionConsumer) {
		final SqlSelection sqlSelection = generateSqlSelection( tableGroup, creationState );
		selectionConsumer.accept( sqlSelection, getJdbcMapping() );
	}

	@Override
	public EntityMappingType findContainingEntityMapping() {
		return getAttributeMapping().findContainingEntityMapping();
	}

	@Override
	public void registerForeignKeyInitializationListener(Consumer<ForeignKey> listener) {
		throw new UnsupportedOperationException(
				"Unexpected call to VirtualToOneKeyBasic#registerForeignKeyInitializationListener"
		);
	}
}
