/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.sql.Timestamp;
import java.util.Date;

import org.hibernate.type.descriptor.spi.MutableMutabilityPlan;

/**
 * @author Steve Ebersole
 */
public class MutabilityPlanTimestampImpl extends MutableMutabilityPlan<Date> {
	public static final MutabilityPlanTimestampImpl INSTANCE = new MutabilityPlanTimestampImpl();

	@Override
	public Date deepCopyNotNull(Date value) {
		if ( value instanceof Timestamp ) {
			Timestamp orig = (Timestamp) value;
			Timestamp ts = new Timestamp( orig.getTime() );
			ts.setNanos( orig.getNanos() );
			return ts;
		}
		else {
			return new Date( value.getTime() );
		}
	}
}
