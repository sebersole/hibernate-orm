/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.intermediate.access;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;

/**
 * Along with ImplicitMappedSuper, implies FIELD access-type
 *
 * @author Steve Ebersole
 */
@Entity
public class ImplicitMappedSuperRoot extends ImplicitMappedSuper {
	@Basic
	private String name;

	protected ImplicitMappedSuperRoot() {
		// for use by Hibernate
	}

	public ImplicitMappedSuperRoot(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
