/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.internal.sql;

import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.type.descriptor.spi.WrapperOptions;
import org.hibernate.type.descriptor.spi.sql.SqlTypeDescriptor;

/**
 * @author Steve Ebersole
 */
public class WrapperOptionsSupport implements WrapperOptions {
	/**
	 * Singleton access
	 */
	public static final WrapperOptionsSupport INSTANCE = new WrapperOptionsSupport();

	@Override
	public boolean useStreamForLobBinding() {
		return false;
	}

	@Override
	public LobCreator getLobCreator() {
		throw new IllegalStateException( "Not expecting LobCreator access while rendering JDBC literal" );
	}

	@Override
	public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
		return sqlTypeDescriptor;
	}
}
