/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.intermediate.attribute;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * @author Steve Ebersole
 */
@Entity
@Access( AccessType.PROPERTY )
public class ExplicitAccessOnSetterEntity {
	private Integer id;
	private String name;

	private ExplicitAccessOnSetterEntity() {
		// for Hibernate use
	}

	public ExplicitAccessOnSetterEntity(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	@Id
	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Basic
	public void setName(String name) {
		this.name = name;
	}
}
