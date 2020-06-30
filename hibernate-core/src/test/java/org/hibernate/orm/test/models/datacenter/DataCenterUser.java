/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.models.datacenter;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author Steve Ebersole
 */
@Entity(name = "DataCenterUser")
@Table(name = "data_center_user")
public class DataCenterUser {

	@EmbeddedId
	private DataCenterUserPk pk;

	private byte privilegeMask;

	public DataCenterUser() {
	}

	public DataCenterUser(DataCenter dataCenter, String username, byte privilegeMask) {
		this( new DataCenterUserPk( dataCenter, username ), privilegeMask );
	}

	public DataCenterUser(DataCenterUserPk pk, byte privilegeMask) {
		this.pk = pk;
		this.privilegeMask = privilegeMask;
	}

	public DataCenterUserPk getPk() {
		return pk;
	}

	public void setPk(DataCenterUserPk pk) {
		this.pk = pk;
	}
}
