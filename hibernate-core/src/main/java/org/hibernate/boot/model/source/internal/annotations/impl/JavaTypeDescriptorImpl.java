/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.impl;

import org.hibernate.boot.model.JavaTypeDescriptor;

import org.jboss.jandex.ClassInfo;

/**
 * @author Steve Ebersole
 */
public class JavaTypeDescriptorImpl implements JavaTypeDescriptor {
	private final ClassInfo classInfo;

	public JavaTypeDescriptorImpl(ClassInfo classInfo) {
		this.classInfo = classInfo;
	}

	@Override
	public String getName() {
		return classInfo == null ? null : classInfo.name().toString();
	}
}
