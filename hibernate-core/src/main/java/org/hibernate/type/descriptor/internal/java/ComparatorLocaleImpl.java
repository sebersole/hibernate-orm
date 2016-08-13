/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.util.Comparator;
import java.util.Locale;

/**
 * @author Steve Ebersole
 */
public class ComparatorLocaleImpl implements Comparator<Locale> {
	public static final ComparatorLocaleImpl INSTANCE = new ComparatorLocaleImpl();

	public int compare(Locale o1, Locale o2) {
		return o1.toString().compareTo( o2.toString() );
	}
}
