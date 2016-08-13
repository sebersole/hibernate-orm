/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.io.Serializable;
import java.sql.Blob;

import org.hibernate.type.descriptor.spi.MutabilityPlan;

/**
 * @author Steve Ebersole
 */
public class MutabilityPlanBlobImpl implements MutabilityPlan<Blob> {
	public static final MutabilityPlanBlobImpl INSTANCE = new MutabilityPlanBlobImpl();

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Blob deepCopy(Blob value) {
		return value;
	}

	@Override
	public Serializable disassemble(Blob value) {
		throw new UnsupportedOperationException( "Blobs are not cacheable" );
	}

	@Override
	public Blob assemble(Serializable cached) {
		throw new UnsupportedOperationException( "Blobs are not cacheable" );
	}
}
