/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.MappingException;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.SharedSessionContract;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.FetchTiming;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.mapping.Any;
import org.hibernate.mapping.BasicValue;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.IndexedConsumer;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Value;
import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.AttributeMetadata;
import org.hibernate.metamodel.mapping.AttributeMetadataAccess;
import org.hibernate.metamodel.mapping.EmbeddableMappingType;
import org.hibernate.metamodel.mapping.EmbeddableValuedModelPart;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.mapping.SelectableConsumer;
import org.hibernate.metamodel.mapping.SelectableMapping;
import org.hibernate.metamodel.mapping.SelectableMappings;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.spi.EmbeddableRepresentationStrategy;
import org.hibernate.metamodel.spi.RuntimeModelCreationContext;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.spi.NavigablePath;
import org.hibernate.sql.ast.Clause;
import org.hibernate.sql.ast.tree.from.TableGroup;
import org.hibernate.sql.ast.tree.from.TableGroupProducer;
import org.hibernate.sql.results.graph.DomainResult;
import org.hibernate.sql.results.graph.DomainResultCreationState;
import org.hibernate.sql.results.graph.Fetchable;
import org.hibernate.sql.results.graph.embeddable.internal.EmbeddableResultImpl;
import org.hibernate.type.AnyType;
import org.hibernate.type.BasicType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.spi.CompositeTypeImplementor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Describes a "normal" embeddable.
 *
 * NOTE: At the moment, this class is used to describe some non-normal cases: mainly
 * composite fks
 */
public class EmbeddableMappingTypeImpl extends AbstractEmbeddableMapping implements SelectableMappings {

	public static EmbeddableMappingTypeImpl from(
			Component bootDescriptor,
			CompositeType compositeType,
			boolean[] insertability,
			boolean[] updateability,
			Function<EmbeddableMappingType, EmbeddableValuedModelPart> embeddedPartBuilder,
			MappingModelCreationProcess creationProcess) {
		return from( bootDescriptor, compositeType, null, null, insertability, updateability, embeddedPartBuilder, creationProcess );
	}

	public static EmbeddableMappingTypeImpl from(
			Component bootDescriptor,
			CompositeType compositeType,
			String rootTableExpression,
			String[] rootTableKeyColumnNames,
			boolean[] insertability,
			boolean[] updateability,
			Function<EmbeddableMappingType,EmbeddableValuedModelPart> embeddedPartBuilder,
			MappingModelCreationProcess creationProcess) {
		final RuntimeModelCreationContext creationContext = creationProcess.getCreationContext();

		final EmbeddableMappingTypeImpl mappingType = new EmbeddableMappingTypeImpl(
				bootDescriptor,
				embeddedPartBuilder,
				creationContext
		);

		if ( compositeType instanceof CompositeTypeImplementor ) {
			( (CompositeTypeImplementor) compositeType ).injectMappingModelPart( mappingType.getEmbeddedValueMapping(), creationProcess );
		}

		creationProcess.registerInitializationCallback(
				"EmbeddableMappingType(" + mappingType.getNavigableRole().getFullPath() + ")#finishInitialization",
				() ->
						mappingType.finishInitialization(
								bootDescriptor,
								compositeType,
								rootTableExpression,
								rootTableKeyColumnNames,
								insertability,
								updateability,
								creationProcess
						)
		);

		return mappingType;
	}

	private final JavaType<?> embeddableJtd;
	private final EmbeddableRepresentationStrategy representationStrategy;

	private final List<AttributeMapping> attributeMappings = new ArrayList<>();
	private SelectableMappings selectableMappings;

	private final EmbeddableValuedModelPart valueMapping;

	private final boolean createEmptyCompositesEnabled;

