/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.source.spi.DerivedValueSource;

/**
 * @author Strong Liu
 * @author Steve Ebersole
 */
public class FormulaValue implements DerivedValueSource {
    private final String tableName;
    private final String expression;

    public FormulaValue(String tableName, String expression) {
        this.tableName = tableName;
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public String getContainingTableName() {
        return tableName;
    }

    @Override
    public Nature getNature() {
        return Nature.DERIVED;
    }
}
