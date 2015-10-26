/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.List;

import org.hibernate.boot.model.JavaTypeDescriptor;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EmbeddableTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AttributeSourceBuildingHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.AttributeSource;
import org.hibernate.boot.model.source.spi.EmbeddableSource;
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
