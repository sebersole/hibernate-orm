/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.hibernate.boot.model.Caching;
import org.hibernate.boot.model.CustomSql;
import org.hibernate.boot.model.source.spi.CollectionNature;
import org.hibernate.boot.model.source.spi.FetchCharacteristicsPluralAttribute;
import org.hibernate.boot.model.source.spi.FetchableAttributeSource;

/**
 * @author Steve Ebersole
 */
public interface PluralAttributeSource
		extends AttributeSource, FetchableAttributeSource, CascadeStyleSource {

	CollectionNature getNature();

	CollectionIdSource getCollectionIdSource();

	PluralAttributeForeignKeySource getForeignKeySource();

	PluralAttributeElementSource getElementSource();

	FilterSource[] getFilterSources();

	TableSpecificationSource getCollectionTableSpecificationSource();

	String getCollectionTableComment();

	String getCollectionTableCheck();

	/**
	 * Obtain any additional table names on which to synchronize (auto flushing) this entity.
	 *
	 * @return Additional synchronized table names or 0 sized String array, never return null.
	 */
	String[] getSynchronizedTableNames();

	Caching getCaching();

	String getCustomPersisterClassName();

	String getWhere();

	boolean isInverse();

	boolean isMutable();

	String getCustomLoaderName();

	CustomSql getCustomSqlInsert();

	CustomSql getCustomSqlUpdate();

	CustomSql getCustomSqlDelete();

	CustomSql getCustomSqlDeleteAll();

	String getMappedBy();

	boolean usesJoinTable();

	@Override
	FetchCharacteristicsPluralAttribute getFetchCharacteristics();
}
