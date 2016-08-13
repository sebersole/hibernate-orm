/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.util.Date;

import org.hibernate.type.descriptor.spi.MutableMutabilityPlan;

/**
 * @author Steve Ebersole
 */
public class MutabilityPlanDateImpl extends MutableMutabilityPlan<Date> {
	public static final MutabilityPlanDateImpl INSTANCE = new MutabilityPlanDateImpl();

	@Override
	public Date deepCopyNotNull(Date value) {
		return java.sql.Date.class.isInstance( value )
				? new java.sql.Date( value.getTime() )
				: new Date( value.getTime() );
	}
}
