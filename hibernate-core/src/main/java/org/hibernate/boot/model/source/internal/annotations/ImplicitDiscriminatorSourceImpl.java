/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.naming.EntityNaming;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.spi.DiscriminatorSource;
import org.hibernate.boot.model.source.spi.RelationalValueSource;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.type.StandardBasicTypes;

/**
 * @author Steve Ebersole
 */
public class ImplicitDiscriminatorSourceImpl
		extends DiscriminatorSourceSupport
		implements DiscriminatorSource {

	private final EntityTypeMetadata entityTypeMetadata;
	private final RelationalValueSource relationalValueSource;

	public ImplicitDiscriminatorSourceImpl(EntityTypeMetadata entityTypeMetadata) {
		super( entityTypeMetadata );
		this.entityTypeMetadata = entityTypeMetadata;

		this.relationalValueSource = new ImplicitDiscriminatorColumnSource( entityTypeMetadata );
	}

	@Override
	public RelationalValueSource getDiscriminatorRelationalValueSource() {
		return relationalValueSource;
	}

	@Override
	public String getExplicitHibernateTypeName() {
		return StandardBasicTypes.STRING.getName();
	}

	@Override
	public EntityNaming getEntityNaming() {
		return entityTypeMetadata;
	}

	@Override
	public MetadataBuildingContext getBuildingContext() {
		return entityTypeMetadata.getLocalBindingContext();
	}
}
