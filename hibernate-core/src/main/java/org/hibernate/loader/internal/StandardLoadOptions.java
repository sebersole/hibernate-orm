/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.loader.internal;

import org.hibernate.LockOptions;
import org.hibernate.loader.spi.SingleIdEntityLoader;

/**
 * @author Steve Ebersole
 */
public class StandardLoadOptions implements SingleIdEntityLoader.LoadOptions {
	private final LockOptions lockOptions;
	private final Object instanceToLoad;

	public StandardLoadOptions(LockOptions lockOptions, Object instanceToLoad) {
		this.lockOptions = lockOptions;
		this.instanceToLoad = instanceToLoad;
	}

	public StandardLoadOptions(LockOptions lockOptions) {
		this( lockOptions, null );
	}

	public StandardLoadOptions() {
		this( LockOptions.NONE );
	}

	@Override
	public LockOptions getLockOptions() {
		return null;
	}

	@Override
	public Object getInstanceToLoad() {
		return null;
	}
}