	private EmbeddableMappingTypeImpl(
			Component bootDescriptor,
			Function<EmbeddableMappingType, EmbeddableValuedModelPart> embeddedPartBuilder,
			RuntimeModelCreationContext creationContext) {
		super( creationContext );
		this.representationStrategy = creationContext
				.getBootstrapContext()
				.getRepresentationStrategySelector()
				.resolveStrategy( bootDescriptor, () -> this, creationContext );

		this.embeddableJtd = representationStrategy.getMappedJavaType();
		this.valueMapping = embeddedPartBuilder.apply( this );

		final ConfigurationService cs = sessionFactory.getServiceRegistry()
				.getService(ConfigurationService.class);

		this.createEmptyCompositesEnabled = ConfigurationHelper.getBoolean(
				Environment.CREATE_EMPTY_COMPOSITES_ENABLED,
				cs.getSettings(),
				false
		);

	}

	public EmbeddableMappingTypeImpl(
			EmbeddedAttributeMapping valueMapping,
			TableGroupProducer declaringTableGroupProducer,
			SelectableMappings selectableMappings,
			EmbeddableMappingType inverseMappingType,
			MappingModelCreationProcess creationProcess) {
		super( creationProcess );

		this.embeddableJtd = inverseMappingType.getJavaType();
		this.representationStrategy = inverseMappingType.getRepresentationStrategy();
		this.valueMapping = valueMapping;
		this.createEmptyCompositesEnabled = inverseMappingType.isCreateEmptyCompositesEnabled();
		this.selectableMappings = selectableMappings;
		creationProcess.registerInitializationCallback(
				"EmbeddableMappingType(" + inverseMappingType.getNavigableRole().getFullPath() + ".{inverse})#finishInitialization",
				() -> inverseInitializeCallback(
						declaringTableGroupProducer,
						selectableMappings,
						inverseMappingType,
						creationProcess,
						valueMapping.getDeclaringType(),
						this.attributeMappings
				)
		);
	}

	public EmbeddableMappingType createInverseMappingType(
			EmbeddedAttributeMapping valueMapping,
			TableGroupProducer declaringTableGroupProducer,
			SelectableMappings selectableMappings,
			MappingModelCreationProcess creationProcess) {
		return new EmbeddableMappingTypeImpl(
				valueMapping,
				declaringTableGroupProducer,
				selectableMappings,
				this,
				creationProcess
		);
	}

