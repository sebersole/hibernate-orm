/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.models.datacenter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Steve Ebersole
 */
@Entity(name = "System")
@Table(name = "systems")
public class System {
	@Id
	private Integer id;
	private String name;

	@ManyToOne
	DataCenterUser dataCenterUser;

	public System() {
	}

	public System(Integer id, String name) {
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DataCenterUser getDataCenterUser() {
		return dataCenterUser;
	}

	public void setDataCenterUser(DataCenterUser dataCenterUser) {
		this.dataCenterUser = dataCenterUser;
	}
}
