/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.hibernate.boot.model.domain.BasicValueMapping;
import org.hibernate.boot.model.domain.PersistentAttributeMapping;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.spi.AbstractNonIdSingularPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.BasicValuedNavigable;
import org.hibernate.metamodel.model.domain.spi.ConvertibleNavigable;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.domain.spi.Readable;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.property.access.spi.PropertyAccess;
import org.hibernate.query.sqm.produce.spi.SqmCreationContext;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableContainerReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmNavigableReference;
import org.hibernate.query.sqm.tree.expression.domain.SqmSingularAttributeReferenceBasic;
import org.hibernate.query.sqm.tree.from.SqmFrom;
import org.hibernate.sql.JdbcValueMapper;
import org.hibernate.sql.ast.produce.metamodel.spi.BasicValuedExpressableType;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.tree.spi.expression.ColumnReference;
import org.hibernate.sql.ast.tree.spi.expression.domain.NavigableReference;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.internal.ScalarQueryResultImpl;
import org.hibernate.sql.results.spi.QueryResult;
import org.hibernate.sql.results.spi.QueryResultCreationContext;
import org.hibernate.sql.results.spi.SqlSelectionResolutionContext;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.spi.Util;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.descriptor.spi.ValueExtractor;
import org.hibernate.type.spi.BasicType;
import org.hibernate.type.spi.TypeConfiguration;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class BasicSingularPersistentAttribute<O, J>
		extends AbstractNonIdSingularPersistentAttribute<O, J>
		implements BasicValuedNavigable<J>, ConvertibleNavigable<J>, ValueBinder, ValueExtractor<J> {
	private static final Logger log = Logger.getLogger( BasicSingularPersistentAttribute.class );

	private final Column boundColumn;
	private final BasicType<J> basicType;
	private final JdbcValueMapper jdbcValueMapper;
	private final BasicValueConverter valueConverter;

	@SuppressWarnings("unchecked")
	public BasicSingularPersistentAttribute(
			ManagedTypeDescriptor<O> runtimeContainer,
			PersistentAttributeMapping bootAttribute,
			PropertyAccess propertyAccess,
			Disposition disposition,
			RuntimeModelCreationContext context) {
		super(
				runtimeContainer,
				bootAttribute,
				propertyAccess,
				disposition
		);

		final BasicValueMapping<J> basicValueMapping = (BasicValueMapping<J>) bootAttribute.getValueMapping();
		this.boundColumn = context.getDatabaseObjectResolver().resolveColumn( basicValueMapping.getMappedColumn() );
		this.basicType = basicValueMapping.resolveType();

		this.valueConverter = basicValueMapping.resolveValueConverter( context, basicType );

		if ( valueConverter != null ) {
			log.debugf(
					"BasicValueConverter [%s] being applied for basic attribute : %s",
					valueConverter,
					getNavigableRole()
			);

			jdbcValueMapper = basicType.getSqlTypeDescriptor().getJdbcValueMapper(
					valueConverter.getRelationalJavaDescriptor(),
					context.getSessionFactory().getTypeConfiguration()
			);
		}
		else {
			jdbcValueMapper = basicType.getSqlTypeDescriptor().getJdbcValueMapper(
					basicType.getJavaTypeDescriptor(),
					context.getSessionFactory().getTypeConfiguration()
			);
		}

		instantiationComplete( bootAttribute, context );
	}


	@Override
	public SingularAttributeClassification getAttributeTypeClassification() {
		return SingularAttributeClassification.BASIC;
	}

	@Override
	public BasicValuedExpressableType<J> getType() {
		return (BasicValuedExpressableType<J>) super.getType();
	}

	@Override
	public BasicJavaDescriptor<J> getJavaTypeDescriptor() {
		return (BasicJavaDescriptor<J>) super.getJavaTypeDescriptor();
	}

	@Override
	public SqmNavigableReference createSqmExpression(
			SqmFrom sourceSqmFrom,
			SqmNavigableContainerReference containerReference,
			SqmCreationContext creationContext) {
		return new SqmSingularAttributeReferenceBasic( containerReference, this, creationContext );
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
								boundColumn
						),
						getJavaTypeDescriptor(),
						creationContext.getSessionFactory().getTypeConfiguration()
				),
				getType()
		);
	}

	@Override
	public Column getBoundColumn() {
		return boundColumn;
	}

	@Override
	public String asLoggableText() {
		return "SingularAttributeBasic(" + getContainer().asLoggableText() + '.' + getAttributeName() + ')';
	}

	@Override
	public BasicValueConverter getValueConverter() {
		return valueConverter;
	}

	@Override
	public PersistentAttributeType getPersistentAttributeType() {
		return PersistentAttributeType.BASIC;
	}

	@Override
	public void visitNavigable(NavigableVisitationStrategy visitor) {
		visitor.visitSingularAttributeBasic( this );
	}

	@Override
	public BasicType<J> getBasicType() {
		return basicType;
	}


	@Override
	public ValueBinder getValueBinder(TypeConfiguration typeConfiguration) {
		return this;
	}

	@Override
	public ValueExtractor getValueExtractor(TypeConfiguration typeConfiguration) {
		return this;
	}