	private boolean finishInitialization(
			Component bootDescriptor,
			CompositeType compositeType,
			String rootTableExpression,
			String[] rootTableKeyColumnNames,
			boolean[] insertability,
			boolean[] updateability,
			MappingModelCreationProcess creationProcess) {
// for some reason I cannot get this to work, though only a single test fails - `CompositeElementTest`
//		return finishInitialization(
//				getNavigableRole(),
//				bootDescriptor,
//				compositeType,
//				rootTableExpression,
//				rootTableKeyColumnNames,
//				this,
//				representationStrategy,
//				(name, type) -> {},
//				(column, jdbcEnvironment) -> getTableIdentifierExpression(
//						column.getValue().getTable(),
//						jdbcEnvironment
//				),
//				this::addAttribute,
//				() -> {
//					// We need the attribute mapping types to finish initialization first before we can build the column mappings
//					creationProcess.registerInitializationCallback(
//							"EmbeddableMappingType(" + getEmbeddedValueMapping().getNavigableRole().getFullPath() + ")#initColumnMappings",
//							this::initColumnMappings
//					);
//				},
//				creationProcess
//		);
// todo (6.0) - get this ^^ to work, or drop the comment

		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
		final TypeConfiguration typeConfiguration = sessionFactory.getTypeConfiguration();
		final JdbcServices jdbcServices = sessionFactory.getJdbcServices();
		final JdbcEnvironment jdbcEnvironment = jdbcServices.getJdbcEnvironment();
		final Dialect dialect = jdbcEnvironment.getDialect();

		final String baseTableExpression = valueMapping.getContainingTableExpression();
		final Type[] subtypes = compositeType.getSubtypes();

		int attributeIndex = 0;
		int columnPosition = 0;

		// Reset the attribute mappings that were added in previous attempts
		this.attributeMappings.clear();

		for ( Property bootPropertyDescriptor : bootDescriptor.getProperties() ) {
			final AttributeMapping attributeMapping;

			final Type subtype = subtypes[attributeIndex];
			final Value value = bootPropertyDescriptor.getValue();
			if ( subtype instanceof BasicType ) {
				final BasicValue basicValue = (BasicValue) value;
				final Selectable selectable = basicValue.getColumn();
				final String containingTableExpression;
				final String columnExpression;
				if ( rootTableKeyColumnNames == null ) {
					if ( selectable.isFormula() ) {
						columnExpression = selectable.getTemplate(
								dialect,
								creationProcess.getCreationContext().getTypeConfiguration(),
								creationProcess.getSqmFunctionRegistry()
						);
					}
					else {
						columnExpression = selectable.getText( dialect );
					}
					if ( selectable instanceof Column ) {
						containingTableExpression = MappingModelCreationHelper.getTableIdentifierExpression(
								( (Column) selectable ).getValue().getTable(),
								creationProcess
						);
					}
					else {
						containingTableExpression = baseTableExpression;
					}
				}
				else {
					containingTableExpression = rootTableExpression;
					columnExpression = rootTableKeyColumnNames[columnPosition];
				}
				final String columnDefinition;
				final Long length;
				final Integer precision;
				final Integer scale;
				final boolean nullable;
				if ( selectable instanceof Column ) {
					Column column = (Column) selectable;
					columnDefinition = column.getSqlType();
					length = column.getLength();
					precision = column.getPrecision();
					scale = column.getScale();
					nullable = column.isNullable();
				}
				else {
					columnDefinition = null;
					length = null;
					precision = null;
					scale = null;
					nullable = true;
				}
				attributeMapping = MappingModelCreationHelper.buildBasicAttributeMapping(
						bootPropertyDescriptor.getName(),
						valueMapping.getNavigableRole().append( bootPropertyDescriptor.getName() ),
						attributeIndex,
						bootPropertyDescriptor,
						this,
						(BasicType<?>) subtype,
						containingTableExpression,
						columnExpression,
						selectable.isFormula(),
						selectable.getCustomReadExpression(),
						selectable.getCustomWriteExpression(),
						columnDefinition,
						length,
						precision,
						scale,
						nullable,
						insertability[columnPosition],
						updateability[columnPosition],
						representationStrategy.resolvePropertyAccess( bootPropertyDescriptor ),
						compositeType.getCascadeStyle( attributeIndex ),
						creationProcess
				);

				columnPosition++;
			}
			else if ( subtype instanceof AnyType ) {
				final Any bootValueMapping = (Any) value;
				final AnyType anyType = (AnyType) subtype;

				final PropertyAccess propertyAccess = representationStrategy.resolvePropertyAccess( bootPropertyDescriptor );
				final boolean nullable = bootValueMapping.isNullable();
				final boolean insertable = insertability[columnPosition];
				final boolean updateable = updateability[columnPosition];
				final boolean includeInOptimisticLocking = bootPropertyDescriptor.isOptimisticLocked();
				final CascadeStyle cascadeStyle = compositeType.getCascadeStyle( attributeIndex );
				final MutabilityPlan<?> mutabilityPlan;

				if ( updateable ) {
					mutabilityPlan = new MutabilityPlan<>() {
						@Override
						public boolean isMutable() {
							return true;
						}

						@Override
						public Object deepCopy(Object value) {
							if ( value == null ) {
								return null;
							}

							return anyType.deepCopy( value, creationProcess.getCreationContext().getSessionFactory() );
						}

						@Override
						public Serializable disassemble(Object value, SharedSessionContract session) {
							throw new NotYetImplementedFor6Exception( getClass() );
						}

						@Override
						public Object assemble(Serializable cached, SharedSessionContract session) {
							throw new NotYetImplementedFor6Exception( getClass() );
						}
					};
				}
				else {
					mutabilityPlan = ImmutableMutabilityPlan.INSTANCE;
				}

				final AttributeMetadataAccess attributeMetadataAccess = entityMappingType -> new AttributeMetadata() {
					@Override
					public PropertyAccess getPropertyAccess() {
						return propertyAccess;
					}

					@Override
					public MutabilityPlan<?> getMutabilityPlan() {
						return mutabilityPlan;
					}

					@Override
					public boolean isNullable() {
						return nullable;
					}

					@Override
					public boolean isInsertable() {
						return insertable;
					}

					@Override
					public boolean isUpdatable() {
						return updateable;
					}

					@Override
					public boolean isIncludedInDirtyChecking() {
						// todo (6.0) : do not believe this is correct
						return updateable;
					}

					@Override
					public boolean isIncludedInOptimisticLocking() {
						return includeInOptimisticLocking;
					}

					@Override
					public CascadeStyle getCascadeStyle() {
						return cascadeStyle;
					}
				};

				attributeMapping = new DiscriminatedAssociationAttributeMapping(
						valueMapping.getNavigableRole().append( bootPropertyDescriptor.getName() ),
						typeConfiguration.getJavaTypeRegistry().getDescriptor( Object.class ),
						this,
						attributeIndex,
						attributeMetadataAccess,
						bootPropertyDescriptor.isLazy() ? FetchTiming.DELAYED : FetchTiming.IMMEDIATE,
						propertyAccess,
						bootPropertyDescriptor,
						anyType,
						bootValueMapping,
						creationProcess
				);
			}
			else if ( subtype instanceof CompositeType ) {
				final CompositeType subCompositeType = (CompositeType) subtype;
				final int columnSpan = subCompositeType.getColumnSpan( sessionFactory );
				final String subTableExpression;
				final String[] subRootTableKeyColumnNames;
				if ( rootTableKeyColumnNames == null ) {
					subTableExpression = baseTableExpression;
					subRootTableKeyColumnNames = null;
				}
				else {
					subTableExpression = rootTableExpression;
					subRootTableKeyColumnNames = new String[columnSpan];
					System.arraycopy( rootTableKeyColumnNames, columnPosition, subRootTableKeyColumnNames, 0, columnSpan );
				}

				attributeMapping = MappingModelCreationHelper.buildEmbeddedAttributeMapping(
						bootPropertyDescriptor.getName(),
						attributeIndex,
						bootPropertyDescriptor,
						this,
						subCompositeType,
						subTableExpression,
						subRootTableKeyColumnNames,
						representationStrategy.resolvePropertyAccess( bootPropertyDescriptor ),
						compositeType.getCascadeStyle( attributeIndex ),
						creationProcess
				);

				columnPosition += columnSpan;
			}
			else if ( subtype instanceof CollectionType ) {
				final EntityPersister entityPersister = creationProcess.getEntityPersister( bootDescriptor.getOwner().getEntityName() );

				attributeMapping = MappingModelCreationHelper.buildPluralAttributeMapping(
						bootPropertyDescriptor.getName(),
						attributeIndex,
						bootPropertyDescriptor,
						entityPersister,
						representationStrategy.resolvePropertyAccess( bootPropertyDescriptor ),
						compositeType.getCascadeStyle( attributeIndex),
						compositeType.getFetchMode( attributeIndex ),
						creationProcess
				);
			}
			else if ( subtype instanceof EntityType ) {
				final EntityPersister entityPersister = creationProcess.getEntityPersister( bootDescriptor.getOwner().getEntityName() );

				attributeMapping = MappingModelCreationHelper.buildSingularAssociationAttributeMapping(
						bootPropertyDescriptor.getName(),
						valueMapping.getNavigableRole().append( bootPropertyDescriptor.getName() ),
						attributeIndex,
						bootPropertyDescriptor,
						entityPersister,
						entityPersister,
						(EntityType) subtype,
						getRepresentationStrategy().resolvePropertyAccess( bootPropertyDescriptor ),
						compositeType.getCascadeStyle( attributeIndex ),
						creationProcess
				);
				columnPosition += bootPropertyDescriptor.getColumnSpan();
			}
			else {
				throw new MappingException(
						String.format(
								Locale.ROOT,
								"Unable to determine attribute nature : %s#%s",
								bootDescriptor.getOwner().getEntityName(),
								bootPropertyDescriptor.getName()
						)
				);
			}

			addAttribute( attributeMapping );

			attributeIndex++;
		}

		// We need the attribute mapping types to finish initialization first before we can build the column mappings
		creationProcess.registerInitializationCallback(
				"EmbeddableMappingType(" + getEmbeddedValueMapping().getNavigableRole().getFullPath() + ")#initColumnMappings",
				this::initColumnMappings
		);

		return true;
	}

