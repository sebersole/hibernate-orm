/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.persistence.TemporalType;

import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.spi.EntityHierarchy;
import org.hibernate.metamodel.model.domain.spi.NaturalIdDescriptor;
import org.hibernate.metamodel.model.domain.spi.NavigableContainer;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.domain.spi.NonIdPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.Writeable;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.query.sqm.AllowableParameterType;
import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.JdbcValueCollector;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.tree.spi.expression.ColumnReference;
import org.hibernate.sql.ast.tree.spi.expression.Expression;
import org.hibernate.sql.ast.tree.spi.expression.SqlTuple;
import org.hibernate.sql.ast.tree.spi.from.TableGroup;
import org.hibernate.sql.results.internal.AggregateSqlSelectionGroupNode;
import org.hibernate.sql.results.spi.SqlSelectionGroupNode;
import org.hibernate.sql.results.spi.SqlSelectionResolutionContext;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.spi.TemporalJavaDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class NaturalIdDescriptorImpl<J> implements NaturalIdDescriptor<J>, AllowableParameterType<J>, Writeable<J,Object> {
	private final EntityHierarchy hierarchy;
	private final NaturalIdDataAccess cacheRegionAccess;
	private final NavigableRole navigableRole;

	private List<NaturalIdAttributeInfo> attributes;

	private JdbcValueBinder<J> valueBinder;
	private JdbcValueExtractor<J> valueExtractor;
	private Integer numberOfParameterBinds;


	public NaturalIdDescriptorImpl(
			EntityHierarchy hierarchy,
			NaturalIdDataAccess cacheRegionAccess) {
		this.hierarchy = hierarchy;
		this.cacheRegionAccess = cacheRegionAccess;

		navigableRole = hierarchy.getRootEntityType().getNavigableRole().append( "naturalId" );
	}

	public void injectAttributes(List<NonIdPersistentAttribute<?,?>> attributes) {
		if ( this.attributes == null ) {
			this.attributes = new ArrayList<>();
			this.numberOfParameterBinds = 0;
		}

		for ( int i = 0; i < attributes.size(); i++ ) {
			this.attributes.add( new NaturalIdAttributeInfoImpl( attributes.get( i ), i ) );
			this.numberOfParameterBinds += ( (AllowableParameterType) attributes.get( i ) ).getNumberOfJdbcParametersToBind();
		}
	}

	@Override
	public List<NaturalIdAttributeInfo> getAttributeInfos() {
		return attributes;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object[] resolveSnapshot(Object entityId, SharedSessionContractImplementor session) {
		return new Object[0];
	}

	@Override
	public NaturalIdDataAccess getCacheAccess() {
		return cacheRegionAccess;
	}

	public static class NaturalIdAttributeInfoImpl implements NaturalIdAttributeInfo {
		private final NonIdPersistentAttribute underlyingAttribute;
		private final int stateArrayPosition;

		public NaturalIdAttributeInfoImpl(NonIdPersistentAttribute underlyingAttribute, int stateArrayPosition) {
			this.underlyingAttribute = underlyingAttribute;
			this.stateArrayPosition = stateArrayPosition;
		}

		@Override
		public NonIdPersistentAttribute getUnderlyingAttributeDescriptor() {
			return underlyingAttribute;
		}

		@Override
		public int getStateArrayPosition() {
			return stateArrayPosition;
		}
	}

	@Override
	public NavigableContainer getContainer() {
		return hierarchy.getRootEntityType();
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public void visitNavigable(NavigableVisitationStrategy visitor) {
		attributes.forEach(
				attributeInfo -> attributeInfo.getUnderlyingAttributeDescriptor()
						.visitNavigable( visitor )
		);
	}

	@Override
	public void visitColumns(Consumer<Column> consumer) {
		attributes.forEach(
				naturalIdAttributeInfo -> naturalIdAttributeInfo.getUnderlyingAttributeDescriptor().visitColumns( consumer )
		);
	}

	@Override
	public SqlSelectionGroupNode resolveSqlSelections(
			ColumnReferenceQualifier qualifier,
			SqlSelectionResolutionContext resolutionContext) {
		final List<SqlSelectionGroupNode> selections = new ArrayList<>();
		attributes.forEach(
				naturalIdAttributeInfo -> {
					naturalIdAttributeInfo.getUnderlyingAttributeDescriptor().visitColumns(
							column -> naturalIdAttributeInfo.getUnderlyingAttributeDescriptor().resolveSqlSelections( qualifier, resolutionContext )
					);
				}
		);

		return new AggregateSqlSelectionGroupNode( selections );
	}

	@Override
	public List<ColumnReference> resolveColumnReferences(
			ColumnReferenceQualifier qualifier,
			SqlSelectionResolutionContext resolutionContext) {
		final List<ColumnReference> columnRefs = new ArrayList<>();

		visitColumns(
				column -> columnRefs.add( qualifier.resolveColumnReference( column ) )
		);

		return columnRefs;
	}

	@Override
	public String asLoggableText() {
		return "NaturalId (" + hierarchy.getRootEntityType().getEntityName() + ")";
	}

	@Override
	public JavaTypeDescriptor<J> getJavaTypeDescriptor() {
		return (JavaTypeDescriptor<J>) hierarchy.getRootEntityType().getJavaTypeDescriptor();
	}

	@Override
	public PersistenceType getPersistenceType() {
		return attributes.size() > 1 ? PersistenceType.EMBEDDABLE : PersistenceType.BASIC;
	}

	@Override
	public int getNumberOfJdbcParametersToBind() {
		return numberOfParameterBinds;
	}

	@Override
	public AllowableParameterType resolveTemporalPrecision(
			TemporalType temporalType,
			TypeConfiguration typeConfiguration) {
		if ( attributes.size() == 1 ) {
			final NaturalIdAttributeInfo naturalIdAttributeInfo = attributes.get( 0 );
			if ( naturalIdAttributeInfo.getUnderlyingAttributeDescriptor() instanceof TemporalJavaDescriptor ) {
				final TemporalJavaDescriptor jtd = (TemporalJavaDescriptor) naturalIdAttributeInfo.getUnderlyingAttributeDescriptor().getJavaTypeDescriptor();
				return (AllowableParameterType) jtd.resolveTypeForPrecision( temporalType, typeConfiguration );
			}
		}

		throw new UnsupportedOperationException(  );
	}


	@Override
	public Expression toJdbcParameters() {
		final TableGroup currentTableGroup = walker.getTableGroupStack().getCurrent();

		final List<Expression> expressions = new ArrayList<>();
		visitColumns(
				column -> {
					expressions.add( currentTableGroup.resolveColumnReference( column ) );
				}
		);

		if ( expressions.size() == 1 ) {
			return expressions.get( 0 );
		}
		else {
			return new SqlTuple( expressions );
		}
	}

	@Override
	public Object unresolve(J value, SharedSessionContractImplementor session) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public void dehydrate(
			Object value,
			JdbcValueCollector jdbcValueCollector,
			SharedSessionContractImplementor session) {
		throw new UnsupportedOperationException(  );
	}
}
