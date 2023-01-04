/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.jandex.internal;

import org.hibernate.boot.jandex.spi.JandexIndexBuilder;

import org.jboss.jandex.IndexView;

/**
 * JandexIndexBuilder implementation used when a Jandex index was supplied to us.
 *
 * @author Steve Ebersole
 */
public class JandexIndexBuilderSupplied implements JandexIndexBuilder {
	private final IndexView suppliedJandexView;

	public JandexIndexBuilderSupplied(IndexView suppliedJandexView) {
		this.suppliedJandexView = suppliedJandexView;
	}

	@Override
	public void indexPackage(String packageName) {
	}

	@Override
	public void indexClass(String className) {
	}

	@Override
	public void indexClass(Class<?> classReference) {
	}

	@Override
	public IndexView buildIndex() {
		return suppliedJandexView;
	}
}