	private boolean initColumnMappings() {
		this.selectableMappings = SelectableMappingsImpl.from( this );
		return true;
	}

	private void addAttribute(AttributeMapping attributeMapping) {
		// check if we've already seen this attribute...
		for ( int i = 0; i < attributeMappings.size(); i++ ) {
			final AttributeMapping previous = attributeMappings.get( i );
			if ( attributeMapping.getAttributeName().equals( previous.getAttributeName() ) ) {
				attributeMappings.set( i, attributeMapping );
				return;
			}
		}

		attributeMappings.add( attributeMapping );
	}

	public EmbeddableValuedModelPart getEmbeddedValueMapping() {
		return valueMapping;
	}

	@Override
	public JavaType<?> getMappedJavaType() {
		return embeddableJtd;
	}

	public EmbeddableRepresentationStrategy getRepresentationStrategy() {
		return representationStrategy;
	}

	@Override
	public String getPartName() {
		return getEmbeddedValueMapping().getPartName();
	}

	@Override
	public NavigableRole getNavigableRole() {
		return valueMapping.getNavigableRole();
	}

	@Override
	public <T> DomainResult<T> createDomainResult(
			NavigablePath navigablePath,
			TableGroup tableGroup,
			String resultVariable,
			DomainResultCreationState creationState) {
		return new EmbeddableResultImpl<>(
				navigablePath,
				valueMapping,
				resultVariable,
				creationState
		);
	}

