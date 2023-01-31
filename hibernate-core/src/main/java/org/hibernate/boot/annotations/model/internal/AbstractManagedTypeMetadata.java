/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.internal;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import org.hibernate.annotations.Any;
import org.hibernate.annotations.ManyToAny;
import org.hibernate.boot.annotations.AnnotationSourceLogging;
import org.hibernate.boot.annotations.model.spi.AttributeMetadata;
import org.hibernate.boot.annotations.model.spi.LocalAnnotationProcessingContext;
import org.hibernate.boot.annotations.model.spi.ManagedTypeMetadata;
import org.hibernate.boot.annotations.model.spi.OverrideAndConverterCollector;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.MemberDetails;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.boot.model.source.spi.AttributePath;
import org.hibernate.boot.model.source.spi.AttributeRole;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.internal.util.IndexedConsumer;

import jakarta.persistence.Basic;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import static org.hibernate.internal.util.collections.CollectionHelper.arrayList;

/**
 * Models metadata about a JPA {@linkplain jakarta.persistence.metamodel.ManagedType managed-type}.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public abstract class AbstractManagedTypeMetadata implements OverrideAndConverterCollector, ManagedTypeMetadata {
	private final ClassDetails classDetails;
	private final AttributePath attributePathBase;
	private final AttributeRole attributeRoleBase;
	private final LocalAnnotationProcessingContextImpl localProcessingContext;

	/**
	 * This form is intended for construction of the root of an entity hierarchy
	 * and its mapped-superclasses
	 */
	public AbstractManagedTypeMetadata(ClassDetails classDetails, AnnotationProcessingContext processingContext) {
		this.classDetails = classDetails;
		this.attributeRoleBase = new AttributeRole( classDetails.getName() );
		this.attributePathBase = new AttributePath();
		this.localProcessingContext = new LocalAnnotationProcessingContextImpl( this, processingContext );
	}

	/**
	 * This form is used to create Embedded references
	 *
	 * @param classDetails The Embeddable descriptor
	 * @param attributeRoleBase The base for the roles of attributes created *from* here
	 * @param attributePathBase The base for the paths of attributes created *from* here
	 */
	public AbstractManagedTypeMetadata(
			ClassDetails classDetails,
			AttributeRole attributeRoleBase,
			AttributePath attributePathBase,
			AnnotationProcessingContext processingContext) {
		this.classDetails = classDetails;
		this.attributeRoleBase = attributeRoleBase;
		this.attributePathBase = attributePathBase;
		this.localProcessingContext = new LocalAnnotationProcessingContextImpl( this, processingContext );
	}

	public LocalAnnotationProcessingContext getLocalProcessingContext() {
		return localProcessingContext;
	}

	public ClassDetails getManagedClass() {
		return classDetails;
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
		return classDetails.getName();
	}

	@Override
	public boolean isAbstract() {
		return classDetails.isAbstract();
	}

	@Override
	public String toString() {
		return "ManagedTypeMetadata(" + classDetails.getName() + ")";
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// attribute handling

	protected abstract List<AttributeMetadata> attributeList();

	@Override
	public int getNumberOfAttributes() {
		return attributeList().size();
	}

	@Override
	public Collection<AttributeMetadata> getAttributes() {
		return attributeList();
	}

	@Override
	public void forEachAttribute(IndexedConsumer<AttributeMetadata> consumer) {
		for ( int i = 0; i < attributeList().size(); i++ ) {
			consumer.accept( i, attributeList().get( i ) );
		}
	}

	protected List<AttributeMetadata> resolveAttributes() {
		final List<MemberDetails> backingMembers = localProcessingContext
				.getMetadataBuildingContext()
				.getBuildingOptions()
				.getPersistentAttributeMemberResolver()
				.resolveAttributesMembers( classDetails, getAccessType(), localProcessingContext );

		final List<AttributeMetadata> attributeList = arrayList( backingMembers.size() );

		for ( MemberDetails backingMember : backingMembers ) {
			final AttributeMetadata attribute = new AttributeMetadataImpl(
					backingMember.resolveAttributeName(),
					determineAttributeNature( backingMember ),
					backingMember
			);
			attributeList.add( attribute );
		}

		return attributeList;
	}

	/**
	 * Determine the attribute's nature - is it a basic mapping, an embeddable, ...?
	 *
	 * Also performs some simple validation around multiple natures being indicated
	 */
	private AttributeMetadata.AttributeNature determineAttributeNature(MemberDetails backingMember) {
		final EnumSet<AttributeMetadata.AttributeNature> natures = EnumSet.noneOf( AttributeMetadata.AttributeNature.class );

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// first, look for explicit nature annotations

		final AnnotationUsage<Any> any = backingMember.getAnnotation( HibernateAnnotations.ANY );
		final AnnotationUsage<Basic> basic = backingMember.getAnnotation( JpaAnnotations.BASIC );
		final AnnotationUsage<ElementCollection> elementCollection = backingMember.getAnnotation( JpaAnnotations.ELEMENT_COLLECTION );
		final AnnotationUsage<Embedded> embedded = backingMember.getAnnotation( JpaAnnotations.EMBEDDED );
		final AnnotationUsage<EmbeddedId> embeddedId = backingMember.getAnnotation( JpaAnnotations.EMBEDDED_ID );
		final AnnotationUsage<ManyToAny> manyToAny = backingMember.getAnnotation( HibernateAnnotations.MANY_TO_ANY );
		final AnnotationUsage<ManyToMany> manyToMany = backingMember.getAnnotation( JpaAnnotations.MANY_TO_MANY );
		final AnnotationUsage<ManyToOne> manyToOne = backingMember.getAnnotation( JpaAnnotations.MANY_TO_ONE );
		final AnnotationUsage<OneToMany> oneToMany = backingMember.getAnnotation( JpaAnnotations.ONE_TO_MANY );
		final AnnotationUsage<OneToOne> oneToOne = backingMember.getAnnotation( JpaAnnotations.ONE_TO_ONE );

		if ( basic != null ) {
			natures.add( AttributeMetadata.AttributeNature.BASIC );
		}

		if ( embedded != null
				|| embeddedId != null
				|| backingMember.getType().getAnnotation( JpaAnnotations.EMBEDDABLE ) != null ) {
			natures.add( AttributeMetadata.AttributeNature.EMBEDDED );
		}

		if ( any != null ) {
			natures.add( AttributeMetadata.AttributeNature.ANY );
		}

		if ( oneToOne != null
				|| manyToOne != null ) {
			natures.add( AttributeMetadata.AttributeNature.TO_ONE );
		}

		final boolean plural = oneToMany != null
				|| manyToMany != null
				|| elementCollection != null
				|| manyToAny != null;
		if ( plural ) {
			natures.add( AttributeMetadata.AttributeNature.PLURAL );
		}

		// look at annotations that imply a nature
		//		NOTE : these could apply to the element or index of collection, so
		//		only do these if it is not a collection

		if ( !plural ) {
			// first implicit basic nature
			if ( backingMember.getAnnotation( JpaAnnotations.TEMPORAL ) != null
					|| backingMember.getAnnotation( JpaAnnotations.LOB ) != null
					|| backingMember.getAnnotation( JpaAnnotations.ENUMERATED ) != null
					|| backingMember.getAnnotation( JpaAnnotations.CONVERT ) != null
					|| backingMember.getAnnotation( JpaAnnotations.VERSION ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.GENERATED ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.NATIONALIZED ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.TZ_COLUMN ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.TZ_STORAGE ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.TYPE ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.TENANT_ID ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.JAVA_TYPE ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.JDBC_TYPE_CODE ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.JDBC_TYPE ) != null ) {
				natures.add( AttributeMetadata.AttributeNature.BASIC );
			}

			// then embedded
			if ( backingMember.getAnnotation( HibernateAnnotations.EMBEDDABLE_INSTANTIATOR ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.COMPOSITE_TYPE ) != null ) {
				natures.add( AttributeMetadata.AttributeNature.EMBEDDED );
			}

			// and any
			if ( backingMember.getAnnotation( HibernateAnnotations.ANY_DISCRIMINATOR ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.ANY_DISCRIMINATOR_VALUE ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.ANY_DISCRIMINATOR_VALUES ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.ANY_KEY_JAVA_TYPE ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.ANY_KEY_JAVA_CLASS ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.ANY_KEY_JDBC_TYPE ) != null
					|| backingMember.getAnnotation( HibernateAnnotations.ANY_KEY_JDBC_TYPE_CODE ) != null ) {
				natures.add( AttributeMetadata.AttributeNature.ANY );
			}
		}

		int size = natures.size();
		switch ( size ) {
			case 0: {
				AnnotationSourceLogging.ANNOTATION_SOURCE_LOGGER.debugf(
						"Implicitly interpreting attribute `%s` as BASIC",
						backingMember.resolveAttributeName()
				);
				return AttributeMetadata.AttributeNature.BASIC;
			}
			case 1: {
				return natures.iterator().next();
			}
			default: {
				throw new MultipleAttributeNaturesException( backingMember.resolveAttributeName(), natures );
			}
		}
	}

	@Override
	public <A extends Annotation> AnnotationUsage<A> findAnnotation(AnnotationDescriptor<A> type) {
		return classDetails.getAnnotation( type );
	}

	@Override
	public <A extends Annotation> List<AnnotationUsage<A>> findAnnotations(AnnotationDescriptor<A> type) {
		return classDetails.getAnnotations( type );
	}

	@Override
	public <A extends Annotation> void forEachAnnotation(AnnotationDescriptor<A> type, Consumer<AnnotationUsage<A>> consumer) {
		classDetails.forEachAnnotation( type, consumer );
	}

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
