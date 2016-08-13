/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.util.Comparator;
import java.util.UUID;

/**
 * @author Steve Ebersole
 */
public class ComparatorUUIDImpl implements Comparator<UUID> {
	public static final ComparatorUUIDImpl INSTANCE = new ComparatorUUIDImpl();

	public int compare(UUID o1, UUID o2) {
		return o1.compareTo( o2 );
	}
}
