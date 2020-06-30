/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.models.datacenter;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 *
 * data_center( id, ... )
 * data_center_user( data_center_id, username, .. )
 * system( id, user_data_center_id, user_user_name... )
 *
 * data_center_user -> DataCenterUserPk( data_center_id, username )
 * system -> ???( data_center_id, username )
 *
 * @author Steve Ebersole
 */
@Embeddable
public class DataCenterUserPk implements Serializable {
	@ManyToOne
	private DataCenter dataCenter;
	private String username;

	public DataCenterUserPk(DataCenter dataCenter, String username) {
		this.setDataCenter( dataCenter );
		this.setUsername( username );
	}

	private DataCenterUserPk() {
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		DataCenterUserPk pk = (DataCenterUserPk) o;
		return Objects.equals( getDataCenter(), pk.getDataCenter() ) &&
				Objects.equals( getUsername(), pk.getUsername() );
	}

	@Override
	public int hashCode() {
		return Objects.hash( getDataCenter(), getUsername() );
	}

	public DataCenter getDataCenter() {
		return dataCenter;
	}

	public void setDataCenter(DataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
