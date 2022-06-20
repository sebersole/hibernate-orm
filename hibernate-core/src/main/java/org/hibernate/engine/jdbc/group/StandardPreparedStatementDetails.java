/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.engine.jdbc.group;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.jdbc.Expectation;
import org.hibernate.sql.group.TableMutation;

/**
 * Describes a particular PreparedStatement within a {@linkplain StandardPreparedStatementGroup group}
 *
 * @author Steve Ebersole
 */
public class StandardPreparedStatementDetails implements PreparedStatementDetails {
	private final TableMutation tableMutation;
	private final PreparedStatement statement;
	private final Expectation expectation;
	private final int baseOffset;

	public StandardPreparedStatementDetails(
			TableMutation tableMutation,
			PreparedStatement statement,
			Expectation expectation) throws SQLException {
		this.tableMutation = tableMutation;
		this.statement = statement;
		this.expectation = expectation;
		this.baseOffset = expectation.prepare( statement );
	}

	@Override
	public TableMutation getTableMutation() {
		return tableMutation;
	}

	@Override
	public PreparedStatement getStatement() {
		return statement;
	}

	@Override
	public Expectation getExpectation() {
		return expectation;
	}

	@Override
	public int getBaseOffset() {
		return baseOffset;
	}
}
