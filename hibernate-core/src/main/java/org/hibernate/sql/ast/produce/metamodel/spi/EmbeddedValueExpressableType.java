/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.spi;

import javax.persistence.TemporalType;

import org.hibernate.metamodel.model.domain.spi.EmbeddedTypeDescriptor;
import org.hibernate.query.sqm.AllowableParameterType;
import org.hibernate.sql.JdbcValueBinder;
import org.hibernate.sql.JdbcValueExtractor;
import org.hibernate.type.descriptor.java.spi.JavaTypeDescriptor;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * @author Steve Ebersole
 */
public interface EmbeddedValueExpressableType<T> extends ExpressableType<T>, AllowableParameterType<T> {
	EmbeddedTypeDescriptor getEmbeddedDescriptor();

	default int getNumberOfJdbcParametersForRestriction() {
		return getNumberOfJdbcParametersToBind();
	}

	@Override
	default int getNumberOfJdbcParametersToBind() {
		return getEmbeddedDescriptor().getNumberOfJdbcParametersToBind();
	}

	@Override
	default JdbcValueBinder getValueBinder() {
		return getEmbeddedDescriptor().getValueBinder();
	}

	@Override
	default JdbcValueExtractor getValueExtractor() {
		return getEmbeddedDescriptor().getValueExtractor();
	}

	@Override
	default AllowableParameterType resolveTemporalPrecision(TemporalType temporalType, TypeConfiguration typeConfiguration) {
		return getEmbeddedDescriptor().resolveTemporalPrecision( temporalType, typeConfiguration );
	}

	@Override
	default JavaTypeDescriptor<T> getJavaTypeDescriptor() {
		return getEmbeddedDescriptor().getJavaTypeDescriptor();
	}

}
