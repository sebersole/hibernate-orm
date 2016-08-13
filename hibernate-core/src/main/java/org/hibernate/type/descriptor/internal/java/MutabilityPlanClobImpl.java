/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.io.Serializable;
import java.sql.Clob;

import org.hibernate.type.descriptor.spi.MutabilityPlan;

/**
 * MutabilityPlan
 * @author Steve Ebersole
 */
public class MutabilityPlanClobImpl implements MutabilityPlan<Clob> {
	public static final MutabilityPlanClobImpl INSTANCE = new MutabilityPlanClobImpl();

	public boolean isMutable() {
		return false;
	}

	public Clob deepCopy(Clob value) {
		return value;
	}

	public Serializable disassemble(Clob value) {
		throw new UnsupportedOperationException( "Clobs are not cacheable" );
	}

	public Clob assemble(Serializable cached) {
		throw new UnsupportedOperationException( "Clobs are not cacheable" );
	}
}
