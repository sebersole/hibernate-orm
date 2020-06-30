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
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.env.spi.QualifiedObjectNameFormatter;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.MutableString;
import org.hibernate.mapping.OneToOne;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.ToOne;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.ColumnConsumer;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.MappingModelCreationLogger;
import org.hibernate.metamodel.mapping.MappingType;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
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
import org.hibernate.sql.ast.tree.from.TableReference;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetch;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.sql.results.graph.FetchParent;
import org.hibernate.sql.results.graph.embeddable.internal.EmbeddableFetchImpl;
import org.hibernate.sql.results.graph.embeddable.internal.EmbeddableKeyFetch;
import org.hibernate.type.ForeignKeyDirection;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class ToOneKeyCompositeReferring
		extends AbstractToOneKey
		implements ToOneKey, KeyModelPartComposite, FetchOptions {

	// built during post-init callback.  See `#finishInitialization`
	private EmbeddableMappingType embeddable;
	private String tableName;
	private List<String> columnNames;
	private List<JdbcMapping> jdbcMappings;

	public ToOneKeyCompositeReferring(
			ToOne bootValue,
			ToOneAttributeMapping toOneAttribute,
			ManagedMappingType declaringType,
			MappingModelCreationProcess creationProcess) {
		super( toOneAttribute );

		final EntityMappingType associatedEntityType = toOneAttribute.getAssociatedEntityMappingType();
		final String referencedAttributeName = bootValue.getReferencedPropertyName();

		if ( referencedAttributeName != null ) {
			creationProcess.registerSubPartGroupInitializationListener(
					associatedEntityType,
					MappingModelCreationProcess.SubPartGroup.NORMAL,
					() -> {
						final AttributeMapping referencedAttr = associatedEntityType.findAttributeMapping( referencedAttributeName );
						assert referencedAttr instanceof EmbeddableValuedModelPart;
						final EmbeddableValuedModelPart referencedComposite = (EmbeddableValuedModelPart) referencedAttr;

						creationProcess.registerSubPartGroupInitializationListener(
								referencedComposite.getEmbeddableTypeDescriptor(),
								MappingModelCreationProcess.SubPartGroup.NORMAL,
								() -> super.postConstruct(
										bootValue,
										(fkConsumer) -> {
											final ForeignKeyComposite foreignKey = generateForeignKey(
													bootValue,
													referencedComposite,
													creationProcess
											);
											fkConsumer.accept( foreignKey );
										},
										creationProcess
								)
						);
					}
			);
		}
		else {
			creationProcess.registerSubPartGroupInitializationListener(
					associatedEntityType,
					MappingModelCreationProcess.SubPartGroup.ROOT,
					() -> {
						final EmbeddableValuedModelPart cid = (EmbeddableValuedModelPart) associatedEntityType.getIdentifierMapping();
						creationProcess.registerSubPartGroupInitializationListener(
								cid.getEmbeddableTypeDescriptor(),
								MappingModelCreationProcess.SubPartGroup.NORMAL,
								() -> super.postConstruct(
										bootValue,
										(fkConsumer) -> {
											final ForeignKeyComposite foreignKey = generateForeignKey(
													bootValue,
													cid,
													creationProcess
											);
											fkConsumer.accept( foreignKey );
										},
										creationProcess
								)
						);
					}
			);
		}
	}

	private ForeignKeyComposite generateForeignKey(
			ToOne bootValue,
			EmbeddableValuedModelPart referencedComposite,
			MappingModelCreationProcess creationProcess) {
		final int columnCount = bootValue.getConstraintColumns().size();
		final List<String> localColumnNamesCopy = new ArrayList<>( columnCount );
		final List<JdbcMapping> localJdbcMappingsCopy = new ArrayList<>( columnCount );

		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
		final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
		final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
		final QualifiedObjectNameFormatter nameFormatter = jdbcEnvironment.getQualifiedObjectNameFormatter();
		final Dialect dialect = jdbcEnvironment.getDialect();

		final String tableName = nameFormatter.format( bootValue.getTable().getQualifiedTableName(), dialect );

		this.embeddable = new EmbeddableMappingType(
				referencedComposite.getEmbeddableTypeDescriptor().getRepresentationStrategy(),
				embeddableMappingType -> {
					final List<AttributeMapping> attributesCopy = new ArrayList<>( referencedComposite.getNumberOfFetchables() );

					final MutableString tableNameBuff = new MutableString();
					final Iterator<Selectable> columnItr = bootValue.getConstraintColumns().iterator();

					referencedComposite.visitSubParts(
							subPart -> {
								final AttributeMapping subPartCopy = ForeignKeyHelper.makeKeyCopy(
										(SingularAttributeMapping) subPart,
										tableName,
										columnItr,
										embeddableMappingType,
										creationProcess
								);

								attributesCopy.add( subPartCopy );

								subPartCopy.visitColumns(
										(containingTableExpression, columnExpression, jdbcMapping) -> {
											if ( tableNameBuff.getValue() == null ) {
												tableNameBuff.setValue( containingTableExpression );
											}
											else {
												if ( ! tableNameBuff.getValue().equals( containingTableExpression ) ) {
													throw new IllegalStateException( "Hibernate does not support composites mapped to multiple tables" );
												}
											}
											localColumnNamesCopy.add( columnExpression );
											localJdbcMappingsCopy.add( jdbcMapping );
										}
								);
							},
							null
					);

					this.tableName = tableNameBuff.getValue();
					assert this.tableName != null;

					assert ! columnItr.hasNext();

					return attributesCopy;
				},
				embeddableMappingType -> this,
				sessionFactory
		);

		this.columnNames = localColumnNamesCopy;
		this.jdbcMappings = localJdbcMappingsCopy;

		if ( MappingModelCreationLogger.DEBUG_ENABLED ) {
			final StringBuilder referringColumnList = new StringBuilder( columnNames.get( 0 ) );
			final StringBuilder targetColumnList = new StringBuilder( referencedComposite.getMappedColumnExpressions().get( 0 ) );
			for ( int i = 1; i < columnCount; i++ ) {
				referringColumnList.append( ", " ).append( columnNames.get( i ) );
				targetColumnList.append( ", " ).append( referencedComposite.getMappedColumnExpressions().get( i ) );
			}
			MappingModelCreationLogger.LOGGER.debugf(
					"Creating composite to-one foreign-key [%s]: %s.(%s) -> %s.(%s)",
					getAttributeMapping().getNavigableRole().getFullPath(),
					tableName,
					referringColumnList,
					referencedComposite.getContainingTableExpression(),
					targetColumnList
			);
		}

		return new ForeignKeyComposite(
				this,
				tableName,
				this.columnNames,
				referencedComposite,
				referencedComposite.getContainingTableExpression(),
				referencedComposite.getMappedColumnExpressions(),
				this.jdbcMappings,
				creationProcess
		);
	}


	@Override
	public void visitColumns(ColumnConsumer consumer) {
		embeddable.visitColumns( consumer );
	}

	@Override
	public Expression toSqlExpression(
			TableGroup tableGroup,
			Clause clause,
			SqmToSqlAstConverter walker,
			SqlAstCreationState sqlAstCreationState) {
		final List<Expression> sqlExpressions = new ArrayList<>();

		visitColumns(
				(containingTableExpression, columnExpression, jdbcMapping) -> {
					final TableReference tableReference = tableGroup.getTableReference( containingTableExpression );
					sqlExpressions.add(
							new ColumnReference(
									tableReference,
									columnExpression,
									jdbcMapping,
									sqlAstCreationState.getCreationContext().getSessionFactory()
							)
					);
				}
		);

		return new SqlTuple( sqlExpressions, this );
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
	public ModelPart findSubPart(String name, EntityMappingType treatTargetType) {
		return embeddable.findSubPart( name, treatTargetType );
	}

	@Override
	public void visitSubParts(Consumer<ModelPart> consumer, EntityMappingType treatTargetType) {
		embeddable.visitSubParts( consumer, treatTargetType );
	}

	@Override
	public ForeignKeyComposite getForeignKeyDescriptor() {
		return (ForeignKeyComposite) super.getForeignKeyDescriptor();
	}

	@Override
	public ForeignKeyDirection getDirection() {
		return ForeignKeyDirection.REFERRING;
	}

	@Override
	public String getSqlAliasStem() {
		return getAttributeMapping().getSqlAliasStem();
	}

	@Override
	public FetchOptions getMappedFetchOptions() {
		return this;
	}

	@Override
	public FetchStyle getStyle() {
		return FetchStyle.JOIN;
	}

	@Override
	public FetchTiming getTiming() {
		return FetchTiming.IMMEDIATE;
	}

	@Override
	public int getNumberOfFetchables() {
		return embeddable.getNumberOfFetchables();
	}

	@Override
	public MappingType getPartMappingType() {
		return embeddable;
	}

	@Override
	public JavaTypeDescriptor<?> getJavaTypeDescriptor() {
		return embeddable.getJavaTypeDescriptor();
	}

	@Override
	public EntityMappingType findContainingEntityMapping() {
		return embeddable.findContainingEntityMapping();
	}

	@Override
	public SingularAttributeMapping getParentInjectionAttributeMapping() {
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
		fetchablePath =	fetchablePath.append( PART_NAME );

		final SqlAstCreationState sqlAstCreationState = creationState.getSqlAstCreationState();
		final FromClauseAccess fromClauseAccess = sqlAstCreationState.getFromClauseAccess();

		if ( selected ) {
			// can never return null, but just want to "hide" it as an assertion
			assert fromClauseAccess.getTableGroup( fetchablePath ) != null;

			return new EmbeddableFetchImpl(
					fetchablePath,
					this,
					fetchParent,
					fetchTiming,
					true,
					false,
					creationState
			);
		}

		return new EmbeddableKeyFetch(
				fetchablePath,
				this,
				fetchTiming,
				fetchParent,
				creationState
		);
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
		return new TableGroupJoin(
				navigablePath,
				sqlAstJoinType,
				new CompositeTableGroup( navigablePath, this, lhs )
		);
	}
}
