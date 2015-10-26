/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import java.util.List;

import org.hibernate.boot.model.source.internal.annotations.ConvertConversionInfo;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;

/**
 * Represents a singular persistent attribute.
 *
 * @author Steve Ebersole
 */
public interface SingularAttribute extends PersistentAttribute {
	boolean isId();

	boolean isVersion();

	NaturalIdMutability getNaturalIdMutability();

	ConvertConversionInfo getConversionInfo();

	FormulaValue getFormulaValue();

	List<Column> getColumnValues();
}
