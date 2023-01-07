/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.model.source.annotations.spi;

import org.hibernate.type.descriptor.jdbc.JdbcType;

/**
 * Source for {@link org.hibernate.type.descriptor.jdbc.JdbcType}
 *
 * @author Steve Ebersole
 */
public class JdbcTypeSource {
	private Integer typeCode;
	private Class<? extends JdbcType> typeImpl;

	public JdbcTypeSource() {
	}

	public JdbcTypeSource(Integer typeCode) {
		this.typeCode = typeCode;
	}

	public JdbcTypeSource(Class<? extends JdbcType> typeImpl) {
		this.typeImpl = typeImpl;
	}

	public JdbcTypeSource(org.hibernate.annotations.JdbcTypeCode annotation) {
		this( annotation.value() );
	}

	public JdbcTypeSource(org.hibernate.annotations.JdbcType annotation) {
		this( annotation.value() );
	}

	public Integer getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(Integer typeCode) {
		this.typeCode = typeCode;
		this.typeImpl = null;
	}

	public Class<? extends JdbcType> getTypeImpl() {
		return typeImpl;
	}

	public void setTypeImpl(Class<? extends JdbcType> typeImpl) {
		this.typeImpl = typeImpl;
		this.typeCode = null;
	}
}
