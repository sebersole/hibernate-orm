/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.spi;

import java.util.List;

import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.MemberDetails;

import jakarta.persistence.AccessType;

/**
 * Contract responsible for resolving the members that identify the persistent
 * attributes for a given class descriptor representing a managed type.
 *
 * These members (field or method) would be where we look for mapping annotations
 * for the attribute.
 *
 * Additionally, whether the member is a field or method would tell us the default
 * runtime access strategy
 *
 * @author Steve Ebersole
 */
public interface PersistentAttributeMemberResolver {
	/**
	 * Given the class descriptor representing a ManagedType and the implicit AccessType
	 * to use, resolve the members that indicate persistent attributes.
	 *
	 * @param classDetails Descriptor of the class
	 * @param classLevelAccessType The implicit AccessType
	 * @param processingContext The local context
	 *
	 * @return The list of "backing members"
	 */
	List<MemberDetails> resolveAttributesMembers(
			ClassDetails classDetails,
			AccessType classLevelAccessType,
			LocalAnnotationProcessingContext processingContext);

}
