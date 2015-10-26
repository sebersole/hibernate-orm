/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.List;
import java.util.Map;

import org.hibernate.boot.model.source.spi.AnyDiscriminatorSource;
import org.hibernate.boot.model.source.spi.AnyKeySource;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.HibernateTypeSource;
import org.hibernate.boot.model.source.spi.PluralAttributeElementNature;
import org.hibernate.boot.model.source.spi.PluralAttributeElementSourceManyToAny;
import org.hibernate.boot.model.source.spi.RelationalValueSource;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.cfg.NotYetImplementedException;

/**
 * PluralAttributeElementSourceManyToAny implementation
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class PluralAttributeElementSourceAssociationManyToAnyImpl
		extends AbstractPluralElementSourceAssociationImpl
		implements PluralAttributeElementSourceManyToAny {

	private final AnyDiscriminatorSource discriminatorSource;
	private final AnyKeySource keySource;

	public PluralAttributeElementSourceAssociationManyToAnyImpl(final PluralAttributeSourceImpl pluralAttributeSource) {
		super( pluralAttributeSource );

		this.discriminatorSource = new AnyDiscriminatorSourceImpl( pluralAttributeSource );
		this.keySource = new AnyKeySourceImpl( pluralAttributeSource );

		throw new NotYetImplementedException( "many-to-any support not yet implemented" );
	}

	@Override
	public PluralAttributeElementNature getNature() {
		return PluralAttributeElementNature.MANY_TO_ANY;
	}

	@Override
	public AnyDiscriminatorSource getDiscriminatorSource() {
		return discriminatorSource;
	}

	@Override
	public AnyKeySource getKeySource() {
		return keySource;
	}

	private static class AnyDiscriminatorSourceImpl implements AnyDiscriminatorSource {
		private final PluralAttributeSourceImpl pluralAttributeSource;

		public AnyDiscriminatorSourceImpl(PluralAttributeSourceImpl pluralAttributeSource) {
			this.pluralAttributeSource = pluralAttributeSource;
		}

		@Override
		public HibernateTypeSource getTypeSource() {
			// todo : perhaps this should be "any"
			return null;
		}

		@Override
		public RelationalValueSource getRelationalValueSource() {
			return null;
		}

		@Override
		public Map<String, String> getValueMappings() {
			return null;
		}

		@Override
		public AttributePath getAttributePath() {
			return pluralAttributeSource.getAttributePath().append( "element" );
		}

		@Override
		public MetadataBuildingContext getBuildingContext() {
			return pluralAttributeSource.getPluralAttribute().getContext();
		}
	}

	private static class AnyKeySourceImpl implements AnyKeySource {
		private final PluralAttributeSourceImpl pluralAttributeSource;

		public AnyKeySourceImpl(PluralAttributeSourceImpl pluralAttributeSource) {
			this.pluralAttributeSource = pluralAttributeSource;
		}

		@Override
		public HibernateTypeSource getTypeSource() {
			return null;
		}

		@Override
		public List<RelationalValueSource> getRelationalValueSources() {
			return null;
		}

		@Override
		public AttributePath getAttributePath() {
			return null;
		}

		@Override
		public MetadataBuildingContext getBuildingContext() {
			return null;
		}
	}
}

