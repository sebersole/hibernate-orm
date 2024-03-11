/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.internal;

import java.util.List;

import org.hibernate.AnnotationException;
import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.boot.spi.PropertyData;
import org.hibernate.models.spi.AnnotationUsage;
import org.hibernate.models.spi.MemberDetails;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import static org.hibernate.boot.model.internal.AnnotatedColumn.buildColumnFromAnnotation;
import static org.hibernate.boot.model.internal.AnnotatedColumn.buildColumnFromNoAnnotation;
import static org.hibernate.boot.model.internal.AnnotatedColumn.buildColumnsFromAnnotations;
import static org.hibernate.boot.model.internal.AnnotatedColumn.buildFormulaFromAnnotation;
import static org.hibernate.boot.model.internal.BinderHelper.getOverridableAnnotation;
import static org.hibernate.boot.model.internal.BinderHelper.getPath;
import static org.hibernate.boot.model.internal.BinderHelper.getPropertyOverriddenByMapperOrMapsId;
import static org.hibernate.internal.util.StringHelper.nullIfEmpty;

/**
 * Do the initial discovery of columns metadata and apply defaults.
 * Also hosts some convenient methods related to column processing
 *
 * @author Emmanuel Bernard
 * @author Brett Meyer
 */
class ColumnsBuilder {

	private final PropertyHolder propertyHolder;
	private final Nullability nullability;
	private final MemberDetails property;
	private final PropertyData inferredData;
	private final EntityBinder entityBinder;
	private final MetadataBuildingContext buildingContext;
	private AnnotatedColumns columns;
	private AnnotatedJoinColumns joinColumns;

	public ColumnsBuilder(
			PropertyHolder propertyHolder,
			Nullability nullability,
			MemberDetails property,
			PropertyData inferredData,
			EntityBinder entityBinder,
			MetadataBuildingContext buildingContext) {
		this.propertyHolder = propertyHolder;
		this.nullability = nullability;
		this.property = property;
		this.inferredData = inferredData;
		this.entityBinder = entityBinder;
		this.buildingContext = buildingContext;
	}

	public AnnotatedColumns getColumns() {
		return columns;
	}

	public AnnotatedJoinColumns getJoinColumns() {
		return joinColumns;
	}

	public ColumnsBuilder extractMetadata() {
		columns = null;
		joinColumns = buildExplicitJoinColumns( property, inferredData );

		final AnnotationUsage<Column> columnAnn = property.getAnnotationUsage( Column.class );
		final AnnotationUsage<Formula> formulaAnn = property.getAnnotationUsage( Formula.class );
		final AnnotationUsage<Columns> columnsAnn = property.getAnnotationUsage( Columns.class );
		if ( columnAnn != null ) {
			columns = buildColumnFromAnnotation(
					columnAnn,
					nullability,
					propertyHolder,
					inferredData,
					entityBinder.getSecondaryTables(),
					buildingContext
			);
		}
		else if ( formulaAnn != null ) {
			columns = buildFormulaFromAnnotation(
					getOverridableAnnotation( property, Formula.class, buildingContext ),
					nullability,
					propertyHolder,
					inferredData,
					entityBinder.getSecondaryTables(),
					buildingContext
			);
		}
		else if ( columnsAnn != null ) {
			columns = buildColumnsFromAnnotations(
					columnsAnn.getList( "columns" ),
					nullability,
					propertyHolder,
					inferredData,
					entityBinder.getSecondaryTables(),
					buildingContext
			);
		}

		//set default values if needed
		if ( joinColumns == null
				&& ( property.hasAnnotationUsage( ManyToOne.class )
						|| property.hasAnnotationUsage( OneToOne.class ) ) ) {
			joinColumns = buildDefaultJoinColumnsForToOne( property, inferredData );
		}
		else if ( joinColumns == null
				&& ( property.hasAnnotationUsage( OneToMany.class )
						|| property.hasAnnotationUsage( ElementCollection.class ) ) ) {
			AnnotationUsage<OneToMany> oneToMany = property.getAnnotationUsage( OneToMany.class );
			joinColumns = AnnotatedJoinColumns.buildJoinColumns(
					null,
//					comment,
					oneToMany == null ? null : nullIfEmpty( oneToMany.getString( "mappedBy" ) ),
					entityBinder.getSecondaryTables(),
					propertyHolder,
					inferredData,
					buildingContext
			);
		}
		else if ( joinColumns == null
				&& property.hasAnnotationUsage( org.hibernate.annotations.Any.class ) ) {
			throw new AnnotationException( "Property '" + getPath( propertyHolder, inferredData )
					+ "' is annotated '@Any' and must declare at least one '@JoinColumn'" );
		}
		if ( columns == null && !property.hasAnnotationUsage( ManyToMany.class ) ) {
			//useful for collection of embedded elements
			columns = buildColumnFromNoAnnotation(
//					comment,
					nullability,
					propertyHolder,
					inferredData,
					entityBinder.getSecondaryTables(),
					buildingContext
			);
		}

		if ( nullability == Nullability.FORCED_NOT_NULL ) {
			//force columns to not null
			for ( AnnotatedColumn column : columns.getColumns() ) {
				column.forceNotNull();
			}
		}
		return this;
	}

