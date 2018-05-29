/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.ast.produce.metamodel.internal;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.model.domain.spi.EntityDescriptor;

/**
 * @author Steve Ebersole
 */
public class SelectByEntityIdentifierBuilder extends AbstractMetamodelSelectBuilder{
	public SelectByEntityIdentifierBuilder(
			SessionFactoryImplementor sessionFactory,
			EntityDescriptor entityDescriptor) {
		super( sessionFactory, entityDescriptor, entityDescriptor.getHierarchy().getIdentifierDescriptor() );
	}
}
