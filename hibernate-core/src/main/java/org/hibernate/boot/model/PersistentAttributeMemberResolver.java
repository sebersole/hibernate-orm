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
package org.hibernate.boot.model;

import java.util.List;
import javax.persistence.AccessType;

import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;

import org.jboss.jandex.ClassInfo;

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
	 * Given the ManagedType Java type descriptor and the implicit AccessType
	 * to use, resolve the members that indicate persistent attributes.
	 *
	 * @param classInfo Jandex ClassInfo representing the class for which to resolve persistent attribute members
	 * @param classLevelAccessType The AccessType determined for the class default
	 * @param bindingContext The local binding context
	 *
	 * @return The list of "backing members"
	 */
	List<MemberDescriptor> resolveAttributesMembers(
			ClassInfo classInfo,
			AccessType classLevelAccessType,
			EntityBindingContext bindingContext);

}
