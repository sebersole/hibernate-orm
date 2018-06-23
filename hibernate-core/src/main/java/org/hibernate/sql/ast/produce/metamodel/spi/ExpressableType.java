/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.spi;

import org.hibernate.metamodel.model.domain.spi.Navigable;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;

/**
 * Polymorphically represents any "type" which can occur as an expression
 * in an SQM tree.
 * <p/>
 * Generally will be one of:<ul>
 *     <li>a {@link Navigable}</li>
 *     <li>a {@link org.hibernate.type.spi.BasicType}</li>
 * </ul>
 * <p/>
 * Note: cannot be just Navigable as we need to account for functions
 * and other non-domain expressions.
 *
 * @author Steve Ebersole
 */
@Deprecated
public interface ExpressableType<T> extends javax.persistence.metamodel.Type<T> {
	JavaTypeDescriptor<T> getJavaTypeDescriptor();

	// todo (6.0) (domain-jdbc) : another case where the lines between JDBC and domain are wrong
	//		More-or-less this contract represents a type in the domain-sense.  This should *never*
	//		be pushed into the SQL AST as the JDBC-level always deals with simple/basic values.
	// 		This contract is probably needed for *producing* the SQL AST - it just should not
	//		be used *in* that SQL AST
}
