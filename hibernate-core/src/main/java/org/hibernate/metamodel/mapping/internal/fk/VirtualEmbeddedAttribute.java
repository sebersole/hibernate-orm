/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Selectable;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EmbeddedAttributeMapping;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;
import org.hibernate.metamodel.mapping.internal.AbstractVirtualAttribute;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.metamodel.model.domain.NavigableRole;
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
import org.hibernate.sql.ast.tree.from.CompositeTableGroup;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableGroupJoin;
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.embeddable.internal.EmbeddableFetchImpl;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * Embedded attribute defined as part of a composite-key
 *
 * @author Steve Ebersole
 */
public class VirtualEmbeddedAttribute
		extends AbstractVirtualAttribute
		implements EmbeddedAttributeMapping {

	private final String tableName;
	private final List<String> columnNames;
	private final List<JdbcMapping> jdbcMappings;

	private final boolean nullable;
	private final EmbeddableMappingType embeddable;

	public VirtualEmbeddedAttribute(
			NavigableRole role,
			EmbeddedAttributeMapping originalEmbedded,
			ManagedMappingType declaringType,
			String tableName,
			Iterator<Selectable> bootColumnItr,
			MappingModelCreationProcess creationProcess) {
		super(
				role,
				originalEmbedded.getMappedFetchOptions(),
				originalEmbedded.getPropertyAccess().getPropertyAccessStrategy().buildPropertyAccess(
						declaringType.getJavaTypeDescriptor().getJavaType(),
						originalEmbedded.getAttributeName()
				),
				declaringType,
				originalEmbedded.getStateArrayPosition()
		);

		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();

		this.tableName = tableName;

		final  List<AttributeMapping> virtualAttributes = new ArrayList<>( originalEmbedded.getNumberOfFetchables() );

		final int jdbcTypeCount = originalEmbedded.getJdbcTypeCount( sessionFactory.getTypeConfiguration() );
		this.columnNames = new ArrayList<>( jdbcTypeCount );
		this.jdbcMappings = new ArrayList<>( jdbcTypeCount );

		originalEmbedded.visitSubParts(
				modelPart -> {
					final SingularAttributeMapping originalSubAttribute = (SingularAttributeMapping) modelPart;
					final SingularAttributeMapping virtualCopy = ForeignKeyHelper.makeKeyCopy(
							originalSubAttribute,
							tableName,
							bootColumnItr,
							declaringType,
							creationProcess
					);

					virtualAttributes.add( virtualCopy );

					virtualCopy.visitColumns(
							(containingTableExpression, columnExpression, jdbcMapping) -> {
								assert containingTableExpression.equals( tableName );

								columnNames.add( columnExpression );
								jdbcMappings.add( jdbcMapping );
							}
					);
				},
				null
		);

		originalEmbedded.visitColumns(
				(containingTableExpression, columnExpression, jdbcMapping) -> {
				}
		);

		this.nullable = originalEmbedded.getAttributeMetadataAccess().resolveAttributeMetadata( null ).isNullable();

		this.embeddable = new EmbeddableMappingType(
				originalEmbedded.getEmbeddableTypeDescriptor().getRepresentationStrategy(),
				embeddableMappingType -> virtualAttributes,
				embeddableMappingType -> this,
				sessionFactory
		);
	}

	@Override
	public EmbeddableMappingType getMappedTypeDescriptor() {
		return embeddable;
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
	public void visitColumns(ColumnConsumer consumer) {
		for ( int i = 0; i < columnNames.size(); i++ ) {
			consumer.accept(
					tableName,
					columnNames.get( i ),
					jdbcMappings.get( i )
			);
		}
	}

	@Override
	public Expression toSqlExpression(
			TableGroup tableGroup,
			Clause clause,
			SqmToSqlAstConverter walker,
			SqlAstCreationState sqlAstCreationState) {
		final TableReference tableReference = tableGroup.getTableReference( tableName );

		final int numberOfColumns = columnNames.size();
		final List<Expression> subExpressions = new ArrayList<>( numberOfColumns );
		final SqlExpressionResolver sqlExpressionResolver = sqlAstCreationState.getSqlExpressionResolver();

		visitColumns(
				(containingTableExpression, columnExpression, jdbcMapping) -> {
					assert containingTableExpression.equals( tableName );

					subExpressions.add(
							sqlExpressionResolver.resolveSqlExpression(
									SqlExpressionResolver.createColumnReferenceKey( tableReference, columnExpression ),
									processingState -> new ColumnReference(
											tableReference,
											columnExpression,
											jdbcMapping,
											sqlAstCreationState.getCreationContext().getSessionFactory()
									)
							)
					);
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
		return new TableGroupJoin( navigablePath, sqlAstJoinType, compositeTableGroup );
	}

	@Override
	public String getSqlAliasStem() {
		return getAttributeName();
	}

	@Override
	public ModelPart findSubPart(String name, EntityMappingType treatTargetType) {
		return embeddable.findAttributeMapping( name );
	}

	@Override
	public void visitSubParts(Consumer<ModelPart> consumer, EntityMappingType treatTargetType) {
		embeddable.visitAttributeMappings( consumer::accept );
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
		// todo (6.0) : will this impl work?
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
	public EmbeddableMappingType getPartMappingType() {
		return embeddable;
	}

	@Override
	public JavaTypeDescriptor<?> getJavaTypeDescriptor() {
		return embeddable.getJavaTypeDescriptor();
	}

	@Override
	public SingularAttributeMapping getParentInjectionAttributeMapping() {
		return null;
	}

	@Override
	protected boolean isNullable() {
		return nullable;
	}
}
