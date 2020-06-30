/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import org.hibernate.LockMode;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.ToOne;
import org.hibernate.metamodel.mapping.BasicValuedModelPart;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.mapping.internal.EntityCollectionPart;
import org.hibernate.metamodel.mapping.internal.InFlightEntityMappingType;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationHelper;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.query.NavigablePath;
import org.hibernate.sql.ast.spi.FromClauseAccess;
import org.hibernate.sql.ast.spi.SqlAstCreationState;
import org.hibernate.sql.ast.spi.SqlExpressionResolver;
import org.hibernate.sql.ast.spi.SqlSelection;
import org.hibernate.sql.ast.tree.expression.ColumnReference;
import org.hibernate.sql.ast.tree.expression.Expression;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.basic.BasicFetch;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class JoinTableKeyBasic
		extends AbstractJoinTableKey
		implements JoinTableKey, KeyModelPartBasic {
	private final NavigableRole navigableRole;

	private final String table;
	private final String column;

	private final PluralAttributeMapping attributeMapping;
	private final EntityCollectionPart collectionPart;

	/**
	 * Constructor for many-to-many join table where the identifier of the associated entity
	 * has not yet been initialized.
	 */
	public JoinTableKeyBasic(
			EntityMappingType associatedEntity,
			PluralAttributeMapping attributeMapping,
			EntityCollectionPart collectionPart,
			Collection bootValue,
			ToOne collectionPartBootValue,
			String joinTableName,
			MappingModelCreationProcess creationProcess) {
		super( collectionPart.getNavigableRole().append( PART_NAME ) );
		assert ! bootValue.isOneToMany();

		this.attributeMapping = attributeMapping;
		this.navigableRole = collectionPart.getNavigableRole().append( PART_NAME );

		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
		final Dialect dialect = sessionFactory.getDialect();

		final Selectable selectable = collectionPartBootValue.getConstraintColumns().get( 0 );
		this.column = selectable.getText( dialect );
		this.table = joinTableName;

		creationProcess.registerSubPartGroupInitializationListener(
				associatedEntity,
				MappingModelCreationProcess.SubPartGroup.ROOT,
				() -> {
					final BasicValuedModelPart associationReferencedPart = (BasicValuedModelPart) associatedEntity.getIdentifierMapping();
					final ForeignKeyBasic foreignKey = new ForeignKeyBasic(
							table,
							column,
							this,
							associationReferencedPart.getContainingTableExpression(),
							associationReferencedPart.getMappedColumnExpression(),
							associationReferencedPart,
							associationReferencedPart.getJdbcMapping()
					);

					setForeignKey( foreignKey );

				}
		);

		this.collectionPart = collectionPart;
	}

	/**
	 * Constructor for many-to-many join table where the identifier of the associated entity
	 * has already been initialized.
	 */
	public JoinTableKeyBasic(
			BasicValuedModelPart associationReferencedPart,
			PluralAttributeMapping attributeMapping,
			EntityCollectionPart collectionPart,
			Collection bootValue,
			ToOne collectionPartBootValue,
			String joinTableName,
			MappingModelCreationProcess creationProcess) {
		super( collectionPart.getNavigableRole().append( PART_NAME ) );
		assert ! bootValue.isOneToMany();

		this.attributeMapping = attributeMapping;
		this.navigableRole = collectionPart.getNavigableRole().append( PART_NAME );

		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
		final Dialect dialect = sessionFactory.getDialect();

		final Selectable selectable = collectionPartBootValue.getConstraintColumns().get( 0 );
		this.column = selectable.getText( dialect );
		this.table = joinTableName;


		creationProcess.registerForeignKeyPostInitCallbacks(
				"`@JoinTable` FK initialization : " + attributeMapping.getNavigableRole().getFullPath(),
				() -> {
					final ForeignKeyBasic foreignKey = new ForeignKeyBasic(
							table,
							column,
							this,
							associationReferencedPart.getContainingTableExpression(),
							associationReferencedPart.getMappedColumnExpression(),
							associationReferencedPart,
							associationReferencedPart.getJdbcMapping()
					);

					setForeignKey( foreignKey );

					return true;
				}
		);


		this.collectionPart = collectionPart;
	}

	@Override
	public String getContainingTableExpression() {
		return table;
	}

	@Override
	public String getMappedColumnExpression() {
		return column;
	}

	@Override
	public JdbcMapping getJdbcMapping() {
		return getForeignKeyDescriptor().getJdbcMapping();
	}

	@Override
	public String getFetchableName() {
		return getPartName();
	}

	@Override
	public FetchOptions getMappedFetchOptions() {
		return null;
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
		final TableReference tableReference = tableGroup.getTableReference( table );

		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();
		final Expression keyColumnExpression = sqlExpressionResolver.resolveSqlExpression(
				SqlExpressionResolver.createColumnReferenceKey( tableReference, column ),
				processingState -> new ColumnReference(
						tableReference,
						column,
						getJdbcMapping(),
						sessionFactory
				)
		);
		final SqlSelection sqlSelection = sqlExpressionResolver.resolveSqlSelection(
				keyColumnExpression,
				getJavaTypeDescriptor(),
				sessionFactory.getTypeConfiguration()
		);
		return new BasicFetch(
				sqlSelection.getValuesArrayPosition(),
				fetchParent,
				fetchablePath,
				this,
				false,
				null,
				fetchTiming,
				creationState
		);
	}

	@Override
	public ForeignKeySource getMappedModelPart() {
		return collectionPart;
	}

	@Override
	public JavaTypeDescriptor getJavaTypeDescriptor() {
		return getMappedTypeDescriptor().getJavaTypeDescriptor();
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public EntityMappingType findContainingEntityMapping() {
		return attributeMapping.findContainingEntityMapping();
	}

	@Override
	public EntityMappingType getMappedTypeDescriptor() {
		return collectionPart.getAssociatedEntityMappingType();
	}

	@Override
	public ForeignKeyBasic getForeignKeyDescriptor() {
		return (ForeignKeyBasic) super.getForeignKeyDescriptor();
	}
}
