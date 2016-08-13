/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.java;

import java.util.Calendar;

import org.hibernate.type.descriptor.spi.MutableMutabilityPlan;

/**
 * @author Steve Ebersole
 */
public class MutabilityPlanCalendarImpl extends MutableMutabilityPlan<Calendar> {
	public static final MutabilityPlanCalendarImpl INSTANCE = new MutabilityPlanCalendarImpl();

	public Calendar deepCopyNotNull(Calendar value) {
		return (Calendar) value.clone();
	}
}
