/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.spi;

import java.sql.SQLException;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.named.spi.RowReaderMemento;

/**
 * @author Steve Ebersole
 */
public interface RowReader<R> {
	Class<R> getResultJavaType();

	int getNumberOfResults();

	/**
	 * todo (6.0) : JdbcValuesSourceProcessingOptions is available through RowProcessingState - why pass it in separately
	 * 		should use one approach or the other
	 */
	R readRow(RowProcessingState processingState, JdbcValuesSourceProcessingOptions options) throws SQLException;

	void finishUp(JdbcValuesSourceProcessingState context);

	RowReaderMemento toMemento(SessionFactoryImplementor factory);
}
