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
import jakarta.persistence.Id;

/**
 * Simple entity for testing implicit access-type determination.
 * <p/>
 * Implicit access-type will be FIELD due to placement of `@Id`
 *
 * @author Steve Ebersole
 */
@Entity
@Access( AccessType.FIELD )
public class SimpleExplicitEntity {
	@Id
	private Integer id;
	@Basic
	private String name;

	protected SimpleExplicitEntity() {
		// for use by Hibernate
	}

	public SimpleExplicitEntity(Integer id, String name) {
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
