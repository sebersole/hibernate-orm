/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.bytecode.enhance.internal.bytebuddy.model;

import java.util.Map;

/**
 * @author Steve Ebersole
 */
public interface ManagedTypeDescriptor {
	ClassDetails getClassDetails();

	Map<String,PersistentAttribute> getPersistentAttributes();

	default PersistentAttribute getPersistentAttribute(String name) {
		return getPersistentAttributes().get( name );
	}
}
