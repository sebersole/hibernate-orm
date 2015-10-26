/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PluralAttribute;
import org.hibernate.boot.model.source.spi.AttributeSource;
import org.hibernate.boot.model.source.spi.PluralAttributeElementSourceAssociation;

/**
 * @author Gail Badner
 */
public abstract class AbstractPluralElementSourceAssociationImpl
		extends AbstractPluralAttributeElementSourceImpl
		implements PluralAttributeElementSourceAssociation {

//	private final Set<MappedByAssociationSource> ownedAssociationSources = new HashSet<MappedByAssociationSource>(  );

	public AbstractPluralElementSourceAssociationImpl(PluralAttributeSourceImpl pluralAttributeSource) {
		super( pluralAttributeSource );
	}

	@Override
	public String getReferencedEntityName() {
		return pluralAssociationAttribute().getElementDetails().getJavaType().name().toString();
	}

	@Override
	public boolean isIgnoreNotFound() {
		return pluralAssociationAttribute().isIgnoreNotFound();
	}

	public AttributeSource getAttributeSource() {
		return getPluralAttributeSource();
	}

//	@Override
//	public Set<MappedByAssociationSource> getOwnedAssociationSources() {
//		return ownedAssociationSources;
//	}
//
//	@Override
//	public void addMappedByAssociationSource(MappedByAssociationSource attributeSource) {
//		if ( attributeSource == null ) {
//			throw new IllegalArgumentException( "attributeSource must be non-null." );
//		}
//		ownedAssociationSources.add( attributeSource );
//	}

	@Override
	public boolean isMappedBy() {
		return false;
	}

//	@Override
//	public Set<CascadeStyle> getCascadeStyles() {
//		return getPluralAttributeSource().getUnifiedCascadeStyles();
//	}

	protected PluralAttribute pluralAssociationAttribute() {
		return getPluralAttribute();
	}
}
