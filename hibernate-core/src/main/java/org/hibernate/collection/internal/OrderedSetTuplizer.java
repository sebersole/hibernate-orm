/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.collection.internal;

import java.util.LinkedHashSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Andrea Boriero
 */
public class OrderedSetTuplizer extends AbstractPersistentCollectionTuplizer<PersistentSet> {
	@Override
	public Object instantiate(int anticipatedSize) {
		return anticipatedSize > 0
				? new LinkedHashSet( anticipatedSize )
				: new LinkedHashSet();
	}

	@Override
	public PersistentSet wrap(
			SharedSessionContractImplementor session, Object rawCollection) {
		return new PersistentSet( session, (java.util.Set) rawCollection );
	}

	@Override
	public Class getCollectionJavaType() {
		return java.util.Set.class;
	}

	@Override
	public Class<PersistentSet> getPersistentCollectionJavaType() {
		return PersistentSet.class;
	}
}
