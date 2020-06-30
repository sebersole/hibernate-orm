/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.test.metamodel.mapping.fk.toone;

import java.util.Iterator;

import org.hibernate.metamodel.mapping.AttributeMapping;
import org.hibernate.metamodel.mapping.BasicEntityIdentifierMapping;
import org.hibernate.metamodel.mapping.EntityIdentifierMapping;
import org.hibernate.metamodel.mapping.EntityMappingType;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.EmbeddedIdentifierMappingImpl;
import org.hibernate.metamodel.mapping.internal.fk.ForeignKey;
import org.hibernate.metamodel.mapping.internal.fk.Side;
import org.hibernate.orm.test.models.datacenter.System;
import org.hibernate.orm.test.models.datacenter.DataCenter;
import org.hibernate.orm.test.models.datacenter.DataCenterDomainModelDescriptor;
import org.hibernate.orm.test.models.datacenter.DataCenterUser;

import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for unidirectional many-to-one mappings used as part of a composite key
 *
 * @author Steve Ebersole
 */
@DomainModel( modelDescriptorClasses = DataCenterDomainModelDescriptor.class )
@SessionFactory( generateStatistics = true )
public class UnidirectionalKeyManyToOneTests {

	@Test
	void testAggregatedCompositeKeyManyToOneMapping(SessionFactoryScope scope) {
		final EntityMappingType dataCenterUserMapping = scope.getSessionFactory()
				.getRuntimeMetamodels()
				.getEntityMappingType( DataCenterUser.class );

		final EmbeddedIdentifierMappingImpl dataCenterUserId = (EmbeddedIdentifierMappingImpl) dataCenterUserMapping.getIdentifierMapping();
		final Iterator<SingularAttributeMapping> attrItr = dataCenterUserId.getAttributes().iterator();
		final AttributeMapping domainCenterAttr = attrItr.next();
		final AttributeMapping usernameAttr = attrItr.next();
		assertThat( attrItr.hasNext(), is( false ) );

		assertThat( domainCenterAttr, instanceOf( ToOneAttributeMapping.class ) );
		final ToOneAttributeMapping toOneMapping = (ToOneAttributeMapping) domainCenterAttr;

		final ForeignKey dataCenterForeignKeyDescriptor = toOneMapping.getKeyModelPart().getForeignKeyDescriptor();
		assertThat( dataCenterForeignKeyDescriptor, notNullValue() );

		// referring side ought to be the to-one
		final Side referringSide = dataCenterForeignKeyDescriptor.getReferringSide();
		assertThat( referringSide.getKeyPart(), is( toOneMapping.getKeyModelPart() ) );

		final EntityMappingType dataCenterMapping = toOneMapping.getAssociatedEntityMappingType();
		assertThat( dataCenterMapping.getMappedJavaTypeDescriptor().getJavaType(), sameInstance( DataCenter.class ) );

		final EntityIdentifierMapping dataCenterId = dataCenterMapping.getIdentifierMapping();

		final Side targetSide = dataCenterForeignKeyDescriptor.getTargetSide();
		assertThat( dataCenterId, instanceOf( BasicEntityIdentifierMapping.class ) );
		assertThat( targetSide.getKeyPart(), is( dataCenterId ) );
	}

	@Test
	void testAggregatedCompositeKeyManyToOneMapping2(SessionFactoryScope scope) {
		final EntityMappingType systemMapping = scope.getSessionFactory()
				.getRuntimeMetamodels()
				.getEntityMappingType( System.class );

		final ToOneAttributeMapping dataCenterUserAttr = (ToOneAttributeMapping) systemMapping.findAttributeMapping( "dataCenterUser" );
		final ForeignKey dataCenterUserFk = dataCenterUserAttr.getForeignKeyDescriptor();


//		final EmbeddedIdentifierMappingImpl dataCenterUserId = (EmbeddedIdentifierMappingImpl) dataCenterUserMapping.getIdentifierMapping();
//		final Iterator<SingularAttributeMapping> attrItr = dataCenterUserId.getAttributes().iterator();
//		final AttributeMapping domainCenterAttr = attrItr.next();
//		final AttributeMapping usernameAttr = attrItr.next();
//		assertThat( attrItr.hasNext(), is( false ) );
//
//		assertThat( domainCenterAttr, instanceOf( ToOneAttributeMapping.class ) );
//		final ToOneAttributeMapping toOneMapping = (ToOneAttributeMapping) domainCenterAttr;
//
//		final ForeignKey dataCenterForeignKeyDescriptor = toOneMapping.getKeyModelPart().getForeignKeyDescriptor();
//		assertThat( dataCenterForeignKeyDescriptor, notNullValue() );
//
//		// referring side ought to be the to-one
//		final Side referringSide = dataCenterForeignKeyDescriptor.getReferringSide();
//		assertThat( referringSide.getKeyPart(), is( toOneMapping.getKeyModelPart() ) );
//
//		final EntityMappingType dataCenterMapping = toOneMapping.getAssociatedEntityMappingType();
//		assertThat( dataCenterMapping.getMappedJavaTypeDescriptor().getJavaType(), sameInstance( DataCenter.class ) );
//
//		final EntityIdentifierMapping dataCenterId = dataCenterMapping.getIdentifierMapping();
//
//		final Side targetSide = dataCenterForeignKeyDescriptor.getTargetSide();
//		assertThat( dataCenterId, instanceOf( BasicEntityIdentifierMapping.class ) );
//		assertThat( targetSide.getKeyPart(), is( dataCenterId ) );
	}
}
