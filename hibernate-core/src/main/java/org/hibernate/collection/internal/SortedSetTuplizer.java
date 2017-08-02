/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.collection.internal;

import java.util.Comparator;
import java.util.TreeSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Andrea Boriero
 */
public class SortedSetTuplizer extends AbstractPersistentCollectionTuplizer<PersistentSortedSet> {
	private final Comparator comparator;

	public SortedSetTuplizer(Comparator comparator) {
		this.comparator = comparator;
	}

	@Override
	public Object instantiate(int anticipatedSize) {
		return new TreeSet( comparator );
	}

	@Override
	public PersistentSortedSet wrap(
			SharedSessionContractImplementor session, Object rawCollection) {
		return new PersistentSortedSet( session, (java.util.SortedSet) rawCollection );
	}

	@Override
	public Class<PersistentSortedSet> getPersistentCollectionJavaType() {
		return PersistentSortedSet.class;
	}

	@Override
	public Class getCollectionJavaType() {
		return java.util.SortedSet.class;
	}
}
