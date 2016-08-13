/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.sql.Time;
import java.util.Date;

import org.hibernate.type.descriptor.spi.MutableMutabilityPlan;

/**
 * @author Steve Ebersole
 */
public class MutabilityPlanTimeImpl extends MutableMutabilityPlan<Date> {
	public static final MutabilityPlanTimeImpl INSTANCE = new MutabilityPlanTimeImpl();

	@Override
	public Date deepCopyNotNull(Date value) {
		return Time.class.isInstance( value )
				? new Time( value.getTime() )
				: new Date( value.getTime() );
	}
}
