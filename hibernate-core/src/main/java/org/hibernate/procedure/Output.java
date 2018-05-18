/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.procedure;

import org.hibernate.result.ResultSetOutput;
import org.hibernate.result.UpdateCountOutput;

/**
 * Common contract for the individual outputs from a JDBC CallableStatement.  Specific types of
 * Output objects include:<ul>
 *
 * </ul>
 *
 * Common contract for individual return objects which can be either results ({@link ResultSetOutput}) or update
 * counts ({@link UpdateCountOutput}).
 *
 * @author Steve Ebersole
 */
public interface Output extends org.hibernate.result.Output {
}
