/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.model.convert.internal;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;

/**
 * A no-op /  pass-through conversion
 *
 * @author Steve Ebersole
 */
public class StandardBasicValueConverter<J> implements BasicValueConverter<J,J> {
	private final BasicJavaDescriptor<J> javaDescriptor;

	private StandardBasicValueConverter(BasicJavaDescriptor<J> javaDescriptor) {
		this.javaDescriptor = javaDescriptor;
	}

	@Override
	@SuppressWarnings("unchecked")
	public J toDomainValue(J relationalForm, SharedSessionContractImplementor session) {
		return relationalForm;
	}

	@Override
	@SuppressWarnings("unchecked")
	public J toRelationalValue(J domainForm, SharedSessionContractImplementor session) {
		return domainForm;
	}

	@Override
	public BasicJavaDescriptor<J> getDomainJavaDescriptor() {
		return javaDescriptor;
	}

	@Override
	public BasicJavaDescriptor<J> getRelationalJavaDescriptor() {
		return javaDescriptor;
	}
}
