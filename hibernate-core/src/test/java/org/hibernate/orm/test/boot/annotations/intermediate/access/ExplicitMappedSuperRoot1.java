/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.intermediate.access;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;

/**
 * @author Steve Ebersole
 */
@Entity
@Access( AccessType.PROPERTY )
public class ExplicitMappedSuperRoot1 extends ExplicitMappedSuper {
	private String name;

	public ExplicitMappedSuperRoot1(Integer id, String name) {
		super( id );
		this.name = name;
	}

	@Basic
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
