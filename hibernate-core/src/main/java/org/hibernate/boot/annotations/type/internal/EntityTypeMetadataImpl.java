/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.type.internal;

import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.ManagedClass;
import org.hibernate.boot.annotations.spi.AnnotationBindingContext;
import org.hibernate.boot.annotations.type.spi.EntityTypeMetadata;

import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;

/**
 * @author Steve Ebersole
 */
public class EntityTypeMetadataImpl
		extends AbstractIdentifiableTypeMetadata
		implements EntityTypeMetadata {
	private final String entityName;
	private final String explicitJpaEntityName;

//	private final String proxy;
//
//	private final String customLoaderQueryName;
//	private final String[] synchronizedTableNames;
//	private final int batchSize;
//	private final boolean isDynamicInsert;
//	private final boolean isDynamicUpdate;
//	private final boolean isSelectBeforeUpdate;
//	private final CustomSql customInsert;
//	private final CustomSql customUpdate;
//	private final CustomSql customDelete;
//	private final String discriminatorMatchValue;
//	private final boolean isLazy;

	/**
	 * This form is intended for construction of root Entity.
	 */
	public EntityTypeMetadataImpl(
			ManagedClass managedClass,
			AccessType defaultAccessType,
			AnnotationBindingContext bindingContext) {
		super( managedClass, defaultAccessType, true, bindingContext );

		final AnnotationUsage<Entity> entityAnnotation = managedClass.getAnnotation( JpaAnnotations.ENTITY );
		this.entityName = managedClass.getName();
		this.explicitJpaEntityName = determineExplicitJpaEntityName( entityAnnotation );

//		this.synchronizedTableNames = determineSynchronizedTableNames();
//		this.batchSize = determineBatchSize();
//
//		this.customLoaderQueryName = determineCustomLoader();
//		this.customInsert = extractCustomSql( managedClass.getAnnotation( HibernateAnnotations.SQL_INSERT ) );
//		this.customUpdate = extractCustomSql( managedClass.getAnnotation( HibernateAnnotations.SQL_UPDATE ) );
//		this.customDelete = extractCustomSql( managedClass.getAnnotation( HibernateAnnotations.SQL_DELETE ) );
//
//		this.isDynamicInsert = decodeDynamicInsert();
//		this.isDynamicUpdate = decodeDynamicUpdate();
//		this.isSelectBeforeUpdate = decodeSelectBeforeUpdate();
//
//		// Proxy generation
//		final AnnotationUsage<Proxy> proxyAnnotation = managedClass.getAnnotation( HibernateAnnotations.PROXY );
//		if ( proxyAnnotation != null ) {
//			final AnnotationUsage.AttributeValue lazyValue = proxyAnnotation.getAttributeValue( "lazy" );
//			if ( lazyValue != null ) {
//				this.isLazy = lazyValue.asBoolean();
//			}
//			else {
//				this.isLazy = true;
//			}
//
//			if ( this.isLazy ) {
//				final AnnotationUsage.AttributeValue proxyClassValue = proxyAnnotation.getAttributeValue( "proxyClass" );
//				if ( proxyClassValue != null && !proxyClassValue.isDefaultValue() ) {
//					this.proxy = proxyClassValue.asString();
//				}
//				else {
//					this.proxy = null;
//				}
//			}
//			else {
//				this.proxy = null;
//			}
//		}
//		else {
//			// defaults are that it is lazy and that the class itself is the proxy class
//			this.isLazy = true;
//			this.proxy = getName();
//		}
//
//		final AnnotationUsage<DiscriminatorValue> discriminatorValueAnnotation = managedClass.getAnnotation( JpaAnnotations.DISCRIMINATOR_VALUE );
//		if ( discriminatorValueAnnotation != null ) {
//			final AnnotationUsage.AttributeValue discriminatorValueValue = discriminatorValueAnnotation.getValueAttributeValue();
//			this.discriminatorMatchValue = discriminatorValueValue.asString();
//		}
//		else {
//			this.discriminatorMatchValue = null;
//		}
	}

	/**
	 * This form is intended for construction of non-root Entity.
	 */
	public EntityTypeMetadataImpl(
			ManagedClass managedClass,
			AbstractIdentifiableTypeMetadata superType,
			AccessType defaultAccessType,
			AnnotationBindingContext bindingContext) {
		super( managedClass, superType, defaultAccessType, bindingContext );

		final AnnotationUsage<Entity> entityAnnotation = managedClass.getAnnotation( JpaAnnotations.ENTITY );
		this.entityName = getManagedClass().getName();
		this.explicitJpaEntityName = determineExplicitJpaEntityName( entityAnnotation );
	}

	private String determineExplicitJpaEntityName(AnnotationUsage<Entity> entityAnnotation) {
		final AnnotationUsage.AttributeValue nameValue = entityAnnotation.getAttributeValue( "name" );
		if ( nameValue != null && !nameValue.isDefaultValue() ) {
			return nameValue.asString();
		}
		return null;
	}

//	private String determineCustomLoader() {
//		final AnnotationUsage<Loader> loaderAnnotation = getManagedClass().getAnnotation( HibernateAnnotations.LOADER );
//		if ( loaderAnnotation != null ) {
//			final AnnotationUsage.AttributeValue namedQueryValue = loaderAnnotation.getAttributeValue( "namedQuery" );
//			return namedQueryValue.asString();
//		}
//		return null;
//	}
//
//	private String[] determineSynchronizedTableNames() {
//		final AnnotationUsage<Synchronize> synchronizeAnnotation = getManagedClass().getAnnotation( HibernateAnnotations.SYNCHRONIZE );
//		if ( synchronizeAnnotation != null ) {
//			return synchronizeAnnotation.getValueAttributeValue().getValue();
//		}
//		return StringHelper.EMPTY_STRINGS;
//	}
//
//	private int determineBatchSize() {
//		final AnnotationUsage<BatchSize> batchSizeAnnotation = getManagedClass().getAnnotation( HibernateAnnotations.BATCH_SIZE );
//		if ( batchSizeAnnotation != null ) {
//			return batchSizeAnnotation.getAttributeValue( "size" ).asInt();
//		}
//		return -1;
//	}
//
//	private boolean decodeDynamicInsert() {
//		final AnnotationUsage<DynamicInsert> dynamicInsertAnnotation = getManagedClass().getAnnotation( HibernateAnnotations.DYNAMIC_INSERT );
//		if ( dynamicInsertAnnotation == null ) {
//			return false;
//		}
//
//		return dynamicInsertAnnotation.getValueAttributeValue().asBoolean();
//	}
//
//	private boolean decodeDynamicUpdate() {
//		final AnnotationUsage<DynamicUpdate> dynamicUpdateAnnotation = getManagedClass().getAnnotation( HibernateAnnotations.DYNAMIC_UPDATE );
//		if ( dynamicUpdateAnnotation == null ) {
//			return false;
//		}
//		return dynamicUpdateAnnotation.getValueAttributeValue().asBoolean();
//	}
//
//	private boolean decodeSelectBeforeUpdate() {
//		final AnnotationUsage<SelectBeforeUpdate> selectBeforeUpdateAnnotation = getManagedClass().getAnnotation( HibernateAnnotations.SELECT_BEFORE_UPDATE );
//		if ( selectBeforeUpdateAnnotation == null ) {
//			return false;
//		}
//		return selectBeforeUpdateAnnotation.getValueAttributeValue().asBoolean();
//	}

	@Override
	public String getEntityName() {
		return entityName;
	}

	@Override
	public String getJpaEntityName() {
		return explicitJpaEntityName;
	}

//	@Override
//	public String getCustomLoaderQueryName() {
//		return customLoaderQueryName;
//	}
//
//	public String[] getSynchronizedTableNames() {
//		return synchronizedTableNames;
//	}
//
//	public int getBatchSize() {
//		return batchSize;
//	}
//
//	public boolean isDynamicInsert() {
//		return isDynamicInsert;
//	}
//
//	public boolean isDynamicUpdate() {
//		return isDynamicUpdate;
//	}
//
//	public boolean isSelectBeforeUpdate() {
//		return isSelectBeforeUpdate;
//	}
//
//	public CustomSql getCustomInsert() {
//		return customInsert;
//	}
//
//	public CustomSql getCustomUpdate() {
//		return customUpdate;
//	}
//
//	public CustomSql getCustomDelete() {
//		return customDelete;
//	}
//
//	public String getDiscriminatorMatchValue() {
//		return discriminatorMatchValue;
//	}
//
//	public boolean isLazy() {
//		return isLazy;
//	}
//
//	public String getProxy() {
//		return proxy;
//	}
}
