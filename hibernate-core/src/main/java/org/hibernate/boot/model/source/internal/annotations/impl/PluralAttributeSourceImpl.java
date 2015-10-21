/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.AssertionFailure;
import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.Caching;
import org.hibernate.boot.model.CustomSql;
import org.hibernate.boot.model.source.internal.annotations.AnnotationAttributeSource;
import org.hibernate.boot.model.source.internal.annotations.AttributeSource;
import org.hibernate.boot.model.source.internal.annotations.CollectionIdSource;
import org.hibernate.boot.model.source.internal.annotations.ColumnSource;
import org.hibernate.boot.model.source.internal.annotations.FilterSource;
import org.hibernate.boot.model.source.internal.annotations.HibernateTypeSource;
import org.hibernate.boot.model.source.internal.annotations.PluralAttributeElementSource;
import org.hibernate.boot.model.source.internal.annotations.PluralAttributeForeignKeySource;
import org.hibernate.boot.model.source.internal.annotations.PluralAttributeSource;
import org.hibernate.boot.model.source.internal.annotations.TableSpecificationSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.CollectionIdInformation;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.Column;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.OverrideAndConverterCollector;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PluralAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.EnumConversionHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.CollectionNature;
import org.hibernate.boot.model.source.spi.FetchCharacteristicsPluralAttribute;
import org.hibernate.boot.model.source.spi.Orderable;
import org.hibernate.boot.model.source.spi.PluralAttributeElementNature;
import org.hibernate.boot.model.source.spi.Sortable;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.CascadeStyles;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;

/**
 * @author Hardy Ferentschik
 */
