/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.internal;

import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.annotations.common.reflection.java.JavaXMember;
import org.hibernate.boot.annotations.internal.AbstractAnnotationTarget;
import org.hibernate.boot.annotations.spi.AnnotationDescriptorRegistry;
import org.hibernate.boot.model.source.annotations.spi.MemberSource;

/**
 * MemberSource based on HCANN's {@link XProperty}.
 *
 * @implNote Actually this depends specifically on HCANN's {@link JavaXMember}
 *
 * @author Steve Ebersole
 */
public class XPropertyMemberSourceImpl extends AbstractAnnotationTarget implements MemberSource {
	private final String name;

	public XPropertyMemberSourceImpl(
			XProperty xProperty,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		this( (JavaXMember) xProperty, annotationDescriptorRegistry );
	}

	public XPropertyMemberSourceImpl(
			JavaXMember xProperty,
			AnnotationDescriptorRegistry annotationDescriptorRegistry) {
		super( xProperty.getAnnotations(), annotationDescriptorRegistry );
		this.name = xProperty.getMember().getName();
	}

	@Override
	public String getName() {
		return name;
	}
}
