/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.intermediate.access;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * MappedSuperclass expressing explicit access-type
 *
 * @author Steve Ebersole
 */
@MappedSuperclass
@Access( AccessType.FIELD )
public class ExplicitMappedSuper {
	@Id
	protected Integer id;

	protected ExplicitMappedSuper() {
		// for use by Hibernate
	}

	public ExplicitMappedSuper(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}
}