public class PluralAttributeSourceImpl
		implements AnnotationAttributeSource, PluralAttributeSource, Orderable, Sortable {
	private final PluralAttribute pluralAttribute;

	private final Set<CascadeStyle> unifiedCascadeStyles;

	private final HibernateTypeSource typeSource;
	private final PluralAttributeForeignKeySource foreignKeySource;
	private final FilterSource[] filterSources;

	private final CollectionIdSource collectionIdSource;

	// If it is not the owner side (i.e., mappedBy != null), then the AttributeSource
	// for the owner is required to determine elementSource.
	private PluralAttributeElementSource elementSource;
	private AttributeSource ownerAttributeSource;

	public PluralAttributeSourceImpl(
			PluralAttribute pluralAttribute,
			OverrideAndConverterCollector overrideAndConverterCollector) {
		this.pluralAttribute = pluralAttribute;

		this.unifiedCascadeStyles = determineCascadeStyles( pluralAttribute );

		this.foreignKeySource = new PluralAttributeForeignKeySourceImpl( pluralAttribute );
		this.typeSource = new HibernateTypeSourceImpl( pluralAttribute );


		if ( ! pluralAttribute.isInverse() ) {
			this.ownerAttributeSource = this;
			this.elementSource = determineElementSource( this, this );
		}
		this.filterSources = determineFilterSources( pluralAttribute );

		if ( pluralAttribute.getCollectionIdInformation() == null ) {
			this.collectionIdSource = null;
		}
		else {
			collectionIdSource = new CollectionIdSourceImpl(
					pluralAttribute.getCollectionIdInformation(),
					pluralAttribute.getContext()
			);
		}
	}

	private static Set<CascadeStyle> determineCascadeStyles(PluralAttribute pluralAttribute) {
		final Set<CascadeStyle> cascadeStyles = EnumConversionHelper.cascadeTypeToCascadeStyleSet(
				pluralAttribute.getJpaCascadeTypes(),
				pluralAttribute.getHibernateCascadeTypes(),
				pluralAttribute.getContext()
		);
		if ( pluralAttribute.isOrphanRemoval() ) {
			cascadeStyles.add( CascadeStyles.DELETE_ORPHAN );
		}
		return cascadeStyles;
	}

	private FilterSource[] determineFilterSources(PluralAttribute associationAttribute) {
		final List<FilterSource> result = new ArrayList<FilterSource>();

		final AnnotationInstance filtersAnnotation = associationAttribute.findAnnotation( HibernateDotNames.FILTERS );
		if ( filtersAnnotation != null ) {
			AnnotationInstance[] annotationInstances = filtersAnnotation.value().asNestedArray();
			for ( AnnotationInstance filterAnnotation : annotationInstances ) {
				result.add( new FilterSourceImpl( filterAnnotation ) );
			}

		}

		final AnnotationInstance filterAnnotation = associationAttribute.findAnnotation( HibernateDotNames.FILTER );
		if ( filterAnnotation != null ) {
			result.add( new FilterSourceImpl( filterAnnotation ) );
		}

		if ( result.isEmpty() ) {
			return null;
		}
		else {
			return result.toArray( new FilterSource[result.size()] );
		}
	}

	@Override
	public AttributePath getAttributePath() {
		return pluralAttribute.getPath();
	}

	@Override
	public AttributeRole getAttributeRole() {
		return pluralAttribute.getRole();
	}

	@Override
	public CollectionIdSource getCollectionIdSource() {
		return collectionIdSource;
	}

	@Override
	public PersistentAttribute getAnnotatedAttribute() {
		return getPluralAttribute();
	}

	public PluralAttribute getPluralAttribute() {
		return pluralAttribute;
	}

	@Override
	public String[] getSynchronizedTableNames() {
		return new String[0];
	}

	@Override
	public Set<CascadeStyle> getCascadeStyles() {
		return unifiedCascadeStyles;
	}

	@Override
	public CollectionNature getNature() {
		return pluralAttribute.getCollectionNature();
	}

	@Override
	public PluralAttributeElementSource getElementSource() {
		return elementSource;
	}

	@Override
	public FilterSource[] getFilterSources() {
		return filterSources;
	}

	@Override
	public boolean usesJoinTable() {
		if ( pluralAttribute.isInverse() ) {
			throw new IllegalStateException( "Cannot determine if a join table is used because plural attribute is not the owner." );
		}
		// By default, a unidirectional one-to-many (i.e., with mappedBy == null) uses a join table,
		// unless it has join columns defined.
		return pluralAttribute.getJoinTableAnnotation() != null ||
				( pluralAttribute.getJoinTableAnnotation() == null &&
						pluralAttribute.getJoinColumnValues().size() == 0 );
	}

//	@Override
//	public JavaTypeDescriptor getElementTypeDescriptor() {
//		// needed for arrays
//		if ( ArrayDescriptor.class.isInstance( pluralAttribute.getBackingMember().getType().getErasedType() ) ) {
//			final ArrayDescriptor arrayDescriptor = (ArrayDescriptor) pluralAttribute.getBackingMember().getType().getErasedType();
//			return arrayDescriptor.getComponentType();
//		}
//		else {
//			return null;
//		}
//	}

	private static PluralAttributeElementSource determineElementSource(
			AttributeSource ownerAttributeSource,
			PluralAttributeSourceImpl pluralAttributeSource) {
		if ( ownerAttributeSource == null ) {
			throw new AssertionFailure( "ownerAssociationSource must be non-null." );
		}

		final PluralAttribute associationAttribute = pluralAttributeSource.pluralAssociationAttribute();
		switch ( pluralAttributeSource.pluralAssociationAttribute().getElementDetails().getElementNature() ) {
			case BASIC: {
				return new PluralAttributeElementSourceBasicImpl( pluralAttributeSource );
			}
			case AGGREGATE: {
				return new PluralAttributeElementSourceEmbeddedImpl( pluralAttributeSource );
			}
			case MANY_TO_MANY: {
				throw new NotYetImplementedException( "not yet implemented" );
//				if ( ! associationAttribute.isInverse() ) {
//					return new PluralAttributeElementSourceAssociationManyToManyImpl( pluralAttributeSource );
//				}
//				else {
//					return new MappedByPluralAttributeElementSourceAssociationManyToManyImpl( pluralAttributeSource );
//				}
			}
			case ONE_TO_MANY: {
				throw new NotYetImplementedException( "not yet implemented" );
//				final boolean usesJoinTable = ownerAttributeSource.isSingular()
//						? ( (ToOneAttributeSource) ownerAttributeSource ).getContainingTableName() != null
//						: ( (PluralAttributeSource) ownerAttributeSource ).usesJoinTable();
//
//				if ( usesJoinTable ) {
//					if ( ! associationAttribute.isInverse() ) {
//						return new PluralAttributeElementSourceAssociationManyToManyImpl( pluralAttributeSource );
//					}
//					else {
//						return new MappedByPluralAttributeElementSourceAssociationManyToManyImpl( pluralAttributeSource );
//					}
//				}
//				else {
//					if ( ! associationAttribute.isInverse() ) {
//						return new PluralAttributeElementSourceAssociationOneToManyImpl( pluralAttributeSource );
//					}
//					else {
//						return new MappedByPluralAttributeElementSourceAssociationOneToManyImpl( pluralAttributeSource );
//					}
//				}
			}
			case MANY_TO_ANY: {
				return new PluralAttributeElementSourceAssociationManyToAnyImpl( pluralAttributeSource );
			}
			default: {
				throw new AssertionError(
						"Unexpected plural attribute element nature : " +
								pluralAttributeSource.pluralAssociationAttribute().getElementDetails().getElementNature()
				);
			}
		}
	}

	@Override
	public PluralAttributeForeignKeySource getForeignKeySource() {
		return foreignKeySource;
	}

	@Override
	public TableSpecificationSource getCollectionTableSpecificationSource() {
		// todo - see org.hibernate.metamodel.internal.Binder#bindOneToManyCollectionKey
		// todo - needs to cater for @CollectionTable and @JoinTable
		if ( pluralAttribute.isInverse() ) {
			throw new IllegalStateException( "Cannot get collection table because this association is not the owner." );
		}
		final AnnotationInstance joinTableAnnotation = pluralAttribute.getJoinTableAnnotation();
		if ( joinTableAnnotation == null ) {
			return null;
		}

		return TableSourceImpl.build( joinTableAnnotation, pluralAttribute.getContext() );
	}

	@Override
	public String getCollectionTableComment() {
		return null;
	}

	@Override
	public String getCollectionTableCheck() {
		return pluralAttribute.getCheckCondition();
	}

	@Override
	public Caching getCaching() {
		return pluralAttribute.getCaching();
	}

	@Override
	public String getCustomPersisterClassName() {
		return pluralAttribute.getCustomPersister();
	}

	@Override
	public String getWhere() {
		return pluralAttribute.getWhereClause();
	}

	@Override
	public String getMappedBy() {
		return pluralAttribute.getMappedByAttributeName();
	}

	@Override
	public boolean isInverse() {
		return pluralAttribute.isInverse();
	}

	@Override
	public String getCustomLoaderName() {
		return pluralAttribute.getCustomLoaderName();
	}

	@Override
	public CustomSql getCustomSqlInsert() {
		return pluralAttribute.getCustomInsert();
	}

	@Override
	public CustomSql getCustomSqlUpdate() {
		return pluralAttribute.getCustomUpdate();
	}

	@Override
	public CustomSql getCustomSqlDelete() {
		return pluralAttribute.getCustomDelete();
	}

	@Override
	public CustomSql getCustomSqlDeleteAll() {
		return pluralAttribute.getCustomDeleteAll();
	}

	@Override
	public String getName() {
		return pluralAttribute.getName();
	}

	@Override
	public boolean isSingular() {
		return false;
	}

	@Override
	public HibernateTypeSource getTypeInformation() {
		return typeSource;
	}

	@Override
	public String getPropertyAccessorName() {
		if ( StringHelper.isNotEmpty( pluralAttribute.getAccessorStrategy() ) ) {
			return pluralAttribute.getAccessorStrategy();
		}
		else if ( pluralAttribute.getAccessType() != null ) {
			return pluralAttribute.getAccessType().name().toLowerCase();
		}
		else {
			return null;
		}
	}

	@Override
	public boolean isIncludedInOptimisticLocking() {
		return pluralAttribute.isIncludeInOptimisticLocking();
	}

	@Override
	public String getOrder() {
		return elementSource.getNature() == PluralAttributeElementNature.MANY_TO_MANY ?
				null :
				pluralAttribute.getOrderBy();
	}

	@Override
	public boolean isMutable() {
		return pluralAttribute.isMutable();
	}

	@Override
	public boolean isOrdered() {
		return StringHelper.isNotEmpty( getOrder() );
	}

	@Override
	public String getComparatorName() {
		return pluralAttribute.getComparatorName();
	}

	@Override
	public boolean isSorted() {
		return pluralAttribute.isSorted();
	}

	@Override
	public FetchCharacteristicsPluralAttribute getFetchCharacteristics() {
		return pluralAttribute.getFetchCharacteristics();
	}

//	@Override
//	public PluralAttributeElementSource resolvePluralAttributeElementSource(AttributeSourceResolutionContext context) {
//		if ( elementSource == null ) {
//			buildElementSource( context );
//
//		}
//		return elementSource;
//	}
//
//	private void buildElementSource(AttributeSourceResolutionContext context) {
//		// elementSource has not been initialized, so we need to resolve it using the
//		// association owner.
//
//		if ( StringHelper.isEmpty( pluralAttribute.getMappedByAttributeName() )) {
//			// The attribute is inverse, but no mappedBy given (ex: HBM XML transformation uses the temporary
//			// @Inverse annotation since it cannot reliably find the owning attribute on its own).  Attempt to resolve
//			// using the join columns.
//			// TODO: Move elsewhere?
//			final JavaTypeDescriptor elementType = pluralAttribute.getElementDetails().getJavaType();
//			final List<String> joinColumnNames = new ArrayList<String>();
//			for (Column joinColumn : pluralAttribute.getJoinColumnValues()) {
//				joinColumnNames.add( joinColumn.getName() );
//			}
//			pluralAttribute.setMappedByAttributeName( context.resolveAttributeName(
//					elementType.getName().toString(), joinColumnNames ) );
//		}
//
//		// Get the owner attribute source that maps the opposite side of the association.
//		ownerAttributeSource = context.resolveAttributeSource(
//				pluralAttribute.getElementDetails().getJavaType().getName().toString(),
//				pluralAttribute.getMappedByAttributeName()
//		);
//
//		// Initialize resolved entitySource.
//		elementSource = determineElementSource( ownerAttributeSource, this );
//		if ( !MappedByAssociationSource.class.isInstance( elementSource ) ) {
//			throw new AssertionFailure( "expected a mappedBy association." );
//		}
//		final AssociationSource ownerAssociationSource;
//		if ( ownerAttributeSource.isSingular() ) {
//			ownerAssociationSource = (ToOneAttributeSource) ownerAttributeSource;
//		}
//		else {
//			final PluralAttributeSource pluralAttributeSource = (PluralAttributeSource) ownerAttributeSource;
//			if ( !AssociationSource.class.isInstance( pluralAttributeSource.getElementSource() ) ) {
//				throw new AssertionFailure( "Owner is not an association." );
//			}
//			ownerAssociationSource = (AssociationSource) pluralAttributeSource.getElementSource();
//		}
//		ownerAssociationSource.addMappedByAssociationSource( (MappedByAssociationSource) elementSource );
//	}

	protected PluralAttribute pluralAssociationAttribute() {
		return pluralAttribute;
	}


	private static class CollectionIdSourceImpl implements CollectionIdSource {
		private final ColumnSourceImpl columnSource;
		private final HibernateTypeSource typeSource;
		private final String generatorName;

		public CollectionIdSourceImpl(CollectionIdInformation collectionIdInformation, EntityBindingContext context) {
			this.columnSource = interpretColumns( collectionIdInformation.getColumns(), context );
			this.typeSource = collectionIdInformation.getExplicitTypeSource();
			this.generatorName = collectionIdInformation.getGeneratorDefinition().getName();
		}

		private static ColumnSourceImpl interpretColumns(List<Column> columns, EntityBindingContext context) {
			if ( columns == null || columns.isEmpty() ) {
				return null;
			}

			if ( columns.size() > 1 ) {
				throw context.makeMappingException( "Expecting just one column for collection id" );
			}

			return new ColumnSourceImpl( columns.get( 0 ) );
		}

		@Override
		public ColumnSource getColumnSource() {
			return columnSource;
		}

		@Override
		public HibernateTypeSource getTypeInformation() {
			return typeSource;
		}

		@Override
		public String getGeneratorName() {
			return generatorName;
		}
	}
}


