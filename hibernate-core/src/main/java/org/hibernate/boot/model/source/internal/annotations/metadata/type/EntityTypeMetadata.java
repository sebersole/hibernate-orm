/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.type;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.AccessType;

import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.CustomSql;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.ForeignKeyInformation;
import org.hibernate.boot.model.source.internal.annotations.metadata.attribute.PrimaryKeyJoinColumn;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.model.source.spi.EntityNamingSource;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;

/**
 * Representation of metadata (configured via annotations or orm.xml) attached
 * to an Entity.
 *
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class EntityTypeMetadata extends IdentifiableTypeMetadata implements EntityNamingSource {
	private final String explicitEntityName;
	private final String explicitJpaEntityName;
	private final String customLoaderQueryName;
	private final String[] synchronizedTableNames;
	private final int batchSize;
	private final String customPersister;
	private final boolean isDynamicInsert;
	private final boolean isDynamicUpdate;
	private final boolean isSelectBeforeUpdate;
	private final CustomSql customInsert;
	private final CustomSql customUpdate;
	private final CustomSql customDelete;
	private final String discriminatorMatchValue;
	private final boolean isLazy;
	private final String proxy;
	private final ForeignKeyInformation foreignKeyInformation;

	private final ClassLoaderService classLoaderService;

	// todo : ???
	private final OnDeleteAction onDeleteAction;
	private final List<PrimaryKeyJoinColumn> joinedSubclassPrimaryKeyJoinColumnSources;


	/**
	 * This form is intended for construction of root Entity.
	 */
	public EntityTypeMetadata(
			ClassInfo classInfo,
			AccessType defaultAccessType,
			RootAnnotationBindingContext bindingContext) {
		super( classInfo, defaultAccessType, true, bindingContext );
		
		this.classLoaderService = bindingContext.getBuildingOptions().getServiceRegistry().getService(
				ClassLoaderService.class
		);

		final AnnotationInstance jpaEntityAnnotation = typeAnnotationMap().get( JpaDotNames.ENTITY );
		this.explicitEntityName = determineExplicitEntityName( jpaEntityAnnotation );
		this.explicitJpaEntityName = determineExplicitJpaEntityName( jpaEntityAnnotation );
		this.customLoaderQueryName = determineCustomLoader();
		this.synchronizedTableNames = determineSynchronizedTableNames();
		this.batchSize = determineBatchSize();

		this.customInsert = AnnotationBindingHelper.extractCustomSql(
				bindingContext.getTypeAnnotationInstances( classInfo.name() ).get( HibernateDotNames.SQL_INSERT )
		);
		this.customUpdate = AnnotationBindingHelper.extractCustomSql(
				bindingContext.getTypeAnnotationInstances( classInfo.name() ).get( HibernateDotNames.SQL_UPDATE )
		);
		this.customDelete = AnnotationBindingHelper.extractCustomSql(
				bindingContext.getTypeAnnotationInstances( classInfo.name() ).get( HibernateDotNames.SQL_DELETE )
		);

		this.isDynamicInsert = decodeDynamicInsert();
		this.isDynamicUpdate = decodeDynamicUpdate();
		this.isSelectBeforeUpdate = decodeSelectBeforeUpdate();

		final AnnotationInstance persisterAnnotation = typeAnnotationMap().get( HibernateDotNames.PERSISTER );
		if ( persisterAnnotation == null ) {
			this.customPersister = null;
		}
		else {
			this.customPersister = persisterAnnotation.value( "impl" ).asString();
		}

		// Proxy generation
		final AnnotationInstance hibernateProxyAnnotation = typeAnnotationMap().get( HibernateDotNames.PROXY );
		if ( hibernateProxyAnnotation != null ) {
			this.isLazy = getLocalBindingContext().getTypedValueExtractor( boolean.class ).extract(
					hibernateProxyAnnotation,
					"lazy"
			);
			if ( this.isLazy ) {
				this.proxy = getLocalBindingContext().getTypedValueExtractor( String.class ).extract(
						hibernateProxyAnnotation,
						"proxyClass"
				);
			}
			else {
				this.proxy = null;
			}
		}
		else {
			// defaults are that it is lazy and that the class itself is the proxy class
			this.isLazy = true;
			this.proxy = getName();
		}

		final AnnotationInstance discriminatorValueAnnotation = typeAnnotationMap().get( JpaDotNames.DISCRIMINATOR_VALUE );
		if ( discriminatorValueAnnotation != null ) {
			this.discriminatorMatchValue = discriminatorValueAnnotation.value().asString();
		}
		else {
			this.discriminatorMatchValue = null;
		}
		
		// TODO: bind JPA @ForeignKey?
		foreignKeyInformation = new ForeignKeyInformation();


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// todo : which (if any) of these to keep?
		this.joinedSubclassPrimaryKeyJoinColumnSources = determinePrimaryKeyJoinColumns();
		this.onDeleteAction = determineOnDeleteAction();
	}

	private String determineExplicitEntityName(AnnotationInstance jpaEntityAnnotation) {
//		if ( jpaEntityAnnotation == null ) {
//			// can this really ever be true here?!
//			return null;
//		}
//
//		final AnnotationValue nameValue = jpaEntityAnnotation.value( "name" );
//		if ( nameValue == null ) {
//			return null;
//		}
//
//		return StringHelper.nullIfEmpty( nameValue.asString() );
		return null;
	}

	private String determineExplicitJpaEntityName(AnnotationInstance jpaEntityAnnotation) {
		if ( jpaEntityAnnotation == null ) {
			// can this really ever be true here?!
			return null;
		}

		final AnnotationValue nameValue = jpaEntityAnnotation.value( "name" );
		if ( nameValue == null ) {
			return null;
		}

		return StringHelper.nullIfEmpty( nameValue.asString() );
	}

	private boolean decodeDynamicInsert() {
		final AnnotationInstance dynamicInsertAnnotation = typeAnnotationMap().get( HibernateDotNames.DYNAMIC_INSERT );
		if ( dynamicInsertAnnotation == null ) {
			return false;
		}

		return getLocalBindingContext().getTypedValueExtractor( boolean.class ).extract(
				dynamicInsertAnnotation,
				"value"
		);
	}

	private boolean decodeDynamicUpdate() {
		final AnnotationInstance dynamicUpdateAnnotation = typeAnnotationMap().get( HibernateDotNames.DYNAMIC_UPDATE );
		if ( dynamicUpdateAnnotation == null ) {
			return false;
		}

		return getLocalBindingContext().getTypedValueExtractor( boolean.class ).extract(
				dynamicUpdateAnnotation,
				"value"
		);
	}

	private boolean decodeSelectBeforeUpdate() {
		final AnnotationInstance selectBeforeUpdateAnnotation = typeAnnotationMap().get( HibernateDotNames.SELECT_BEFORE_UPDATE );
		if ( selectBeforeUpdateAnnotation == null ) {
			return false;
		}

		return getLocalBindingContext().getTypedValueExtractor( boolean.class ).extract(
				selectBeforeUpdateAnnotation,
				"value"
		);
	}

	/**
	 * This form is intended for construction of Entity subclasses.
	 */
	public EntityTypeMetadata(
			ClassInfo classInfo,
			IdentifiableTypeMetadata superType,
			AccessType defaultAccessType,
			RootAnnotationBindingContext bindingContext) {
		super( classInfo, superType, defaultAccessType, bindingContext );
		
		this.classLoaderService = bindingContext.getBuildingOptions().getServiceRegistry().getService(
				ClassLoaderService.class
		);

		final AnnotationInstance jpaEntityAnnotation = typeAnnotationMap().get( JpaDotNames.ENTITY );
		this.explicitEntityName = determineExplicitEntityName( jpaEntityAnnotation );
		this.explicitJpaEntityName = determineExplicitJpaEntityName( jpaEntityAnnotation );

		this.customLoaderQueryName = determineCustomLoader();
		this.synchronizedTableNames = determineSynchronizedTableNames();
		this.batchSize = determineBatchSize();

		this.customInsert = AnnotationBindingHelper.extractCustomSql(
				bindingContext.getTypeAnnotationInstances( classInfo.name() ).get( HibernateDotNames.SQL_INSERT )
		);
		this.customUpdate = AnnotationBindingHelper.extractCustomSql(
				bindingContext.getTypeAnnotationInstances( classInfo.name() ).get( HibernateDotNames.SQL_UPDATE )
		);
		this.customDelete = AnnotationBindingHelper.extractCustomSql(
				bindingContext.getTypeAnnotationInstances( classInfo.name() ).get( HibernateDotNames.SQL_DELETE )
		);

		this.isDynamicInsert = decodeDynamicInsert();
		this.isDynamicUpdate = decodeDynamicUpdate();
		this.isSelectBeforeUpdate = decodeSelectBeforeUpdate();

		final AnnotationInstance persisterAnnotation = typeAnnotationMap().get( HibernateDotNames.PERSISTER );
		if ( persisterAnnotation == null ) {
			this.customPersister = null;
		}
		else {
			this.customPersister = persisterAnnotation.value( "impl" ).asString();
		}

		// Proxy generation
		final AnnotationInstance hibernateProxyAnnotation = typeAnnotationMap().get( HibernateDotNames.PROXY );
		if ( hibernateProxyAnnotation != null ) {
			this.isLazy = getLocalBindingContext().getTypedValueExtractor( boolean.class ).extract(
					hibernateProxyAnnotation,
					"lazy"
			);
			if ( this.isLazy ) {
				this.proxy = getLocalBindingContext().getTypedValueExtractor( String.class ).extract(
						hibernateProxyAnnotation,
						"proxyClass"
				);
			}
			else {
				this.proxy = null;
			}
		}
		else {
			// defaults are that it is lazy and that the class itself is the proxy class
			this.isLazy = true;
			this.proxy = getName();
		}

		final AnnotationInstance discriminatorValueAnnotation = typeAnnotationMap().get( JpaDotNames.DISCRIMINATOR_VALUE );
		if ( discriminatorValueAnnotation != null ) {
			this.discriminatorMatchValue = discriminatorValueAnnotation.value().asString();
		}
		else {
			this.discriminatorMatchValue = null;
		}

		// TODO: bind JPA @ForeignKey?
		foreignKeyInformation = new ForeignKeyInformation();

		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// todo : which (if any) of these to keep?
		this.joinedSubclassPrimaryKeyJoinColumnSources = determinePrimaryKeyJoinColumns();
		this.onDeleteAction = determineOnDeleteAction();
	}

	private String determineCustomLoader() {
		String customLoader = null;
		// Custom sql loader
		final AnnotationInstance sqlLoaderAnnotation = typeAnnotationMap().get( HibernateDotNames.LOADER );
		if ( sqlLoaderAnnotation != null && sqlLoaderAnnotation.target() instanceof ClassInfo ) {
			customLoader = sqlLoaderAnnotation.value( "namedQuery" ).asString();
		}
		return customLoader;
	}

	private String[] determineSynchronizedTableNames() {
		final AnnotationInstance synchronizeAnnotation = typeAnnotationMap().get( HibernateDotNames.SYNCHRONIZE );
		if ( synchronizeAnnotation != null ) {
			return synchronizeAnnotation.value().asStringArray();
		}
		else {
			return StringHelper.EMPTY_STRINGS;
		}
	}

	private int determineBatchSize() {
		final AnnotationInstance batchSizeAnnotation = typeAnnotationMap().get( HibernateDotNames.BATCH_SIZE );
		return batchSizeAnnotation == null ? -1 : batchSizeAnnotation.value( "size" ).asInt();
	}

	private OnDeleteAction determineOnDeleteAction() {
		final AnnotationInstance onDeleteAnnotation = typeAnnotationMap().get( HibernateDotNames.ON_DELETE );
		if ( onDeleteAnnotation == null ) {
			return OnDeleteAction.NO_ACTION;
		}

		return getLocalBindingContext().getTypedValueExtractor( OnDeleteAction.class ).extract(
				onDeleteAnnotation,
				"action"
		);
	}


	public String getExplicitEntityName() {
		return explicitEntityName;
	}

	@Override
	public String getEntityName() {
		return getClassInfo().name().local();
	}

	@Override
	public String getJpaEntityName() {
		return explicitJpaEntityName;
	}

	@Override
	public String getTypeName() {
		return StringHelper.isNotEmpty( getJpaEntityName() ) ? getJpaEntityName() : getClassName();
	}

	@Override
	public String getClassName() {
		return getClassInfo().name().toString();
	}

	public boolean isDynamicInsert() {
		return isDynamicInsert;
	}

	public boolean isDynamicUpdate() {
		return isDynamicUpdate;
	}

	public boolean isSelectBeforeUpdate() {
		return isSelectBeforeUpdate;
	}

	public String getCustomLoaderQueryName() {
		return customLoaderQueryName;
	}

	public CustomSql getCustomInsert() {
		return customInsert;
	}

	public CustomSql getCustomUpdate() {
		return customUpdate;
	}

	public CustomSql getCustomDelete() {
		return customDelete;
	}

	public String[] getSynchronizedTableNames() {
		return synchronizedTableNames;
	}

	public List<PrimaryKeyJoinColumn> getJoinedSubclassPrimaryKeyJoinColumnSources() {
		return joinedSubclassPrimaryKeyJoinColumnSources;
	}

	public String getCustomPersister() {
		return customPersister;
	}

	public boolean isLazy() {
		return isLazy;
	}

	public String getProxy() {
		return proxy;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public boolean isEntityRoot() {
		return getSuperType() == null;
	}

	public String getDiscriminatorMatchValue() {
		return discriminatorMatchValue;
	}

	public ForeignKeyInformation getForeignKeyInformation() {
		return foreignKeyInformation;
	}

	public OnDeleteAction getOnDeleteAction() {
		return onDeleteAction;
	}

	protected List<PrimaryKeyJoinColumn> determinePrimaryKeyJoinColumns() {
		final AnnotationInstance primaryKeyJoinColumns = typeAnnotationMap().get( JpaDotNames.PRIMARY_KEY_JOIN_COLUMNS );
		final AnnotationInstance primaryKeyJoinColumn = typeAnnotationMap().get( JpaDotNames.PRIMARY_KEY_JOIN_COLUMN );

		final List<PrimaryKeyJoinColumn> results;
		if ( primaryKeyJoinColumns != null ) {
			AnnotationInstance[] values = primaryKeyJoinColumns.value().asNestedArray();
			results = new ArrayList<PrimaryKeyJoinColumn>( values.length );
			for ( final AnnotationInstance annotationInstance : values ) {
				results.add( new PrimaryKeyJoinColumn( annotationInstance ) );
			}
		}
		else if ( primaryKeyJoinColumn != null ) {
			results = new ArrayList<PrimaryKeyJoinColumn>( 1 );
			results.add( new PrimaryKeyJoinColumn( primaryKeyJoinColumn ) );
		}
		else {
			results = null;
		}
		return results;
	}

	public boolean hasMultiTenancySourceInformation() {
		return getSuperType().hasMultiTenancySourceInformation();
	}

	public boolean containsDiscriminator() {
		return containsDiscriminator( this );
	}

	public boolean containsDiscriminator(IdentifiableTypeMetadata typeMetadata) {
		final boolean hasLocally = typeMetadata.typeAnnotationMap().containsKey( JpaDotNames.DISCRIMINATOR_COLUMN )
				|| typeMetadata.typeAnnotationMap().containsKey( HibernateDotNames.DISCRIMINATOR_FORMULA );
		if ( hasLocally ) {
			return true;
		}

		if ( typeMetadata.getSuperType() != null ) {
			return containsDiscriminator( typeMetadata.getSuperType() );
		}

		return false;
	}
}
