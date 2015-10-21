/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
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

import java.util.List;

import org.hibernate.boot.model.JavaTypeDescriptor;
import org.hibernate.boot.model.source.internal.annotations.AttributeSource;
import org.hibernate.boot.model.source.internal.annotations.EmbeddableSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EmbeddableTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AttributeSourceBuildingHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.LocalMetadataBuildingContext;

/**
 * @author Steve Ebersole
 */
public class EmbeddableSourceImpl implements EmbeddableSource {
	private final EmbeddableTypeMetadata embeddableTypeMetadata;

	private final List<AttributeSource> attributeSources;

	public EmbeddableSourceImpl(
			EmbeddableTypeMetadata embeddableTypeMetadata,
			AttributeSourceBuildingHelper.AttributeBuilder attributeBuilder) {
		this.embeddableTypeMetadata = embeddableTypeMetadata;
		this.attributeSources = AttributeSourceBuildingHelper.buildAttributeSources( embeddableTypeMetadata, attributeBuilder );
	}

	protected EmbeddableTypeMetadata getEmbeddableTypeMetadata() {
		return embeddableTypeMetadata;
	}

	@Override
	public AttributePath getAttributePathBase() {
		return embeddableTypeMetadata.getAttributePathBase();
	}

	@Override
	public AttributeRole getAttributeRoleBase() {
		return embeddableTypeMetadata.getAttributeRoleBase();
	}

	@Override
	public List<AttributeSource> attributeSources() {
		return attributeSources;
	}

	@Override
	public JavaTypeDescriptor getTypeDescriptor() {
		return new JavaTypeDescriptorImpl( embeddableTypeMetadata.getClassInfo() );
	}

	@Override
	public String getParentReferenceAttributeName() {
		return embeddableTypeMetadata.getParentReferencingAttributeName();
	}

	@Override
	public String getTuplizerImplementationName() {
		return embeddableTypeMetadata.getCustomTuplizerClassName();
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public boolean isUnique() {
		// todo : check if this can be
		return false;
	}

	@Override
	public LocalMetadataBuildingContext getLocalMetadataBuildingContext() {
		return embeddableTypeMetadata.getLocalBindingContext();
	}
}
