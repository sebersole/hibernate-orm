/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations.impl;

import java.util.Map;

import org.hibernate.boot.model.source.internal.annotations.HibernateTypeSource;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PluralAttributeElementDetails;

import org.jboss.jandex.ClassInfo;

/**
 * @author Hardy Ferentschik
 * @author Strong Liu
 * @author Steve Ebersole
 */
public class HibernateTypeSourceImpl implements HibernateTypeSource {
	private final String name;
	private final Map<String, String> parameters;
	private JavaTypeDescriptorImpl javaTypeDescriptor;

	public HibernateTypeSourceImpl(PersistentAttribute attribute) {
		this(
				// todo : need to capture Type info on PersistentAttribute
				null,
				null,
				attribute.getContext().getJandexIndex().getClassByName( attribute.getBackingMember().type().name() )
		);
	}

	public HibernateTypeSourceImpl(String name, Map<String, String> parameters, ClassInfo javaTypeDescriptor) {
		this.name = name;
		this.parameters = parameters;
		this.javaTypeDescriptor = new JavaTypeDescriptorImpl( javaTypeDescriptor );
	}

	public HibernateTypeSourceImpl(final PluralAttributeElementDetails element) {
		this(
				null,
				null,
				element.getJavaType()
		);
	}

//	public HibernateTypeSourceImpl() {
//		this( (String) null );
//	}
//
//	public HibernateTypeSourceImpl(String name) {
//		this.name = name;
//		this.parameters = Collections.emptyMap();
//	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public JavaTypeDescriptorImpl getJavaType() {
		return javaTypeDescriptor;
	}
}


