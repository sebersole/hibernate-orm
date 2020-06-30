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
import java.util.Objects;
import java.util.function.Consumer;

import org.hibernate.LockMode;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.jdbc.env.spi.QualifiedObjectNameFormatter;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.MutableString;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.KeyValue;
import org.hibernate.mapping.Selectable;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.MappingType;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.sqm.sql.SqmToSqlAstConverter;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.SqlAstJoinType;
import org.hibernate.sql.ast.spi.FromClauseAccess;
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
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.embeddable.internal.EmbeddableFetchImpl;

/**
 * @author Steve Ebersole
 */
public class CollectionKeyComposite
		extends AbstractCollectionKey
		implements CollectionKey, KeyModelPartComposite, FetchOptions {
	private final EmbeddableMappingType embeddableDescriptor;

	public CollectionKeyComposite(
			PluralAttributeMapping attributeMapping,
			EmbeddableMappingType embeddableDescriptor,
			Collection bootCollectionDescriptor,
			Component bootCompositeDescriptor,
			MappingModelCreationProcess creationProcess) {
		super( attributeMapping );
		this.embeddableDescriptor = embeddableDescriptor;

		postConstruct(
				(fkConsumer) -> {
					final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
					final Dialect dialect = sessionFactory.getDialect();
					final QualifiedObjectNameFormatter nameFormatter = sessionFactory.getJdbcServices()
							.getJdbcEnvironment()
							.getQualifiedObjectNameFormatter();

					final CollectionPersister collectionDescriptor = attributeMapping.getCollectionDescriptor();

					final KeyValue bootCollectionKeyDescriptor = bootCollectionDescriptor.getKey();

					final EmbeddableValuedModelPart keyTargetPart;
					final String lhsPropertyName = collectionDescriptor.getCollectionType().getLHSPropertyName();
					if ( lhsPropertyName == null ) {
						keyTargetPart = (EmbeddableValuedModelPart) collectionDescriptor.getOwnerEntityPersister().getIdentifierMapping();
					}
					else {
						keyTargetPart = (EmbeddableValuedModelPart) collectionDescriptor.getOwnerEntityPersister().findAttributeMapping( lhsPropertyName );
					}

					final int numberOfColumns = bootCollectionKeyDescriptor.getColumnSpan();

					final MutableString targetTable = new MutableString();
					final List<String> targetColumns = new ArrayList<>( numberOfColumns );

					final String referringTable = nameFormatter.format(
							bootCollectionKeyDescriptor.getTable().getQualifiedTableName(),
							dialect
					);
					final ArrayList<String> referringColumns = new ArrayList<>( bootCollectionKeyDescriptor.getColumnSpan() );
					final Iterator<Selectable> bootReferringColumnItr = bootCollectionKeyDescriptor.getColumnIterator();

					final List<JdbcMapping> jdbcMappings = new ArrayList<>( numberOfColumns );


					keyTargetPart.visitColumns(
							(containingTableExpression, columnExpression, jdbcMapping) -> {
								assert bootReferringColumnItr.hasNext();

								referringColumns.add( bootReferringColumnItr.next().getText( dialect ) );

								final String currentTableName = targetTable.getValue();
								if ( currentTableName != null ) {
									if ( ! Objects.equals( currentTableName, containingTableExpression ) ) {
										throw new UnsupportedOperationException( "Foreign-key columns cannot be split across tables" );
									}
								}
								else {
									targetTable.setValue( containingTableExpression );
								}

								targetColumns.add( columnExpression );
								jdbcMappings.add( jdbcMapping );
							}
					);

					assert ! bootReferringColumnItr.hasNext();

					fkConsumer.accept(
							new ForeignKeyComposite(
									this,
									referringTable,
									referringColumns,
									keyTargetPart,
									targetTable.getValue(),
									targetColumns,
									jdbcMappings,
									creationProcess
							)
					);
				},
				creationProcess
		);
	}

	@Override
	public ForeignKeyComposite getForeignKeyDescriptor() {
		return (ForeignKeyComposite) super.getForeignKeyDescriptor();
	}

	@Override
	public EmbeddableMappingType getEmbeddableTypeDescriptor() {
		return embeddableDescriptor;
	}

	@Override
	public String getContainingTableExpression() {
		return getMappedModelPart().getForeignKeyDescriptor().getReferringSide().getTableName();
	}

	@Override
	public List<String> getMappedColumnExpressions() {
		return getMappedModelPart().getForeignKeyDescriptor().getReferringSide().getColumnNames();
	}

	@Override
	public SingularAttributeMapping getParentInjectionAttributeMapping() {
		return null;
	}

	@Override
	public Expression toSqlExpression(
			TableGroup tableGroup,
			Clause clause,
			SqmToSqlAstConverter walker,
			SqlAstCreationState sqlAstCreationState) {
		final SessionFactoryImplementor sessionFactory = sqlAstCreationState.getCreationContext().getSessionFactory();

		final int jdbcTypeCount = getForeignKeyDescriptor().getReferringSide().getKeyPart().getJdbcTypeCount( sessionFactory.getTypeConfiguration() );
		final List<Expression> subExpressions = new ArrayList<>( jdbcTypeCount );

		getForeignKeyDescriptor().getReferringSide().visitColumns(
				(containingTableExpression, columnExpression, jdbcMapping) -> subExpressions.add(
						new ColumnReference(
								tableGroup.getTableReference( containingTableExpression ),
								columnExpression,
								jdbcMapping,
								sessionFactory
						)
				)
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
		return "key";
	}

	@Override
	public ModelPart findSubPart(String name, EntityMappingType treatTargetType) {
		return embeddableDescriptor.findSubPart( name, treatTargetType );
	}

	@Override
	public void visitSubParts(Consumer<ModelPart> consumer, EntityMappingType treatTargetType) {
		embeddableDescriptor.visitSubParts( consumer, treatTargetType );
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
		final FromClauseAccess fromClauseAccess = creationState.getSqlAstCreationState().getFromClauseAccess();
		fromClauseAccess.resolveTableGroup(
				fetchablePath,
				p -> {
					final TableGroup parentTableGroup = fromClauseAccess.getTableGroup( fetchParent.getNavigablePath() );
					return new CompositeTableGroup( fetchablePath, this, parentTableGroup );
				}
		);
		return new EmbeddableFetchImpl(
				fetchablePath,
				this,
				fetchParent,
				fetchTiming,
				true,
				getAttributeMapping().getAttributeMetadataAccess().resolveAttributeMetadata( null ).isNullable(),
				creationState
		);
	}

	@Override
	public int getNumberOfFetchables() {
		return embeddableDescriptor.getNumberOfFetchables();
	}

	@Override
	public MappingType getPartMappingType() {
		return embeddableDescriptor;
	}
}
