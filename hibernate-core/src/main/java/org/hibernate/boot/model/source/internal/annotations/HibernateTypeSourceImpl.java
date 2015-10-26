/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.Map;

import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PersistentAttribute;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PluralAttributeElementDetails;
import org.hibernate.boot.model.source.spi.HibernateTypeSource;

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


