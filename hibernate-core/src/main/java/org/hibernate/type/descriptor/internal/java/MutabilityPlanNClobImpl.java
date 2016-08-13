/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.io.Serializable;
import java.sql.NClob;

import org.hibernate.type.descriptor.spi.MutabilityPlan;

/**
 * @author Steve Ebersole
 */
public class MutabilityPlanNClobImpl implements MutabilityPlan<NClob> {
	public static final MutabilityPlanNClobImpl INSTANCE = new MutabilityPlanNClobImpl();

	public boolean isMutable() {
		return false;
	}

	public NClob deepCopy(NClob value) {
		return value;
	}

	public Serializable disassemble(NClob value) {
		throw new UnsupportedOperationException( "LOB locators are not cacheable" );
	}

	public NClob assemble(Serializable cached) {
		throw new UnsupportedOperationException( "LOB locators are not cacheable" );
	}
}
