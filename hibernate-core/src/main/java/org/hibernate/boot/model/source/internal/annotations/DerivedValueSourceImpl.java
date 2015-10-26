/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.FormulaValue;
import org.hibernate.boot.model.source.spi.DerivedValueSource;

/**
 * @author Strong Liu
 * @author Steve Ebersole
 */
public class DerivedValueSourceImpl implements DerivedValueSource {
	private final String expression;
	private final String containingTableName;

    public DerivedValueSourceImpl(FormulaValue formulaValue) {
		this.expression = formulaValue.getExpression();
		this.containingTableName = formulaValue.getContainingTableName();
    }

	public DerivedValueSourceImpl(String expression, String containingTableName) {
		this.expression = expression;
		this.containingTableName = containingTableName;
	}

	@Override
	public Nature getNature() {
		return Nature.DERIVED;
	}

	@Override
    public String getExpression() {
        return expression;
    }

    @Override
    public String getContainingTableName() {
        return containingTableName;
    }
}
