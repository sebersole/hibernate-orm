/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.io.Serializable;

import org.hibernate.internal.util.SerializationHelper;
import org.hibernate.type.descriptor.spi.MutableMutabilityPlan;

/**
 * @author Steve Ebersole
 */
public class MutabilityPlanSerializableImpl<S extends Serializable> extends MutableMutabilityPlan<S> {
	private final Class<S> type;

	public static final MutabilityPlanSerializableImpl<Serializable> INSTANCE = new MutabilityPlanSerializableImpl<>(
			Serializable.class );

	public MutabilityPlanSerializableImpl(Class<S> type) {
		this.type = type;
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public S deepCopyNotNull(S value) {
		return (S) SerializationHelper.clone( value );
	}

}
