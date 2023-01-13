/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.type.internal;

import org.hibernate.annotations.AttributeAccessor;
import org.hibernate.boot.annotations.parts.spi.OverrideAndConverterCollector;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.ManagedClass;
import org.hibernate.boot.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.annotations.type.spi.ManagedTypeAnnotationBindingContext;
import org.hibernate.boot.annotations.type.spi.ManagedTypeMetadata;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.property.access.spi.PropertyAccessStrategy;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;

/**
 * Models metadata about a JPA {@linkplain jakarta.persistence.metamodel.ManagedType managed-type}.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public abstract class AbstractManagedTypeMetadata implements OverrideAndConverterCollector, ManagedTypeMetadata {
    private static final CoreMessageLogger LOG = CoreLogging.messageLogger( AbstractManagedTypeMetadata.class );

	private final ManagedClass managedClass;
	private final ManagedTypeAnnotationBindingContext localBindingContext;

	private final AttributePath attributePathBase;
	private final AttributeRole attributeRoleBase;

	private final AccessType classLevelAccessType;
	private final Class <? extends PropertyAccessStrategy> classLevelAccessorStrategy;

	/**
	 * This form is intended for construction of the root of an entity hierarchy,
	 * and its MappedSuperclasses
	 *
	 * @param managedClass Metadata for the Entity/MappedSuperclass
	 * @param defaultAccessType The default AccessType for the entity hierarchy
	 * @param bindingContext The binding context
	 */
	public AbstractManagedTypeMetadata(
			ManagedClass managedClass,
			AccessType defaultAccessType,
			boolean isRootEntity,
			AnnotationBindingContext bindingContext) {
		assert isRootEntity;

		this.managedClass =  managedClass;
		this.localBindingContext = new ManagedTypeAnnotationBindingContext( this, bindingContext );

		this.classLevelAccessType = determineAccessType( defaultAccessType );
		this.classLevelAccessorStrategy = determineExplicitAccessorStrategy( null );

		this.attributeRoleBase = new AttributeRole( managedClass.getName() );
		this.attributePathBase = new AttributePath();
	}

	/**
	 * This form is intended for construction of entity hierarchy subclasses.
	 */
	public AbstractManagedTypeMetadata(
			ManagedClass managedClass,
			AbstractManagedTypeMetadata superType,
			AccessType defaultAccessType,
			AnnotationBindingContext bindingContext) {
		this.managedClass =  managedClass;
		this.localBindingContext = new ManagedTypeAnnotationBindingContext( this, bindingContext );

		this.classLevelAccessType = determineAccessType( defaultAccessType );
		this.classLevelAccessorStrategy = determineExplicitAccessorStrategy( null );

		this.attributeRoleBase = new AttributeRole( managedClass.getName() );
		this.attributePathBase = new AttributePath();
	}

	/**
	 * This form is used to create Embedded references
	 *
	 * @param managedClass The Embeddable descriptor
	 * @param attributeRoleBase The base for the roles of attributes created *from* here
	 * @param attributePathBase The base for the paths of attributes created *from* here
	 * @param defaultAccessType The default AccessType from the context of this Embedded
	 * @param defaultAccessorStrategy The default accessor strategy from the context of this Embedded
	 * @param bindingContext The binding context
	 */
	public AbstractManagedTypeMetadata(
			ManagedClass managedClass,
			AttributeRole attributeRoleBase,
			AttributePath attributePathBase,
			AccessType defaultAccessType,
			Class<? extends PropertyAccessStrategy> defaultAccessorStrategy,
			AnnotationBindingContext bindingContext) {
		this.managedClass = managedClass;
		this.localBindingContext = new ManagedTypeAnnotationBindingContext( this, bindingContext );

		this.classLevelAccessType = determineAccessType( defaultAccessType );
		this.classLevelAccessorStrategy = determineExplicitAccessorStrategy( defaultAccessorStrategy );

		this.attributeRoleBase = attributeRoleBase;
		this.attributePathBase = attributePathBase;
	}

	private AccessType determineAccessType(AccessType defaultAccessType) {
		final AnnotationUsage<Access> accessAnnotation = managedClass.getAnnotation( JpaAnnotations.ACCESS );
		if ( accessAnnotation != null ) {
			final AnnotationUsage.AttributeValue accessTypeValue = accessAnnotation.getValueAttributeValue();
			return accessTypeValue.getValue();
		}
		return defaultAccessType;
	}

	private Class<? extends PropertyAccessStrategy> determineExplicitAccessorStrategy(Class<? extends PropertyAccessStrategy> defaultValue) {
		// look for a @AttributeAccessor annotation
		final AnnotationUsage<AttributeAccessor> attrAccessorAnnotation = managedClass.getAnnotation( HibernateAnnotations.ATTRIBUTE_ACCESSOR );
		if ( attrAccessorAnnotation != null ) {
			return attrAccessorAnnotation.getAttributeValue( "strategy" ).getValue();
		}
		return defaultValue;
	}

	public ManagedTypeAnnotationBindingContext getLocalBindingContext() {
		return localBindingContext;
	}

	public ManagedClass getManagedClass() {
		return managedClass;
	}

	@Override
	public AttributeRole getAttributeRoleBase() {
		return attributeRoleBase;
	}

	@Override
	public AttributePath getAttributePathBase() {
		return attributePathBase;
	}

	@Override
	public String getName() {
		return managedClass.getName();
	}

	@Override
	public boolean isAbstract() {
		return managedClass.isAbstract();
	}

	@Override
	public AccessType getClassLevelAccessType() {
		return classLevelAccessType;
	}

	@Override
	public String toString() {
		return "ManagedTypeMetadata(" + managedClass.getName() + ")";
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// attribute handling

//	/**
//	 * Collect all persistent attributes for this managed type
//	 */
//	private void collectPersistentAttributes() {
//		// Call the strategy responsible for resolving the members that identify a persistent attribute
//		final List<MemberDetails> backingMembers = localBindingContext.getBuildingOptions()
//				.getPersistentAttributeMemberResolver()
//				.resolveAttributesMembers( managedClass, classLevelAccessType, localBindingContext );
//
//		for ( MemberDetails backingMember : backingMembers ) {
//			final AttributeMetadata attribute = AttributeFactory.buildAttribute(
//					AbstractManagedTypeMetadata.this,
//					backingMember,
//					localBindingContext
//			);
//			categorizeAttribute( attribute );
//		}
//	}
//
//	protected void categorizeAttribute(AttributeMetadata attr) {
//		attributeMetadataMap.put( attr.getName(), attr );
//	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Stuff affecting attributes built from this managed type.

	public boolean canAttributesBeInsertable() {
		return true;
	}

	public boolean canAttributesBeUpdatable() {
		return true;
	}

	public NaturalIdMutability getContainerNaturalIdMutability() {
		return NaturalIdMutability.NOT_NATURAL_ID;
	}
}
