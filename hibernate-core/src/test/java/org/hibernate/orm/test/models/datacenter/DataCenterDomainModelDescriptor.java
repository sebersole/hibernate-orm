/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.models.datacenter;

import org.hibernate.boot.MetadataSources;

import org.hibernate.testing.orm.domain.DomainModelDescriptor;

/**
 * @author Steve Ebersole
 */
public class DataCenterDomainModelDescriptor implements DomainModelDescriptor {
	@Override
	public void applyDomainModel(MetadataSources sources) {
		sources.addAnnotatedClass( DataCenter.class )
				.addAnnotatedClass( System.class )
				.addAnnotatedClass( DataCenterUserPk.class )
				.addAnnotatedClass( DataCenterUser.class );
	}
}
