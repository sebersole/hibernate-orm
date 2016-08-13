/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.util.Comparator;
import java.util.TimeZone;

/**
 * @author Steve Ebersole
 */
public class ComparatorTimeZoneImpl implements Comparator<TimeZone> {
	public static final ComparatorTimeZoneImpl INSTANCE = new ComparatorTimeZoneImpl();

	public int compare(TimeZone o1, TimeZone o2) {
		return o1.getID().compareTo( o2.getID() );
	}
}
