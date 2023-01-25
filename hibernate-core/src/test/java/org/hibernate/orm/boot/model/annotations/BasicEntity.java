/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.boot.model.annotations;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Very simple entity, with lots of implicit mappings to test
 * resolution of those implicit values
 *
 * @author Steve Ebersole
 */
@Entity
public class BasicEntity {
	@Id
	private Integer id;
	private String name;
}