	@Override
	public int getNumberOfFetchables() {
		return attributeMappings.size();
	}

	@Override
	public void visitFetchables(Consumer<Fetchable> fetchableConsumer, EntityMappingType treatTargetType) {
		visitAttributeMappings( fetchableConsumer );
	}

	@Override
	public SelectableMapping getSelectable(int columnIndex) {
		return getSelectableMappings().getSelectable( columnIndex );
	}

	@Override
	public int getJdbcTypeCount() {
		return getSelectableMappings().getJdbcTypeCount();
	}

	@Override
	public List<JdbcMapping> getJdbcMappings() {
		return getSelectableMappings().getJdbcMappings();
	}

	@Override
	public int forEachJdbcType(int offset, IndexedConsumer<JdbcMapping> action) {
		return getSelectableMappings().forEachSelectable(
				offset,
				(index, selectable) -> action.accept( index, selectable.getJdbcMapping() )
		);
	}

	@Override
	public void breakDownJdbcValues(Object domainValue, JdbcValueConsumer valueConsumer, SharedSessionContractImplementor session) {
		if ( domainValue instanceof Object[] ) {
			final Object[] values = (Object[]) domainValue;
			assert values.length == attributeMappings.size();

			for ( int i = 0; i < attributeMappings.size(); i++ ) {
				final AttributeMapping attributeMapping = attributeMappings.get( i );
				final Object attributeValue = values[ i ];
				attributeMapping.breakDownJdbcValues( attributeValue, valueConsumer, session );
			}
		}
		else {
			attributeMappings.forEach( (attributeMapping) -> {
				if ( attributeMapping instanceof PluralAttributeMapping ) {
					return;
				}

				final Object attributeValue = domainValue == null
						? null
						: attributeMapping.getPropertyAccess().getGetter().get( domainValue );
				attributeMapping.breakDownJdbcValues( attributeValue, valueConsumer, session );
			} );
		}
	}

