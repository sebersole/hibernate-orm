/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.domain.internal;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import javax.persistence.TemporalType;

import org.hibernate.cache.spi.access.NaturalIdDataAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.spi.AllowableParameterType;
import org.hibernate.metamodel.model.domain.spi.EntityHierarchy;
import org.hibernate.metamodel.model.domain.spi.NaturalIdDescriptor;
import org.hibernate.metamodel.model.domain.spi.NavigableContainer;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.domain.spi.NonIdPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.ast.tree.spi.expression.ColumnReference;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.internal.AggregateSqlSelectionGroupNode;
import org.hibernate.sql.results.spi.SqlSelectionGroupNode;
import org.hibernate.sql.results.spi.SqlAstCreationContext;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.spi.TemporalJavaDescriptor;
import org.hibernate.type.descriptor.spi.ValueBinder;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class NaturalIdDescriptorImpl<J> implements NaturalIdDescriptor<J>, AllowableParameterType<J> {
	private final EntityHierarchy hierarchy;
	private final NaturalIdDataAccess cacheRegionAccess;
	private final NavigableRole navigableRole;

	private List<NaturalIdAttributeInfo> attributes;

	private ValueBinder valueBinder;

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
			this.numberOfParameterBinds += ( (AllowableParameterType) attributes.get( i ) ).getNumberOfJdbcParametersNeeded();
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
	public SqlSelectionGroupNode resolveSqlSelections(
			ColumnReferenceQualifier qualifier,
			SqlAstCreationContext creationContext) {
		final List<SqlSelectionGroupNode> selections = new ArrayList<>();
		attributes.forEach( attributeInfo -> selections.add( attributeInfo.getUnderlyingAttributeDescriptor().resolveSqlSelections( qualifier, creationContext ) ) );
		return new AggregateSqlSelectionGroupNode( selections );
	}

	@Override
	public List<ColumnReference> resolveColumnReferences(
			ColumnReferenceQualifier qualifier,
			SqlAstCreationContext creationContext) {
		final List<ColumnReference> columnRefs = new ArrayList<>();
		attributes.forEach( attributeInfo -> columnRefs.addAll( attributeInfo.getUnderlyingAttributeDescriptor().resolveColumnReferences( qualifier, creationContext ) ) );
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
	public int getNumberOfJdbcParametersNeeded() {
		return numberOfParameterBinds;
	}

	@Override
	public ValueBinder getValueBinder(Predicate<StateArrayContributor> inclusionChecker, TypeConfiguration typeConfiguration) {
		if ( valueBinder == null ) {
			if ( attributes.size() == 1 ) {
				valueBinder = ( (AllowableParameterType) attributes.get( 0 ).getUnderlyingAttributeDescriptor() )
						.getValueBinder( inclusionChecker, typeConfiguration );
			}
			else {
				valueBinder = new ValueBinder() {
					@Override
					public int getNumberOfJdbcParametersNeeded() {
						return numberOfParameterBinds;
					}

					@Override
					public void bind(
							PreparedStatement st,
							int position,
							Object value,
							ExecutionContext executionContext) throws SQLException {
						int segmentStart = position;

						for ( NaturalIdAttributeInfo attributeInfo : attributes ) {
							final AllowableParameterType attributeDescriptor = (AllowableParameterType) attributeInfo
									.getUnderlyingAttributeDescriptor();
							attributeDescriptor.getValueBinder( inclusionChecker, typeConfiguration )
									.bind( st, segmentStart, value, executionContext );
							segmentStart += attributeDescriptor.getNumberOfJdbcParametersNeeded();
						}
					}

					@Override
					public void bind(
							PreparedStatement st,
							String name,
							Object value,
							ExecutionContext executionContext) throws SQLException {
						for ( NaturalIdAttributeInfo attributeInfo : attributes ) {
							final AllowableParameterType attributeDescriptor = (AllowableParameterType) attributeInfo
									.getUnderlyingAttributeDescriptor();
							attributeDescriptor.getValueBinder( inclusionChecker, typeConfiguration )
									.bind( st, name, value, executionContext );
						}
					}
				};
			}
		}

		return valueBinder;
	}

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

		throw new UnsupportedOperationException( "Composite natural-id cannot be treated as a temporal value" );
	}
}
