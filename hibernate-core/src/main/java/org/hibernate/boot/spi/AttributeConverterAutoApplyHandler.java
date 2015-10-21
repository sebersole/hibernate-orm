/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.spi;

import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.boot.internal.MultipleMatchingConvertersException;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.AnnotationBindingContext;

/**
 * Access to AttributeConverters defined with {@code autoApply=true}
 *
 * @author Steve Ebersole
 */
public interface AttributeConverterAutoApplyHandler {
	/**
	 * Find the auto-apply AttributeConverter for the given member.
	 *
	 * @param memberDescriptor The member (of basic type) for which to find an auto-apply AttributeConverter
	 * @param context Access to the AnnotationBindingContext
	 *
	 * @return Descriptor for the matching AttributeConverter, or {@code null} if none found.
	 *
	 * @throws MultipleMatchingConvertersException If more than one AttributeConverter matched
	 */
	AttributeConverterDescriptor findAutoApplyConverterForAttribute(MemberDescriptor memberDescriptor, AnnotationBindingContext context);

	/**
	 * Find the auto-apply AttributeConverter for the elements of the collection represented by the given member.
	 *
	 * @param memberDescriptor The member which is a collection for which we are to find an auto-apply
	 * AttributeConverter for the elements (of basic type)
	 * @param context Access to the AnnotationBindingContext
	 *
	 * @return Descriptor for the matching AttributeConverter, or {@code null} if none found.
	 *
	 * @throws MultipleMatchingConvertersException If more than one AttributeConverter matched
	 */
	AttributeConverterDescriptor findAutoApplyConverterForCollectionElement(MemberDescriptor memberDescriptor, AnnotationBindingContext context);

	/**
	 * Find the auto-apply AttributeConverter for the key of the Map represented by the given member.
	 *
	 * @param memberDescriptor The member which is a Map for which we are to find an auto-apply
	 * AttributeConverter for the key (of basic type)
	 * @param context Access to the AnnotationBindingContext
	 *
	 * @return Descriptor for the matching AttributeConverter, or {@code null} if none found.
	 *
	 * @throws MultipleMatchingConvertersException If more than one AttributeConverter matched
	 */
	AttributeConverterDescriptor findAutoApplyConverterForMapKey(MemberDescriptor memberDescriptor, AnnotationBindingContext context);


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Deprecated HCANN forms

	@Deprecated
	AttributeConverterDescriptor findAutoApplyConverterForAttribute(XProperty xProperty, MetadataBuildingContext context);
	@Deprecated
	AttributeConverterDescriptor findAutoApplyConverterForCollectionElement(XProperty xProperty, MetadataBuildingContext context);
	@Deprecated
	AttributeConverterDescriptor findAutoApplyConverterForMapKey(XProperty xProperty, MetadataBuildingContext context);
}