	@Override
	public Object disassemble(Object value, SharedSessionContractImplementor session) {
		final List<AttributeMapping> attributeMappings = getAttributeMappings();

		final Object[] result = new Object[ attributeMappings.size() ];
		for ( int i = 0; i < attributeMappings.size(); i++ ) {
			final AttributeMapping attributeMapping = attributeMappings.get( i );
			Object o = attributeMapping.getPropertyAccess().getGetter().get( value );
			result[i] = attributeMapping.disassemble( o, session );
		}

		return result;
	}

	@Override
	public int forEachJdbcValue(
			Object value,
			Clause clause,
			int offset,
			JdbcValuesConsumer consumer,
			SharedSessionContractImplementor session) {
		int span = 0;

		for ( int i = 0; i < attributeMappings.size(); i++ ) {
			final AttributeMapping attributeMapping = attributeMappings.get( i );
			if ( attributeMapping instanceof PluralAttributeMapping ) {
				continue;
			}
			final Object o = attributeMapping.getPropertyAccess().getGetter().get( value );
			span += attributeMapping.forEachJdbcValue( o, clause, span + offset, consumer, session );
		}
		return span;
	}

	@Override
	public int forEachDisassembledJdbcValue(
			Object value,
			Clause clause,
			int offset,
			JdbcValuesConsumer valuesConsumer,
			SharedSessionContractImplementor session) {
		final Object[] values = (Object[]) value;
		int span = 0;
		for ( int i = 0; i < attributeMappings.size(); i++ ) {
			final AttributeMapping mapping = attributeMappings.get( i );
			span += mapping.forEachDisassembledJdbcValue( values[i], clause, span + offset, valuesConsumer, session );
		}
		return span;
	}

	@Override
	public int forEachSelectable(SelectableConsumer consumer) {
		return getSelectableMappings().forEachSelectable( 0, consumer );
	}

	@Override
	public int forEachSelectable(int offset, SelectableConsumer consumer) {
		return getSelectableMappings().forEachSelectable( offset, consumer );
	}

	private SelectableMappings getSelectableMappings() {
		if (selectableMappings == null) {
			// This is expected to happen when processing a
			// PostInitCallbackEntry because the callbacks
			// are not ordered. The exception is caught in
			// MappingModelCreationProcess.executePostInitCallbacks()
			// and the callback is re-queued.
			throw new IllegalStateException("Not yet ready");
		}
		return selectableMappings;
	}

	@Override
	public EntityMappingType findContainingEntityMapping() {
		return valueMapping.findContainingEntityMapping();
	}

	@Override
	public int getNumberOfAttributeMappings() {
		return attributeMappings.size();
	}

	@Override
	public AttributeMapping getAttributeMapping(int position) {
		return attributeMappings.get( position );
	}

	@Override
	public AttributeMapping findAttributeMapping(String name) {
		for ( int i = 0; i < attributeMappings.size(); i++ ) {
			final AttributeMapping attr = attributeMappings.get( i );
			if ( name.equals( attr.getAttributeName() ) ) {
				return attr;
			}
		}
		return null;
	}

	@Override
	public List<AttributeMapping> getAttributeMappings() {
		return attributeMappings;
	}

	@Override
	public void forEachAttributeMapping(IndexedConsumer<AttributeMapping> consumer) {
		for ( int i = 0; i < attributeMappings.size(); i++ ) {
			consumer.accept( i, attributeMappings.get( i ) );
		}
	}

	@Override
	public void visitAttributeMappings(Consumer<? super AttributeMapping> action) {
		attributeMappings.forEach( action );
	}

	@Override
	public ModelPart findSubPart(String name, EntityMappingType treatTargetType) {
		return findAttributeMapping( name );
	}

	@Override
	public void visitSubParts(Consumer<ModelPart> consumer, EntityMappingType treatTargetType) {
		visitAttributeMappings( consumer );
	}

	public boolean isCreateEmptyCompositesEnabled() {
		return createEmptyCompositesEnabled;
	}
}
