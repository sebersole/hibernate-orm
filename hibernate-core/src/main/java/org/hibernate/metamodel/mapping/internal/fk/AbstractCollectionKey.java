/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;
import org.hibernate.metamodel.mapping.CollectionPart;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.MappingModelCreationException;
import org.hibernate.metamodel.mapping.ModelPart;
import org.hibernate.metamodel.mapping.PluralAttributeMapping;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.EntityCollectionPart;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.sql.results.graph.FetchOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

/**
 * Base support for CollectionKey implementations
 *
 * @author Steve Ebersole
 */
public abstract class AbstractCollectionKey extends AbstractKeyModelPart implements CollectionKey, FetchOptions {
	private final PluralAttributeMapping attributeMapping;

	public AbstractCollectionKey(PluralAttributeMapping attributeMapping) {
		super( attributeMapping.getNavigableRole().append( PART_NAME ) );

		this.attributeMapping = attributeMapping;
	}

	protected final void postConstruct(
			Consumer<Consumer<ForeignKey>> fkConsumerAccess,
			MappingModelCreationProcess creationProcess) {
//		final CollectionPart elementDescriptor = attributeMapping.getElementDescriptor();
//		assert elementDescriptor instanceof EntityCollectionPart;
//		// could technically be an embeddable too, no?
//
//		super.afterInitialize(
//				( (EntityCollectionPart) elementDescriptor ).getAssociatedEntityMappingType(),
//				attributeMapping.getCollectionDescriptor().getMappedByProperty(),
//				foreignKeyGenerator,
//				creationProcess
//		);

		final String mappedByProperty = attributeMapping.getCollectionDescriptor().getMappedByProperty();
		if ( mappedByProperty != null ) {
			final EntityCollectionPart mappedByContainer;

			final CollectionPart elementDescriptor = attributeMapping.getElementDescriptor();
			final CollectionPart indexDescriptor = attributeMapping.getIndexDescriptor();

			if ( elementDescriptor instanceof EntityCollectionPart ) {
				mappedByContainer = (EntityCollectionPart) elementDescriptor;
			}
			else if ( indexDescriptor instanceof EntityCollectionPart ) {
				mappedByContainer = (EntityCollectionPart) indexDescriptor;
			}
			else {
				mappedByContainer = null;
			}

			if ( mappedByContainer == null ) {
				throw new MappingModelCreationException(
						"Could not locate mapped-by container for CollectionKey : " + attributeMapping.getNavigableRole()
								.getFullPath()
				);
			}

			creationProcess.registerSubPartGroupInitializationListener(
					mappedByContainer.getAssociatedEntityMappingType(),
					MappingModelCreationProcess.SubPartGroup.NORMAL,
					() -> {
						final ModelPart mappedByPart = mappedByContainer.findSubPart( mappedByProperty );
						if ( !( mappedByPart instanceof ToOneAttributeMapping ) ) {
							throw new MappingModelCreationException(
									"Mapped-by did not reference to-one association : " + attributeMapping.getNavigableRole()
											.getFullPath()
							);
						}

						final ToOneAttributeMapping mappedByAttribute = (ToOneAttributeMapping) mappedByPart;

						super.postConstruct(
								() -> mappedByAttribute,
								fkConsumerAccess,
								creationProcess
						);
					}
			);

			return;
		}

		super.postConstruct(
				null,
				fkConsumerAccess,
				creationProcess
		);
	}

	public PluralAttributeMapping getAttributeMapping() {
		return attributeMapping;
	}

	@Override
	public PluralAttributeMapping getMappedModelPart() {
		return attributeMapping;
	}

	@Override
	public EntityMappingType findContainingEntityMapping() {
		return getMappedModelPart().findContainingEntityMapping();
	}

	@Override
	public String getPartName() {
		return PART_NAME;
	}

	@Override
	public String getFetchableName() {
		return PART_NAME;
	}

	@Override
	public JavaTypeDescriptor<?> getJavaTypeDescriptor() {
		return getAttributeMapping().getJavaTypeDescriptor();
	}

	@Override
	public FetchOptions getMappedFetchOptions() {
		return this;
	}

	@Override
	public FetchStyle getStyle() {
		return FetchStyle.JOIN;
	}

	@Override
	public FetchTiming getTiming() {
		return FetchTiming.IMMEDIATE;
	}
}
