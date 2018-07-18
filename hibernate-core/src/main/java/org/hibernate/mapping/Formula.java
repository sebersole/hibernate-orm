/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.mapping;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.model.relational.spi.Column;
import org.hibernate.metamodel.model.relational.spi.DerivedColumn;
import org.hibernate.metamodel.model.relational.spi.PhysicalNamingStrategy;
import org.hibernate.metamodel.model.relational.spi.Table;
import org.hibernate.query.sqm.produce.function.SqmFunctionRegistry;
import org.hibernate.sql.Template;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * A formula is a derived column value
 * @author Gavin King
 */
public class Formula implements Selectable, Serializable {

	private String formula;
	private SqlTypeDescriptor sqlTypeDescriptor;

	public Formula(String formula) {
		this.formula = formula;
	}

	@Override
	public String getTemplate(Dialect dialect, SqmFunctionRegistry functionRegistry) {
		String template = Template.renderWhereStringTemplate(formula, dialect, functionRegistry);
		return StringHelper.replace( template, "{alias}", Template.TEMPLATE );
	}

	@Override
	public String getText(Dialect dialect) {
		return getFormula();
	}

	@Override
	public String getText() {
		return getFormula();
	}

	@Override
	public SqlTypeDescriptor getSqlTypeDescriptor() {
		return sqlTypeDescriptor;
	}

	@Override
	public Column generateRuntimeColumn(
			Table runtimeTable,
			PhysicalNamingStrategy namingStrategy,
			JdbcEnvironment jdbcEnvironment,
			TypeConfiguration typeConfiguration) {
		return new DerivedColumn( runtimeTable, formula, sqlTypeDescriptor );
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String string) {
		formula = string;
	}

	@Override
	public boolean isFormula() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		Formula formula1 = (Formula) o;
		return Objects.equals( formula, formula1.formula );
	}

	@Override
	public int hashCode() {

		return Objects.hash( formula );
	}

	@Override
	public String toString() {
		return this.getClass().getName() + "( " + formula + " )";
	}
}
