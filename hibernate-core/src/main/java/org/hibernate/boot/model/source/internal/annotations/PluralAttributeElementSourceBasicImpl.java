/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.Column;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.HibernateTypeSource;
import org.hibernate.boot.model.source.spi.PluralAttributeElementNature;
import org.hibernate.boot.model.source.spi.PluralAttributeElementSourceBasic;
import org.hibernate.boot.model.source.spi.RelationalValueSource;
import org.hibernate.boot.spi.MetadataBuildingContext;


/**
 * @author Hardy Ferentschik
 */
public class PluralAttributeElementSourceBasicImpl
		extends AbstractPluralAttributeElementSourceImpl
		implements PluralAttributeElementSourceBasic {

	private final HibernateTypeSourceImpl typeSource;
	private final AttributePath attributePath;
	private final List<RelationalValueSource> relationalValueSources;

	public PluralAttributeElementSourceBasicImpl(PluralAttributeSourceImpl pluralAttributeSource) {
		super( pluralAttributeSource );

		this.typeSource = new HibernateTypeSourceImpl( getPluralAttribute().getElementDetails() );
		this.attributePath = pluralAttributeSource.getAttributePath().append( "element" );
		this.relationalValueSources = resolveRelationalValueSources();
	}

	private List<RelationalValueSource> resolveRelationalValueSources() {
		List<RelationalValueSource> valueSources = new ArrayList<RelationalValueSource>();
		if ( !getPluralAttribute().getColumnValues().isEmpty() ) {
			for ( Column columnValues : getPluralAttribute().getColumnValues() ) {
				valueSources.add( new ColumnSourceImpl( columnValues ) );
			}
		}
		return valueSources;
	}

	@Override
	public HibernateTypeSource getExplicitHibernateTypeSource() {
		return typeSource;
	}

	@Override
	public PluralAttributeElementNature getNature() {
		return getPluralAttribute().getElementDetails().getElementNature();
	}

	@Override
	public List<RelationalValueSource> getRelationalValueSources() {
		return relationalValueSources;
	}

	@Override
	public AttributePath getAttributePath() {
		return attributePath;
	}

	@Override
	public boolean isCollectionElement() {
		return true;
	}

	@Override
	public MetadataBuildingContext getBuildingContext() {
		return getPluralAttribute().getContext();
	}

	// TODO - these values are also hard coded in the hbm version of this source implementation. Do we really need them? (HF)
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
}


