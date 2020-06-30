/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.metamodel.mapping.internal.fk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.mapping.Selectable;
import org.hibernate.metamodel.mapping.BasicSingularAttribute;
import org.hibernate.metamodel.mapping.EmbeddedAttributeMapping;
import org.hibernate.metamodel.mapping.JdbcMapping;
import org.hibernate.metamodel.mapping.ManagedMappingType;
import org.hibernate.metamodel.mapping.SingularAttributeMapping;
import org.hibernate.metamodel.mapping.ToOneAttributeMapping;
import org.hibernate.metamodel.mapping.internal.MappingModelCreationProcess;
import org.hibernate.metamodel.model.domain.NavigableRole;
import org.hibernate.property.access.spi.PropertyAccess;

/**
 * @author Steve Ebersole
 */
public class ForeignKeyHelper {

	/**
	 * Used to make a "shallow" copy of an attribute used as part of a composite key, with the new
	 * attribute:
	 *
	 * 		1) mapped to the given table and columns rather than table and columns from `original`
	 * 		2) the created attribute is a "shell" only capable of creating "shallow" fetches
	 * 		3) the created attribute matches type of the `original` in terms of basic, embeddable, entity
	 */
	public static SingularAttributeMapping makeKeyCopy(
			SingularAttributeMapping original,
			String tableName,
			Iterator<Selectable> columnItr,
			ManagedMappingType declaringType,
			MappingModelCreationProcess creationProcess) {
		final SessionFactoryImplementor sessionFactory = creationProcess.getCreationContext().getSessionFactory();
		final Dialect dialect = sessionFactory.getDialect();

//		final NavigableRole copyRole = original.getNavigableRole().append( "{virtual-copy}" );
		final NavigableRole copyRole = original.getNavigableRole();

		if ( original instanceof BasicSingularAttribute ) {
			final BasicSingularAttribute originalBasic = (BasicSingularAttribute) original;
			final String columnName = columnItr.next().getText( dialect );

			FkDescriptorCreationLogger.LOGGER.debugf(
					"Building VirtualBasicAttribute corresponding to `%s` - %s.%s",
					originalBasic.getNavigableRole().getFullPath(),
					tableName,
					columnName
			);

			return new VirtualBasicAttribute(
					copyRole,
					original.getAttributeMetadataAccess().resolveAttributeMetadata( null ).isNullable(),
					original.getMappedFetchOptions(),
					null,
					tableName,
					columnName,
					originalBasic.getJdbcMapping(),
					declaringType,
					original.getStateArrayPosition(),
					createPropertyAccessCopy( original ),
					creationProcess
			);
		}
		else if ( original instanceof EmbeddedAttributeMapping ) {
			final EmbeddedAttributeMapping originalEmbedded = (EmbeddedAttributeMapping) original;

			return new VirtualEmbeddedAttribute(
					copyRole,
					originalEmbedded,
					declaringType,
					tableName,
					columnItr,
					creationProcess
			);
		}
		else if ( original instanceof ToOneAttributeMapping ) {
			final ToOneAttributeMapping originalToOne = (ToOneAttributeMapping) original;

			final int jdbcTypeCount = originalToOne.getJdbcTypeCount( sessionFactory.getTypeConfiguration() );
			final List<String> columnNames = new ArrayList<>( jdbcTypeCount );
			final List<JdbcMapping> jdbcMappings = new ArrayList<>( jdbcTypeCount );

			originalToOne.visitColumns(
					(containingTableExpression, columnExpression, jdbcMapping) -> {
						assert columnItr.hasNext();

						final Selectable selectable = columnItr.next();

						columnNames.add( selectable.getText( dialect ) );
						jdbcMappings.add( jdbcMapping );
					}
			);

			return new VirtualToOneAttribute(
					copyRole,
					// todo (6.0) : not sure this is correct - I *think* the virtual mapping would always be the target side
					//		which should always be a logical/physical one-to-one
					originalToOne.getCardinality(),
					originalToOne.getMappedFetchOptions(),
					createPropertyAccessCopy( originalToOne ),
					declaringType,
					original.getStateArrayPosition(),
					originalToOne.getAssociatedEntityMappingType(),
					tableName,
					columnNames,
					jdbcMappings,
					originalToOne.getAttributeMetadataAccess().resolveAttributeMetadata( null ).isNullable(),
					originalToOne.getKeyModelPart(),
					creationProcess
			);
		}

		throw new UnsupportedOperationException();
	}

	private static PropertyAccess createPropertyAccessCopy(SingularAttributeMapping original) {
		return original.getPropertyAccess().getPropertyAccessStrategy().buildPropertyAccess(
				original.getDeclaringType().getJavaTypeDescriptor().getJavaType(),
				original.getAttributeName()
		);
	}
}
