/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.dialect;

import org.hibernate.query.sqm.consume.multitable.internal.StandardIdTableSupport;
import org.hibernate.query.sqm.consume.multitable.spi.IdTableStrategy;
import org.hibernate.query.sqm.consume.multitable.spi.idtable.GlobalTempTableExporter;
import org.hibernate.query.sqm.consume.multitable.spi.idtable.GlobalTemporaryTableStrategy;
import org.hibernate.query.sqm.consume.multitable.spi.idtable.IdTable;
import org.hibernate.query.sqm.consume.multitable.spi.idtable.IdTableSupport;
import org.hibernate.tool.schema.spi.Exporter;

/**
 * An SQL dialect for HANA. <br/>
 * <a href="http://help.sap.com/hana/html/sqlmain.html">SAP HANA Reference</a> <br/>
 * Column tables are created by this dialect when using the auto-ddl feature.
 * 
 * @author Andrew Clemons <andrew.clemons@sap.com>
 */
public class HANAColumnStoreDialect extends AbstractHANADialect {

	public HANAColumnStoreDialect() {
		super();
	}

	@Override
	public String getCreateTableString() {
		return "create column table";
	}

	@Override
	public IdTableStrategy getDefaultIdTableStrategy() {
		return new GlobalTemporaryTableStrategy(
				generateIdTableSupport()
		);
	}

	protected IdTableSupport generateIdTableSupport() {
		return new StandardIdTableSupport( new GlobalTempTableExporter() ) {
			@Override
			public Exporter<IdTable> getIdTableExporter() {
				return generateIdTableExporter();
			}

		};
	}

	protected Exporter<IdTable> generateIdTableExporter() {
		return new GlobalTempTableExporter() {
			@Override
			protected String getCreateOptions() {
				return "not logged";
			}

			@Override
			protected String getTruncateIdTableCommand() {
				return "truncate table";
			}

			@Override
			protected String getCreateCommand() {
				return "create global temporary column table";
			}
		};
	}
}
