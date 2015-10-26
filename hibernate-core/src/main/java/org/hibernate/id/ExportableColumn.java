/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.id;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.mapping.Column;

/**
 * @author Steve Ebersole
 */
public class ExportableColumn extends Column {
	public ExportableColumn(
			Identifier logicalName,
			Identifier physicalName,
			String dbTypeDeclaration) {
		super();
		setLogicalName( logicalName );
		setPhysicalName( physicalName );
		setSqlType( dbTypeDeclaration );
	}
}
