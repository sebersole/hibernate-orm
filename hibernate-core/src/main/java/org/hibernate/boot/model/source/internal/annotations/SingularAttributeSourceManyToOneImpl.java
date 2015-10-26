/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AssociationOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.Column;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.OverrideAndConverterCollector;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.SingularAssociationAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.ForeignKeyContributingSource;
import org.hibernate.boot.model.source.spi.RelationalValueSource;
import org.hibernate.boot.model.source.spi.SingularAttributeNature;
import org.hibernate.boot.model.source.spi.SingularAttributeSourceManyToOne;
import org.hibernate.mapping.Selectable;
import org.hibernate.mapping.Table;

/**
 * @author Hardy Ferentschik
 * @author Gail Badner
 * @author Steve Ebersole
 */
public class SingularAttributeSourceManyToOneImpl
		extends AbstractSingularAttributeSourceToOneImpl
		implements SingularAttributeSourceManyToOne {

	private final List<RelationalValueSource> relationalValueSources;
	private final ForeignKeyInformation foreignKeyInformation;

	private final EntityBindingContext bindingContext;

	public SingularAttributeSourceManyToOneImpl(
			SingularAssociationAttribute associationAttribute,
			OverrideAndConverterCollector overrideAndConverterCollector) {
		super( associationAttribute );
		if ( associationAttribute.getMappedByAttributeName() != null ) {
			throw new IllegalArgumentException( "associationAttribute.getMappedByAttributeName() must be null" );
		}

		this.bindingContext = associationAttribute.getContext();

		final AssociationOverride override = overrideAndConverterCollector.locateAssociationOverride(
				associationAttribute.getPath()
		);

		// Need to initialize relationalValueSources before determining logicalJoinTableName.
		this.relationalValueSources = resolveRelationalValueSources( associationAttribute, override );

		this.foreignKeyInformation = ForeignKeyInformation.from(
				AnnotationBindingHelper.findFirstNonNull(
						associationAttribute.findAnnotation( JpaDotNames.JOIN_COLUMN ),
						associationAttribute.findAnnotation( JpaDotNames.JOIN_TABLE ),
						associationAttribute.findAnnotation( JpaDotNames.COLLECTION_TABLE )
				),
				associationAttribute.getContext()
		);
	}

	@Override
	public SingularAttributeNature getSingularAttributeNature() {
		return SingularAttributeNature.MANY_TO_ONE;
	}

	@Override
	public List<RelationalValueSource> getRelationalValueSources() {
		return relationalValueSources;
	}

	private List<RelationalValueSource> resolveRelationalValueSources(
			SingularAssociationAttribute attribute,
			AssociationOverride override) {
		// todo : utilize the override
		final List<Column> joinColumns;
		if ( attribute.getJoinTableAnnotation() == null ) {
			joinColumns = attribute.getJoinColumnValues();
		}
		else {
			joinColumns = attribute.getInverseJoinColumnValues();
		}

		if ( joinColumns.isEmpty() ) {
			return Collections.emptyList();
		}

		final List<RelationalValueSource> valueSources = new ArrayList<RelationalValueSource>( joinColumns.size() );
		for ( Column joinColumn : joinColumns ) {
			valueSources.add(
					new ColumnSourceImpl(
							attribute,
							joinColumn,
							getDefaultLogicalJoinTableName( attribute )
					)
			);
		}
		return valueSources;
	}

	private String getDefaultLogicalJoinTableName(SingularAssociationAttribute attribute) {
		if ( attribute.getJoinTableAnnotation() == null ) {
			return null;
		}
		return bindingContext.getTypedValueExtractor( String.class ).extract(
				attribute.getJoinTableAnnotation(),
				"name"
		);
	}

	@Override
	public JoinColumnResolutionDelegate getForeignKeyTargetColumnResolutionDelegate() {
		List<Column> joinColumns = associationAttribute().getJoinTableAnnotation() == null
				? associationAttribute().getJoinColumnValues()
				: associationAttribute().getInverseJoinColumnValues();
		boolean hasReferencedColumn = false;
		for ( Column joinColumn : joinColumns ) {
			if ( joinColumn.getReferencedColumnName() != null ) {
				hasReferencedColumn = true;
				break;
			}
		}
		return hasReferencedColumn ? new AnnotationJoinColumnResolutionDelegate() : null;
	}

	@Override
	public ForeignKeyInformation getForeignKeyInformation() {
		return foreignKeyInformation;
	}

	@Override
	public boolean isCascadeDeleteEnabled() {
		return false;
	}

	@Override
	public AttributePath getAttributePath() {
		return getAnnotatedAttribute().getPath();
	}

	@Override
	public AttributeRole getAttributeRole() {
		return getAnnotatedAttribute().getRole();
	}

	public class AnnotationJoinColumnResolutionDelegate
			implements ForeignKeyContributingSource.JoinColumnResolutionDelegate {

		@Override
		public List<? extends Selectable> getJoinColumns(JoinColumnResolutionContext context) {
			final List<Selectable> values = new ArrayList<Selectable>();
			final List<Column> joinColumns = associationAttribute().getJoinTableAnnotation() == null
					? associationAttribute().getJoinColumnValues()
					: associationAttribute().getInverseJoinColumnValues();
			for ( Column joinColumn : joinColumns ) {
				if ( joinColumn.getReferencedColumnName() == null ) {
					return context.resolveRelationalValuesForAttribute( null );
				}
				org.hibernate.mapping.Column resolvedColumn = context.resolveColumn(
						joinColumn.getReferencedColumnName(),
						null,
						null,
						null
				);
				values.add( resolvedColumn );
			}
			return values;
		}

		@Override
		public Table getReferencedTable(JoinColumnResolutionContext context) {
			return context.resolveTable(
					null,
					null,
					null
			);
		}

		@Override
		public String getReferencedAttributeName() {
			// in annotations we are not referencing attribute but column names via @JoinColumn(s)
			return null;
		}
	}

	@Override
	public boolean areValuesIncludedInInsertByDefault() {
		return true;
	}

	@Override
	public boolean areValuesIncludedInUpdateByDefault() {
		return true;
	}

	@Override
	public boolean areValuesNullableByDefault() {
		return true;
	}

	@Override
	public boolean isUnique() {
		// todo : see note on declaration
		return false;
	}
}


