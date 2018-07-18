/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.util.Collections;
import java.util.List;

import org.hibernate.boot.model.domain.BasicValueMapping;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.mapping.RootClass;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractSingularPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.BasicValuedNavigable;
import org.hibernate.metamodel.model.domain.spi.EntityIdentifierSimple;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.results.internal.ScalarQueryResultImpl;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.descriptor.spi.ValueExtractor;
import org.hibernate.type.spi.BasicType;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class EntityIdentifierSimpleImpl<O,J>
		extends AbstractSingularPersistentAttribute<O,J>
		implements EntityIdentifierSimple<O,J>, BasicValuedNavigable<J> {

	private final String name;
	private final Column column;
	private final BasicType<J> basicType;
	private final IdentifierGenerator identifierGenerator;

	@SuppressWarnings({"unchecked", "WeakerAccess"})
	public EntityIdentifierSimpleImpl(
			EntityHierarchyImpl runtimeModelHierarchy,
			RootClass bootModelRootEntity,
			RuntimeModelCreationContext creationContext) {
		super(
				runtimeModelHierarchy.getRootEntityType(),
				bootModelRootEntity.getIdentifierAttributeMapping(),
				runtimeModelHierarchy.getRootEntityType().getRepresentationStrategy().generatePropertyAccess(
						bootModelRootEntity,
						bootModelRootEntity.getIdentifierAttributeMapping(),
						runtimeModelHierarchy.getRootEntityType(),
						creationContext.getSessionFactory().getSessionFactoryOptions().getBytecodeProvider()
				),
				Disposition.ID
		);

		this.name = bootModelRootEntity.getIdentifierAttributeMapping().getName();

		final BasicValueMapping<J> basicValueMapping = (BasicValueMapping<J>) bootModelRootEntity.getIdentifierAttributeMapping().getValueMapping();
		this.column = creationContext.getDatabaseObjectResolver().resolveColumn( basicValueMapping.getMappedColumn() );
		this.basicType = basicValueMapping.resolveType();
		this.identifierGenerator = creationContext.getSessionFactory().getIdentifierGenerator( bootModelRootEntity.getEntityName() );
	}

	@Override
	public boolean hasSingleIdAttribute() {
		return true;
	}

	@Override
	public List<Column> getColumns() {
		return Collections.singletonList( column );
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
	public Column getBoundColumn() {
		return column;
	}

	@Override
	public BasicType<J> getBasicType() {
		return basicType;
	}

	@Override
	public BasicJavaDescriptor<J> getJavaTypeDescriptor() {
		return (BasicJavaDescriptor<J>) super.getJavaTypeDescriptor();
	}

	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	@Override
	public String asLoggableText() {
		return "IdentifierSimple(" + getContainer().asLoggableText() + ")";
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return PersistentAttributeType.BASIC;
	}

	@Override
	public PersistenceType getPersistenceType() {
		return PersistenceType.BASIC;
	}

	@Override
	public void visitNavigable(NavigableVisitationStrategy visitor) {
		visitor.visitSimpleIdentifier( this );
	}

	@Override
	public QueryResult createQueryResult(
			NavigableReference navigableReference,
			String resultVariable,
			QueryResultCreationContext creationContext) {
		return new ScalarQueryResultImpl(
				resultVariable,
				creationContext.getSqlSelectionResolver().resolveSqlSelection(
						creationContext.getSqlSelectionResolver().resolveSqlExpression(
								navigableReference.getSqlExpressionQualifier(),
								column
						),
						getJavaTypeDescriptor(),
						creationContext.getSessionFactory().getTypeConfiguration()
				),
				this
		);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ValueBinder getValueBinder(TypeConfiguration typeConfiguration) {
		return basicType.getValueBinder( typeConfiguration );
	}

	@Override
	public ValueExtractor getValueExtractor(TypeConfiguration typeConfiguration) {
		return basicType.getValueExtractor( typeConfiguration );
	}

	@Override
	public boolean isOptional() {
		return false;
	}

	@Override
	public Object unresolve(Object value, SharedSessionContractImplementor session) {
		return value;
	}

	@Override
	public void dehydrate(
			Object value,
			JdbcValueCollector jdbcValueCollector,
			SharedSessionContractImplementor session) {
		jdbcValueCollector.collect( value, this, getBoundColumn() );
	}

	@Override
	public Object resolveHydratedState(
			Object hydratedForm,
			ResolutionContext resolutionContext,
			SharedSessionContractImplementor session,
			Object containerInstance) {
		return hydratedForm;
	}
}
