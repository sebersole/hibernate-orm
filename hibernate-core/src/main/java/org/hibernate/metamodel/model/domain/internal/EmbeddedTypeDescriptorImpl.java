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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.TemporalType;

import org.hibernate.HibernateException;
import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.boot.model.domain.spi.EmbeddedValueMappingImplementor;
import org.hibernate.boot.model.domain.spi.ManagedTypeMappingImplementor;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.model.creation.spi.RuntimeModelCreationContext;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.metamodel.model.domain.spi.AbstractManagedType;
import org.hibernate.metamodel.model.domain.spi.EmbeddedContainer;
import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.metamodel.model.domain.spi.InheritanceCapable;
import org.hibernate.metamodel.model.domain.spi.Instantiator;
import org.hibernate.metamodel.model.domain.spi.ManagedTypeRepresentationStrategy;
import org.hibernate.metamodel.model.domain.spi.NavigableVisitationStrategy;
import org.hibernate.metamodel.model.domain.spi.NonIdPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.SingularPersistentAttribute;
import org.hibernate.metamodel.model.domain.spi.StateArrayContributor;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.procedure.ParameterMisuseException;
import org.hibernate.query.sqm.AllowableParameterType;
import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.sql.ast.produce.spi.ColumnReferenceQualifier;
import org.hibernate.sql.exec.spi.ExecutionContext;
import org.hibernate.sql.results.internal.CompositeSqlSelectionGroupImpl;
import org.hibernate.sql.results.spi.CompositeSqlSelectionGroup;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.SqlSelection;
import org.hibernate.sql.results.spi.SqlSelectionResolutionContext;
import org.hibernate.type.descriptor.java.internal.EmbeddableJavaDescriptorImpl;
import org.hibernate.type.descriptor.java.spi.EmbeddableJavaDescriptor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptorRegistry;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public class EmbeddedTypeDescriptorImpl<J>
		extends AbstractManagedType<J>
		implements EmbeddedTypeDescriptor<J> {
	private final EmbeddedContainer container;
	private final NavigableRole navigableRole;

	private final SingularPersistentAttribute.Disposition compositeDisposition;

	private ManagedTypeRepresentationStrategy representationStrategy;
	private Instantiator<J> instantiator;


	@SuppressWarnings("unchecked")
	public EmbeddedTypeDescriptorImpl(
			EmbeddedValueMappingImplementor embeddedMapping,
			EmbeddedContainer container,
			EmbeddedTypeDescriptor superTypeDescriptor,
			String localName,
			SingularPersistentAttribute.Disposition compositeDisposition,
			RuntimeModelCreationContext creationContext) {
		super(
				embeddedMapping,
				superTypeDescriptor,
				resolveJtd( creationContext, embeddedMapping ),
				creationContext
		);

		// todo (6.0) : support for specific MutalibilityPlan and Comparator

		this.container = container;
		this.compositeDisposition = compositeDisposition;
		this.navigableRole = container.getNavigableRole().append( localName );
	}

	@SuppressWarnings("unchecked")
	private static <T> EmbeddableJavaDescriptor<T> resolveJtd(RuntimeModelCreationContext creationContext, EmbeddedValueMappingImplementor embeddedMapping) {
		final JavaTypeDescriptorRegistry jtdr = creationContext.getTypeConfiguration().getJavaTypeDescriptorRegistry();

		EmbeddableJavaDescriptor<T> jtd = (EmbeddableJavaDescriptor<T>) jtdr.getDescriptor( embeddedMapping.getName() );
		if ( jtd == null ) {
			final Class<T> javaType;
			if ( StringHelper.isEmpty( embeddedMapping.getEmbeddableClassName() ) ) {
				javaType = null;
			}
			else {
				javaType = creationContext.getSessionFactory()
						.getServiceRegistry()
						.getService( ClassLoaderService.class )
						.classForName( embeddedMapping.getEmbeddableClassName() );
			}

			jtd = new EmbeddableJavaDescriptorImpl(
					embeddedMapping.getName(),
					javaType,
					null
			);
			jtdr.addDescriptor( jtd );
		}

		return jtd;
	}

	private boolean fullyInitialized;

	@Override
	public void finishInitialization(
			ManagedTypeMappingImplementor bootDescriptor,
			RuntimeModelCreationContext creationContext) {
		if ( this.fullyInitialized ) {
			return;
		}

		super.finishInitialization( bootDescriptor, creationContext );

		this.representationStrategy = creationContext.getMetadata().getMetadataBuildingOptions()
				.getManagedTypeRepresentationResolver()
				.resolveStrategy( bootDescriptor, this, creationContext);
		this.instantiator = representationStrategy.resolveInstantiator( bootDescriptor, this, creationContext.getSessionFactory().getSessionFactoryOptions().getBytecodeProvider() );

		this.fullyInitialized = true;
	}

	@Override
	public EmbeddedContainer<?> getContainer() {
		return container;
	}

	@Override
	public EmbeddedTypeDescriptor<J> getEmbeddedDescriptor() {
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EmbeddableJavaDescriptor<J> getJavaTypeDescriptor() {
		return (EmbeddableJavaDescriptor<J>) super.getJavaTypeDescriptor();
	}

	@Override
	public J instantiate(SharedSessionContractImplementor session) {
		return instantiator.instantiate( session );
	}

	@Override
	public NavigableRole getNavigableRole() {
		return navigableRole;
	}

	@Override
	public void visitNavigable(NavigableVisitationStrategy visitor) {
		throw new UnsupportedOperationException(  );
	}

	@Override
	public CompositeSqlSelectionGroup resolveSqlSelections(
			ColumnReferenceQualifier qualifier,
			SqlSelectionResolutionContext resolutionContext) {
		return CompositeSqlSelectionGroupImpl.buildSqlSelectionGroup( this, qualifier, resolutionContext );
	}

	private List<Column> collectedColumns;

	@Override
	public List<Column> collectColumns() {
		if ( collectedColumns == null ) {
			collectedColumns = new ArrayList<>();
			visitAttributes(
					persistentAttribute -> {
						collectedColumns.addAll( persistentAttribute.getColumns() );
					}
			);
		}

		return collectedColumns;
	}

	@Override
	public int getNumberOfJdbcParametersToBind() {
		return collectColumns().size();
	}

	private final JdbcValueBinder binder = new JdbcValueBinder() {
		@Override
		public void bind(
				PreparedStatement st,
				Object value,
				int index,
				ExecutionContext executionContext) throws SQLException {
			final Object[] values = extractValues( value );

			int position = index;
			for ( StateArrayContributor contributor : getStateArrayContributors() ) {
				final Object subValue = values[ contributor.getStateArrayPosition() ];

				contributor.getValueBinder().bind(
						st,
						values[ position ],
						position,
						executionContext
				);
				position += contributor.getColumns().size();
			}

		}

		private Object[] extractValues(Object value) {
			assert value instanceof Object[];


			final Object[] values = (Object[]) value;
			assert values.length == getStateArrayContributors().size();

			return values;
		}

		@Override
		public void bind(
				CallableStatement st, Object value, String name, ExecutionContext executionContext) throws SQLException {

		}
	};

	@Override
	public JdbcValueBinder getValueBinder() {
		return binder;
	}

	@Override
	public JdbcValueExtractor getValueExtractor() {
		return new JdbcValueExtractor() {
			@Override
			public Object extract(ResultSet rs, SqlSelection selectionMemento, JdbcValuesSourceProcessingState processingState) throws SQLException {
				return null;
			}

			@Override
			public Object extract(CallableStatement statement, SqlSelection index, JdbcValuesSourceProcessingState processingState) throws SQLException {
				return null;
			}

			@Override
			public Object extract(
					CallableStatement statement, String name, JdbcValuesSourceProcessingState processingState) throws SQLException {
				return null;
			}
		};
	}

	@Override
	public List<InheritanceCapable<? extends J>> getSubclassTypes() {
		return Collections.emptyList();
	}





	@Override
	public void setPropertyValue(Object object, int i, Object value) {
		getPersistentAttributes().get( i ).getPropertyAccess().getSetter().set( object, value, getTypeConfiguration().getSessionFactory() );
	}

	@Override
	public Object getPropertyValue(Object object, int i) throws HibernateException {
		return getPersistentAttributes().get( i ).getPropertyAccess().getGetter().get( object );
	}

	@Override
	public Object getPropertyValue(Object object, String propertyName) {
		final NonIdPersistentAttribute<? super J, ?> attribute = findPersistentAttribute( propertyName );
		if ( attribute == null ) {
			throw new HibernateException( "No persistent attribute named [" + propertyName + "] on embeddable [" + getRoleName() + ']' );
		}

		return attribute.getPropertyAccess().getGetter().get( object );
	}

	@Override
	public boolean[] getPropertyNullability() {
		throw new NotYetImplementedFor6Exception(  );
	}

	@Override
	public CascadeStyle getCascadeStyle(int i) {
		throw new NotYetImplementedFor6Exception( );
	}

	@Override
	public AllowableParameterType resolveTemporalPrecision(TemporalType temporalType, TypeConfiguration typeConfiguration) {
		throw new ParameterMisuseException( "Cannot apply temporal precision to embeddable value" );
	}
}
