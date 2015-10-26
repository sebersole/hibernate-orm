/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.type;

import java.util.Collection;
import javax.persistence.AccessType;

import org.hibernate.AssertionFailure;
import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.ConvertConversionInfo;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AssociationOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.AttributeOverride;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.EmbeddedContainer;
import org.hibernate.boot.model.source.internal.annotations.metadata.util.ConverterAndOverridesHelper;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.internal.util.ReflectHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

/**
 * This is called *Embeddable* type metadata to closely follow the JPA
 * terminology; just be aware that this more closely models the *Embedded*.
 * Generally this is used as a delegate from "composite contexts" such as
 * Embedded attributes and plural attributes with Embedded elements/keys.
 *
 * @author Steve Ebersole
 * @author Hardy Ferentschik
 */
public class EmbeddableTypeMetadata extends ManagedTypeMetadata {
	private final EmbeddedContainer container;
	private final NaturalIdMutability naturalIdMutability;
	private final String parentReferencingAttributeName;
	private final String customTuplizerClassName;

	public EmbeddableTypeMetadata(
			ClassInfo embeddableType,
			EmbeddedContainer container,
			AttributeRole attributeRoleBase,
			AttributePath attributePathBase,
			AccessType defaultAccessType,
			String defaultAccessorStrategy,
			RootAnnotationBindingContext context) {
		super( embeddableType, attributeRoleBase, attributePathBase, defaultAccessType, defaultAccessorStrategy, context );

		this.container = container;
		this.naturalIdMutability = container.getContainerNaturalIdMutability();
		this.parentReferencingAttributeName = decodeParentAnnotation( embeddableType );
		this.customTuplizerClassName = decodeTuplizerAnnotation( container );

		// the idea here is to collect up class-level annotations and to apply
		// the maps from supers
		collectConversionInfo();
		collectAttributeOverrides();
		collectAssociationOverrides();

		collectAttributesIfNeeded();
	}

	private String decodeParentAnnotation(ClassInfo embeddableType) {
		final Collection<AnnotationInstance> parentAnnotations = embeddableType.annotations().get(
				HibernateDotNames.PARENT
		);
		if ( parentAnnotations == null || parentAnnotations.isEmpty() ) {
			return null;
		}

		if ( parentAnnotations.size() > 1 ) {
			throw getLocalBindingContext().makeMappingException(
					"Embeddable class contained multiple @Parent annotations; only one is allowed"
			);
		}

		final AnnotationInstance parentAnnotation = parentAnnotations.iterator().next();
		return extractParentAttributeName( parentAnnotation.target() );
	}

	/**
	 * Expects a method or field annotation target and returns the property name for this target
	 *
	 * @param target the annotation target
	 *
	 * @return the property name of the target. For a field it is the field name and for a method name it is
	 *         the method name stripped of 'is', 'has' or 'get'
	 */
	public static String extractParentAttributeName(AnnotationTarget target) {
		if ( !( target instanceof MethodInfo || target instanceof FieldInfo ) ) {
			throw new AssertionFailure( "Unexpected annotation target " + target.toString() );
		}

		if ( target instanceof FieldInfo ) {
			return ( (FieldInfo) target ).name();
		}
		else {
			return ReflectHelper.getPropertyNameFromGetterMethod( ( (MethodInfo) target ).name() );
		}
	}

	private String decodeTuplizerAnnotation(EmbeddedContainer container) {
		// prefer tuplizer defined at the embedded level
		// might be null though in the case of an IdClass
		if ( container.getBackingMember() != null ) {
			final AnnotationInstance tuplizerAnnotation = getLocalBindingContext().getMemberAnnotationInstances(
					container.getBackingMember()
			).get(
					HibernateDotNames.TUPLIZER
			);
			if ( tuplizerAnnotation != null ) {
				return tuplizerAnnotation.value( "impl" ).asString();
			}
		}

		// The tuplizer on the embeddable (if one) would be covered by this.getCustomTuplizerClassName()...
		return super.getCustomTuplizerClassName();
	}

	private void collectConversionInfo() {
	}

	private void collectAttributeOverrides() {
		ConverterAndOverridesHelper.processAttributeOverrides( this );
	}

	private void collectAssociationOverrides() {
	}

	public String getParentReferencingAttributeName() {
		return parentReferencingAttributeName;
	}

	public NaturalIdMutability getNaturalIdMutability() {
		 return naturalIdMutability;
	}

	@Override
	public String getCustomTuplizerClassName() {
		return customTuplizerClassName;
	}

	@Override
	public ConvertConversionInfo locateConversionInfo(AttributePath attributePath) {
		return container.locateConversionInfo( attributePath );
	}

	@Override
	public AttributeOverride locateAttributeOverride(AttributePath attributePath) {
		return container.locateAttributeOverride( attributePath );
	}

	@Override
	public AssociationOverride locateAssociationOverride(AttributePath attributePath) {
		return container.locateAssociationOverride( attributePath );
	}

	@Override
	public void registerConverter(AttributePath attributePath, ConvertConversionInfo conversionInfo) {
		container.registerConverter( attributePath, conversionInfo );
	}

	@Override
	public void registerAttributeOverride(AttributePath attributePath, AttributeOverride override) {
		container.registerAttributeOverride( attributePath, override );
	}

	@Override
	public void registerAssociationOverride(AttributePath attributePath, AssociationOverride override) {
		container.registerAssociationOverride( attributePath, override );
	}

	@Override
	public boolean canAttributesBeInsertable() {
		return container.getContainerInsertability();
	}

	@Override
	@SuppressWarnings("SimplifiableIfStatement")
	public boolean canAttributesBeUpdatable() {
		if ( naturalIdMutability == NaturalIdMutability.IMMUTABLE ) {
			return false;
		}
		return container.getContainerUpdatability();
	}

	@Override
	public NaturalIdMutability getContainerNaturalIdMutability() {
		return naturalIdMutability;
	}

	@Override
	public boolean hasMultiTenancySourceInformation() {
		return false;
	}
}