//	@Override
//	public Object hydrate(Object jdbcValues, SharedSessionContractImplementor session) {
//		return jdbcValues;
//	}

	@Override
	@SuppressWarnings("unchecked")
	public Object resolveHydratedState(
			Object hydratedForm,
			Readable.ResolutionContext resolutionContext,
			SharedSessionContractImplementor session,
			Object containerInstance) {
		if ( valueConverter != null ) {
			return valueConverter.toDomainValue( hydratedForm, session );
		}
		return hydratedForm;
	}

	@Override
	public List<Column> getColumns() {
		return Collections.singletonList( getBoundColumn() );
	}

	@Override
	public List<ColumnReference> resolveColumnReferences(
			ColumnReferenceQualifier qualifier,
			SqlSelectionResolutionContext resolutionContext) {
		return Collections.singletonList(
				qualifier.resolveColumnReference( getBoundColumn() )
		);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object unresolve(Object value, SharedSessionContractImplementor session) {
		return value;
	}

	@Override
	public void dehydrate(
			Object value,
			JdbcValueCollector jdbcValueCollector,
			SharedSessionContractImplementor session) {
//		if ( valueConverter != null ) {
//			value = valueConverter.toRelationalValue( value, session );
//		}
		jdbcValueCollector.collect( value, this, getBoundColumn() );
	}



	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// ValueBinder

	@Override
	@SuppressWarnings("unchecked")
	public void bind(
			PreparedStatement st,
			int position,
			Object value,
			ExecutionContext executionContext) throws SQLException {
		final Object bindValue = bindValue( value, executionContext.getSession() );

		jdbcValueMapper.getJdbcValueBinder().bind( st, position, bindValue, executionContext );
	}

	@SuppressWarnings("unchecked")
	private Object bindValue(Object value, SharedSessionContractImplementor session) {
		return valueConverter == null
				? value
				: valueConverter.toRelationalValue( value, session );
	}

	@Override
	@SuppressWarnings("unchecked")
	public void bind(
			PreparedStatement st,
			String name,
			Object value,
			ExecutionContext executionContext) throws SQLException {
		final CallableStatement callable = Util.asCallableStatementForNamedParam( st );
		final Object bindValue = bindValue( value, executionContext.getSession() );

		jdbcValueMapper.getJdbcValueBinder().bind( callable, name, bindValue, executionContext );
	}

	@Override
	@SuppressWarnings("unchecked")
	public J extract(ResultSet rs, int position, ExecutionContext executionContext) throws SQLException {
		final Object value = jdbcValueMapper.getJdbcValueExtractor().extract( rs, position, executionContext );
		return extractValue( executionContext, value );
	}

	@SuppressWarnings("unchecked")
	private J extractValue(ExecutionContext executionContext, Object value) {
		return (J) ( valueConverter == null
				? value
				: valueConverter.toDomainValue( value, executionContext.getSession() ) );
	}

	@Override
	public int getNumberOfJdbcParametersNeeded() {
		return 1;
	}

	@Override
	@SuppressWarnings("unchecked")
	public J extract(
			CallableStatement statement,
			int position,
			ExecutionContext executionContext) throws SQLException {
		final Object value = jdbcValueMapper.getJdbcValueExtractor().extract( statement, position, executionContext );
		return extractValue( executionContext, value );
	}

	@Override
	@SuppressWarnings("unchecked")
	public J extract(
			CallableStatement statement,
			String name,
			ExecutionContext executionContext) throws SQLException {
		final Object value = jdbcValueMapper.getJdbcValueExtractor().extract( statement, name, executionContext );
		return extractValue( executionContext, value );
	}
}
