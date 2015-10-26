/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.spi.IdentifierGenerationInformation;
import org.hibernate.boot.model.source.spi.IdentifierSourceSimple;
import org.hibernate.boot.model.source.spi.SingularAttributeSourceBasic;
import org.hibernate.id.EntityIdentifierNature;

/**
 * @author Steve Ebersole
 * @author Hardy Ferentschik
 */
public class IdentifierSourceSimpleImpl extends AbstractIdentifierSource implements IdentifierSourceSimple {
	private final SingularAttributeSourceBasic attributeSource;

	public IdentifierSourceSimpleImpl(
			RootEntitySourceImpl rootEntitySource,
			SingularAttributeSourceBasic attributeSource) {
		super( rootEntitySource );
		this.attributeSource = attributeSource;
	}

	@Override
	public EntityIdentifierNature getNature() {
		return EntityIdentifierNature.SIMPLE;
	}

	@Override
	public SingularAttributeSourceBasic getIdentifierAttributeSource() {
		return attributeSource;
	}

	@Override
	public IdentifierGenerationInformation getIdentifierGenerationInformation() {
		return attributeSource.getIdentifierGenerationInformation();
	}
}


