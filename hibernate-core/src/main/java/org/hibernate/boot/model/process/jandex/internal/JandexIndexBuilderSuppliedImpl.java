/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.jandex.internal;

import org.hibernate.boot.model.process.jandex.spi.JandexIndexBuilder;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

/**
 * JandexIndexBuilder implementation used when a Jandex index was supplied to us.
 *
 * @author Steve Ebersole
 */
public class JandexIndexBuilderSuppliedImpl implements JandexIndexBuilder {
	private final IndexView suppliedJandexView;

	public JandexIndexBuilderSuppliedImpl(IndexView suppliedJandexView) {
		this.suppliedJandexView = suppliedJandexView;
	}

	@Override
	public ClassInfo indexPackage(String packageName) {
		return suppliedJandexView.getClassByName( DotName.createSimple( packageName ) );
	}

	@Override
	public ClassInfo indexClass(String className) {
		return suppliedJandexView.getClassByName( DotName.createSimple( className ) );
	}

	@Override
	public ClassInfo indexClass(Class classReference) {
		return suppliedJandexView.getClassByName( DotName.createSimple( classReference.getName() ) );
	}

	@Override
	public IndexView buildIndexView() {
		return suppliedJandexView;
	}
}