	private AnnotatedJoinColumns buildDefaultJoinColumnsForToOne(
			MemberDetails property,
			PropertyData inferredData) {
		final AnnotationUsage<JoinTable> joinTableAnn = propertyHolder.getJoinTable( property );
//		final Comment comment = property.getAnnotation(Comment.class);
		if ( joinTableAnn != null ) {
			return AnnotatedJoinColumns.buildJoinColumns(
					joinTableAnn.getList( "inverseJoinColumns" ),
//					comment,
					null,
					entityBinder.getSecondaryTables(),
					propertyHolder,
					inferredData,
					buildingContext
			);
		}
		else {
			final AnnotationUsage<OneToOne> oneToOneAnn = property.getAnnotationUsage( OneToOne.class );
			return AnnotatedJoinColumns.buildJoinColumns(
					null,
//					comment,
					oneToOneAnn == null ? null : nullIfEmpty( oneToOneAnn.getString( "mappedBy" ) ),
					entityBinder.getSecondaryTables(),
					propertyHolder,
					inferredData,
					buildingContext
			);
		}
	}

	private AnnotatedJoinColumns buildExplicitJoinColumns(MemberDetails property, PropertyData inferredData) {
		// process @JoinColumns before @Columns to handle collection of entities properly
		final List<AnnotationUsage<JoinColumn>> joinColumnAnnotations = getJoinColumnAnnotations( property, inferredData );
		if ( joinColumnAnnotations != null ) {
			return AnnotatedJoinColumns.buildJoinColumns(
					joinColumnAnnotations,
					null,
					entityBinder.getSecondaryTables(),
					propertyHolder,
					inferredData,
					buildingContext
			);
		}

		final List<AnnotationUsage<JoinColumnOrFormula>> joinColumnOrFormulaAnnotations = joinColumnOrFormulaAnnotations( property, inferredData );
		if ( joinColumnOrFormulaAnnotations != null ) {
			return AnnotatedJoinColumns.buildJoinColumnsOrFormulas(
					joinColumnOrFormulaAnnotations,
					null,
					entityBinder.getSecondaryTables(),
					propertyHolder,
					inferredData,
					buildingContext
			);
		}

		if ( property.hasAnnotationUsage( JoinFormula.class) ) {
			final AnnotationUsage<JoinFormula> joinFormula = getOverridableAnnotation( property, JoinFormula.class, buildingContext );
			return AnnotatedJoinColumns.buildJoinColumnsWithFormula(
					joinFormula,
					entityBinder.getSecondaryTables(),
					propertyHolder,
					inferredData,
					buildingContext
			);
		}

		return null;
	}

	private List<AnnotationUsage<JoinColumnOrFormula>> joinColumnOrFormulaAnnotations(MemberDetails property, PropertyData inferredData) {
		if ( property.hasAnnotationUsage( JoinColumnOrFormula.class ) ) {
			return List.of( property.getAnnotationUsage( JoinColumnOrFormula.class ) );
		}

		if ( property.hasAnnotationUsage( JoinColumnsOrFormulas.class ) ) {
			final AnnotationUsage<JoinColumnsOrFormulas> joinColumnsOrFormulas = property.getAnnotationUsage( JoinColumnsOrFormulas.class );
			final List<AnnotationUsage<JoinColumnOrFormula>> joinColumnOrFormulaList = joinColumnsOrFormulas.getList( "value" );
			if ( joinColumnOrFormulaList.isEmpty() ) {
				throw new AnnotationException( "Property '" + getPath( propertyHolder, inferredData)
						+ "' has an empty '@JoinColumnsOrFormulas' annotation" );
			}
			return joinColumnOrFormulaList;
		}

		return null;
	}

	private List<AnnotationUsage<JoinColumn>> getJoinColumnAnnotations(MemberDetails property, PropertyData inferredData) {
		if ( property.hasAnnotationUsage( JoinColumn.class ) ) {
			return List.of( property.getAnnotationUsage( JoinColumn.class ) );
		}

		if ( property.hasAnnotationUsage( JoinColumns.class ) ) {
			final AnnotationUsage<JoinColumns> joinColumns = property.getAnnotationUsage( JoinColumns.class );
			final List<AnnotationUsage<JoinColumn>> joinColumnsList = joinColumns.getList( "value" );
			if ( joinColumnsList.isEmpty() ) {
				throw new AnnotationException( "Property '" + getPath( propertyHolder, inferredData)
						+ "' has an empty '@JoinColumns' annotation" );
			}
			return joinColumnsList;
		}

		return null;
	}

	/**
	 * Useful to override a column either by {@code @MapsId} or by {@code @IdClass}
	 */
	AnnotatedColumns overrideColumnFromMapperOrMapsIdProperty(boolean isId) {
		final PropertyData override =
				getPropertyOverriddenByMapperOrMapsId( isId, propertyHolder, property.resolveAttributeName(), buildingContext );
		if ( override != null ) {
			final AnnotatedJoinColumns joinColumns = buildExplicitJoinColumns( override.getAttributeMember(), override );
			return joinColumns == null
					? buildDefaultJoinColumnsForToOne( override.getAttributeMember(), override )
					: joinColumns;
		}
		else {
			return columns;
		}
	}

}
