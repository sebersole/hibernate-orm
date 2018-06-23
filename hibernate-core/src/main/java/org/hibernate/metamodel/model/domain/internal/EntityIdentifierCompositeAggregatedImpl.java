/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.TemporalType;

import org.hibernate.boot.model.domain.ValueMapping;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.mapping.RootClass;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.spi.AbstractSingularPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.EntityIdentifierCompositeAggregated;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.procedure.ParameterMisuseException;
import org.hibernate.query.sqm.AllowableParameterType;
import org.hibernate.sql.JdbcValueCollector;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.type.descriptor.java.spi.EmbeddableJavaDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class EntityIdentifierCompositeAggregatedImpl<O,J>
		extends AbstractSingularPersistentAttribute<O,J>
		implements EntityIdentifierCompositeAggregated<O,J> {
	private final EmbeddedTypeDescriptor<J> embeddedMetadata;
	private final IdentifierGenerator identifierGenerator;
	private final List<Column> columns;

	@SuppressWarnings("unchecked")
	public EntityIdentifierCompositeAggregatedImpl(
			EntityHierarchyImpl runtimeModelHierarchy,
			RootClass bootModelRootEntity,
			EmbeddedTypeDescriptor embeddedMetadata,
			RuntimeModelCreationContext creationContext) {
		super(
				runtimeModelHierarchy.getRootEntityType(),
				bootModelRootEntity.getIdentifierProperty(),
				embeddedMetadata.getRepresentationStrategy().generatePropertyAccess(
						bootModelRootEntity,
						bootModelRootEntity.getIdentifierProperty(),
						(ManagedTypeDescriptor<?>) embeddedMetadata.getContainer(),
						creationContext.getSessionFactory().getSessionFactoryOptions().getBytecodeProvider()
				),
				Disposition.ID
		);

		this.embeddedMetadata = embeddedMetadata;
		this.identifierGenerator = creationContext.getSessionFactory().getIdentifierGenerator( bootModelRootEntity.getEntityName() );

		final ValueMapping<?> valueMapping = bootModelRootEntity.getIdentifierAttributeMapping().getValueMapping();
		this.columns = valueMapping.getMappedColumns().stream()
				.map( creationContext.getDatabaseObjectResolver()::resolveColumn )
				.collect( Collectors.toList() );
	}

	@Override
	public EmbeddedTypeDescriptor<J> getEmbeddedDescriptor() {
		return embeddedMetadata;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// NavigableSource (embedded)

	@Override
	public NavigableRole getNavigableRole() {
		return embeddedMetadata.getNavigableRole();
	}

	@Override
	public EmbeddableJavaDescriptor<J> getJavaTypeDescriptor() {
		return embeddedMetadata.getJavaTypeDescriptor();
	}

	@Override
	public int getNumberOfJdbcParametersForRestriction() {
		return embeddedMetadata.getNumberOfJdbcParametersForRestriction();
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SingularAttribute


	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return PersistentAttributeType.EMBEDDED;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.EMBEDDABLE;
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.EMBEDDED;
	}

	@Override
	public String asLoggableText() {
		return "IdentifierCompositeAggregated(" + embeddedMetadata.asLoggableText() + ")";
	}

	@Override
	public QueryResult createQueryResult(
			NavigableReference expression,
			String resultVariable,
			QueryResultCreationContext creationContext) {
		return embeddedMetadata.createQueryResult(
				expression,
				resultVariable,
				creationContext
		);
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public SingularPersistentAttribute asAttribute(Class javaType) {
		return this;
	}

	@Override
	public IdentifierGenerator getIdentifierValueGenerator() {
		return identifierGenerator;
	}

	@Override
	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public <N> Navigable<N> findNavigable(String navigableName) {
		return getEmbeddedDescriptor().findNavigable( navigableName );
	}

	@Override
	public void visitNavigables(NavigableVisitationStrategy visitor) {
		getEmbeddedDescriptor().visitNavigables( visitor );
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public int getNumberOfJdbcParametersToBind() {
		return getColumns().size();
	}

	@Override
	public AllowableParameterType resolveTemporalPrecision(TemporalType temporalType, TypeConfiguration typeConfiguration) {
		throw new ParameterMisuseException( "Cannot apply temporal precision to embeddable value" );
	}

	@Override
	public Object unresolve(Object value, SharedSessionContractImplementor session) {
		final Object[] values = getEmbeddedDescriptor().getPropertyValues( value );
		getEmbeddedDescriptor().visitStateArrayContributors(
				contributor -> {
					final int index = contributor.getStateArrayPosition();
					values[index] = contributor.unresolve( values[index], session );
				}
		);

		return values;
	}

	@Override
	public void dehydrate(
			Object value,
			JdbcValueCollector jdbcValueCollector,
			SharedSessionContractImplementor session) {
		final Object[] values = (Object[]) value;
		getEmbeddedDescriptor().visitStateArrayContributors(
				contributor -> contributor.dehydrate(
						values[ contributor.getStateArrayPosition() ],
						jdbcValueCollector,
						session
				)
		);
	}
}
