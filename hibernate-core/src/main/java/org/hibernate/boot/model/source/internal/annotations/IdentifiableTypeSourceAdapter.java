/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.IdentifiableTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.MappedSuperclassTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.util.AttributeSourceBuildingHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.AttributeSource;
import org.hibernate.boot.model.source.spi.EntityHierarchySource;
import org.hibernate.boot.model.source.spi.IdentifiableTypeSource;
import org.hibernate.boot.model.source.spi.InheritanceType;
import org.hibernate.boot.model.source.spi.JpaCallbackSource;
import org.hibernate.boot.model.source.spi.LocalMetadataBuildingContext;


/**
 * Base class adapting "identifiable types" (entities and mapped-superclasses)
 * from annotation (plus XML overrides) representation to the source
 * representation consumed by the metamodel binder.
 *
 * @author Steve Ebersole
 */
public abstract class IdentifiableTypeSourceAdapter implements IdentifiableTypeSource {
	private final IdentifiableTypeMetadata identifiableTypeMetadata;
	private final EntityHierarchySourceImpl hierarchy;
	private final IdentifiableTypeSourceAdapter superTypeSource;

	private Collection<IdentifiableTypeSource> subclassSources;
	private List<AttributeSource> attributes;

	/**
	 * This form is intended for the root of a hierarchy
	 *
	 * @param identifiableTypeMetadata Metadata about the "identifiable type"
	 * @param hierarchy The hierarchy flyweight
	 */
	protected IdentifiableTypeSourceAdapter(
			IdentifiableTypeMetadata identifiableTypeMetadata,
			EntityHierarchySourceImpl hierarchy,
			boolean isRootEntity) {
		this.identifiableTypeMetadata = identifiableTypeMetadata;
		this.hierarchy = hierarchy;

		// walk up
		this.superTypeSource = walkRootSuperclasses( identifiableTypeMetadata.getSuperType(), hierarchy );
		if ( superTypeSource != null ) {
			superTypeSource.addSubclass( this );
		}

		if ( isRootEntity ) {
			// walk down
			walkSubclasses( identifiableTypeMetadata, this );
		}
	}

	private void addSubclass(IdentifiableTypeSourceAdapter subclassSource) {
		assert subclassSource.identifiableTypeMetadata.getSuperType() == this.identifiableTypeMetadata;
		if ( subclassSources == null ) {
			subclassSources = new ArrayList<IdentifiableTypeSource>();
		}
		subclassSources.add( subclassSource );
	}

	private static IdentifiableTypeSourceAdapter walkRootSuperclasses(
			ManagedTypeMetadata clazz,
			EntityHierarchySourceImpl hierarchy) {
		if ( clazz == null ) {
			return null;
		}

		if ( MappedSuperclassTypeMetadata.class.isInstance( clazz ) ) {
			// IMPORTANT : routing through the root constructor!
			return new MappedSuperclassSourceImpl( (MappedSuperclassTypeMetadata) clazz, hierarchy );
		}
		else {
			throw new UnsupportedOperationException(
					String.format(
							Locale.ENGLISH,
							"Unexpected @Entity [%s] as MappedSuperclass of entity hierarchy",
							clazz.getName()
					)
			);
		}
	}

	private void walkSubclasses(
			IdentifiableTypeMetadata classMetadata,
			IdentifiableTypeSourceAdapter classSource) {
		for ( ManagedTypeMetadata subclass : classMetadata.getSubclasses() ) {
			final IdentifiableTypeSourceAdapter subclassSource;
			if ( MappedSuperclassTypeMetadata.class.isInstance( subclass ) ) {
				subclassSource = new MappedSuperclassSourceImpl(
						(MappedSuperclassTypeMetadata) subclass,
						this.hierarchy,
						classSource
				);
			}
			else if ( this.hierarchy.getHierarchyInheritanceType() == InheritanceType.JOINED ) {
				subclassSource = new JoinedSubclassEntitySourceImpl(
						(EntityTypeMetadata) subclass,
						this.hierarchy,
						classSource
				);
			}
			else {
				subclassSource = new SubclassEntitySourceImpl(
						(EntityTypeMetadata) subclass,
						this.hierarchy,
						classSource
				);
			}
			classSource.addSubclass( subclassSource );

			walkSubclasses( (IdentifiableTypeMetadata) subclass, subclassSource );
		}
	}


	/**
	 * This form is intended for creating subclasses
	 *
	 * @param identifiableTypeMetadata
	 * @param hierarchy
	 * @param superTypeSource
	 */
	protected IdentifiableTypeSourceAdapter(
			IdentifiableTypeMetadata identifiableTypeMetadata,
			EntityHierarchySourceImpl hierarchy,
			IdentifiableTypeSourceAdapter superTypeSource) {
		this.identifiableTypeMetadata = identifiableTypeMetadata;
		this.hierarchy = hierarchy;
		this.superTypeSource = superTypeSource;
	}

	public IdentifiableTypeMetadata getIdentifiableTypeMetadata() {
		return identifiableTypeMetadata;
	}

	public ManagedTypeMetadata getManagedTypeMetadata() {
		return identifiableTypeMetadata;
	}

	@Override
	public Origin getOrigin() {
		return identifiableTypeMetadata.getLocalBindingContext().getOrigin();
	}

	@Override
	public LocalMetadataBuildingContext getLocalMetadataBuildingContext() {
		return identifiableTypeMetadata.getLocalBindingContext();
	}

	@Override
	public EntityHierarchySource getHierarchy() {
		return hierarchy;
	}

	@Override
	public String getTypeName() {
		return identifiableTypeMetadata.getName();
	}

	@Override
	public IdentifiableTypeSourceAdapter getSuperType() {
		return superTypeSource;
	}

	@Override
	public Collection<IdentifiableTypeSource> getSubTypes() {
		return subclassSources == null ? Collections.<IdentifiableTypeSource>emptyList() : subclassSources;
	}

	@Override
	public List<JpaCallbackSource> getJpaCallbackClasses() {
		return identifiableTypeMetadata.getJpaCallbacks();
	}

	@Override
	public AttributePath getAttributePathBase() {
		return identifiableTypeMetadata.getAttributePathBase();
	}

	@Override
	public AttributeRole getAttributeRoleBase() {
		return identifiableTypeMetadata.getAttributeRoleBase();
	}

	@Override
	public List<AttributeSource> attributeSources() {
		if ( attributes == null ) {
			attributes = AttributeSourceBuildingHelper.buildAttributeSources(
					identifiableTypeMetadata,
					AttributeSourceBuildingHelper.StandardAttributeBuilder.INSTANCE
			);
		}
		return attributes;
	}
}
