/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.orm.test.boot.annotations.intermediate.access;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * MappedSuperclass expressing implicit access-type
 * <p/>
 * Implicit access-type will be FIELD due to placement of `@Id`
 *
 * @author Steve Ebersole
 */
@MappedSuperclass
public class ImplicitMappedSuper {
	@Id
	protected Integer id;
}
