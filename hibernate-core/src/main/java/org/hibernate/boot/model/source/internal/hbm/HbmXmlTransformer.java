/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.hbm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.persistence.FetchType;
import javax.persistence.TemporalType;
import javax.xml.bind.JAXBElement;

import org.hibernate.MappingException;
import org.hibernate.annotations.FetchMode;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.hbm.spi.EntityInfo;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmAnyAssociationType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmArrayType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmAuxiliaryDatabaseObjectType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmBasicAttributeType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmClassRenameType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmColumnType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmCompositeAttributeType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmCompositeIdType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmCompositeKeyBasicAttributeType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmCompositeKeyManyToOneType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmConfigParameterType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmDiscriminatorSubclassEntityType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmDynamicComponentType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmEntityBaseDefinition;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmFetchProfileType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmFetchStyleEnum;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmFetchStyleWithSubselectEnum;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmFilterAliasMappingType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmFilterDefinitionType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmFilterParameterType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmFilterType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmHibernateMapping;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmIdBagCollectionType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmIdentifierGeneratorDefinitionType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmIndexType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmJoinedSubclassEntityType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmLazyEnum;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmLazyWithExtraEnum;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmLazyWithNoProxyEnum;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmListIndexType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmListType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmManyToOneType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmMapType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmNamedNativeQueryType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmNamedQueryType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmNativeQueryCollectionLoadReturnType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmNativeQueryJoinReturnType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmNativeQueryPropertyReturnType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmNativeQueryPropertyReturnType.JaxbHbmReturnColumn;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmNativeQueryReturnType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmNativeQueryScalarReturnType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmOnDeleteEnum;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmOneToOneType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmOuterJoinEnum;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmPrimitiveArrayType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmPropertiesType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmQueryParamType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmResultSetMappingType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmRootEntityType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSecondaryTableType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSetType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSimpleIdType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmSynchronizeType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmTimestampAttributeType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmToolingHintType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmTuplizerType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmTypeDefinitionType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmUnionSubclassEntityType;
import org.hibernate.boot.jaxb.hbm.spi.JaxbHbmVersionAttributeType;
import org.hibernate.boot.jaxb.hbm.spi.PluralAttributeInfo;
import org.hibernate.boot.jaxb.mapping.spi.AttributesContainer;
import org.hibernate.boot.jaxb.mapping.spi.CollectionAttribute;
import org.hibernate.boot.jaxb.mapping.spi.FetchableAttribute;
import org.hibernate.boot.jaxb.mapping.spi.JaxbAny;
import org.hibernate.boot.jaxb.mapping.spi.JaxbAttributes;
import org.hibernate.boot.jaxb.mapping.spi.JaxbBasic;
import org.hibernate.boot.jaxb.mapping.spi.JaxbCacheElement;
import org.hibernate.boot.jaxb.mapping.spi.JaxbCollectionTable;
import org.hibernate.boot.jaxb.mapping.spi.JaxbColumn;
import org.hibernate.boot.jaxb.mapping.spi.JaxbDiscriminatorColumn;
import org.hibernate.boot.jaxb.mapping.spi.JaxbElementCollection;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEmbeddable;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEmbeddableAttributes;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEmbedded;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEmbeddedId;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEmptyType;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEntity;
import org.hibernate.boot.jaxb.mapping.spi.JaxbEntityMappings;
import org.hibernate.boot.jaxb.mapping.spi.JaxbForeignKey;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmCascadeType;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmCustomSql;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmFetchProfile;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmFilter;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmFilterDef;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmIdGenerator;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmIdGeneratorDef;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmLoader;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmMultiTenancy;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmParam;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmToolingHint;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmType;
import org.hibernate.boot.jaxb.mapping.spi.JaxbHbmTypeDef;
import org.hibernate.boot.jaxb.mapping.spi.JaxbId;
import org.hibernate.boot.jaxb.mapping.spi.JaxbIdClass;
import org.hibernate.boot.jaxb.mapping.spi.JaxbInheritance;
import org.hibernate.boot.jaxb.mapping.spi.JaxbInheritanceType;
import org.hibernate.boot.jaxb.mapping.spi.JaxbJoinColumn;
import org.hibernate.boot.jaxb.mapping.spi.JaxbManyToAny;
import org.hibernate.boot.jaxb.mapping.spi.JaxbManyToMany;
import org.hibernate.boot.jaxb.mapping.spi.JaxbManyToOne;
import org.hibernate.boot.jaxb.mapping.spi.JaxbMapKeyColumn;
import org.hibernate.boot.jaxb.mapping.spi.JaxbNamedNativeQuery;
import org.hibernate.boot.jaxb.mapping.spi.JaxbNamedQuery;
import org.hibernate.boot.jaxb.mapping.spi.JaxbNaturalId;
import org.hibernate.boot.jaxb.mapping.spi.JaxbOnDeleteType;
import org.hibernate.boot.jaxb.mapping.spi.JaxbOneToMany;
import org.hibernate.boot.jaxb.mapping.spi.JaxbOneToOne;
import org.hibernate.boot.jaxb.mapping.spi.JaxbOrderColumn;
import org.hibernate.boot.jaxb.mapping.spi.JaxbPersistenceUnitMetadata;
import org.hibernate.boot.jaxb.mapping.spi.JaxbPrimaryKeyJoinColumn;
import org.hibernate.boot.jaxb.mapping.spi.JaxbQueryParamType;
import org.hibernate.boot.jaxb.mapping.spi.JaxbSecondaryTable;
import org.hibernate.boot.jaxb.mapping.spi.JaxbSqlResultSetMapping;
import org.hibernate.boot.jaxb.mapping.spi.JaxbSqlResultSetMappingColumnResult;
import org.hibernate.boot.jaxb.mapping.spi.JaxbSqlResultSetMappingEntityResult;
import org.hibernate.boot.jaxb.mapping.spi.JaxbSqlResultSetMappingFieldResult;
import org.hibernate.boot.jaxb.mapping.spi.JaxbSynchronizeType;
import org.hibernate.boot.jaxb.mapping.spi.JaxbTable;
import org.hibernate.boot.jaxb.mapping.spi.JaxbTransient;
import org.hibernate.boot.jaxb.mapping.spi.JaxbVersion;
import org.hibernate.boot.model.process.spi.ResourceLocator;
import org.hibernate.cfg.NotYetImplementedException;
import org.hibernate.internal.util.StringHelper;

import org.jboss.logging.Logger;

import static org.hibernate.internal.util.StringHelper.isNotEmpty;

/**
 * Transforms a JAXB binding of a hbm.xml file into a unified orm.xml representation
 *
 * @author Steve Ebersole
 * @author Brett Meyer
 */
public class HbmXmlTransformer {
	private static final Logger log = Logger.getLogger( HbmXmlTransformer.class );

	/**
	 * Main entry into hbm.xml transformation
	 *
	 * @param hbmXmlMapping The hbm.xml mapping to be transformed
	 * @param origin The origin of the hbm.xml mapping
	 * @param resourceLocator Access to resource lookups
	 *
	 * @return The transformed representation
	 */
	public static JaxbEntityMappings transform(
			JaxbHbmHibernateMapping hbmXmlMapping,
			Origin origin,
			ResourceLocator resourceLocator) {
		return new HbmXmlTransformer( hbmXmlMapping, origin, resourceLocator ).doTransform();
	}

	private final JaxbHbmHibernateMapping hbmXmlMapping;
	private final Origin origin;
	private final ResourceLocator resourceLocator;
	private final JaxbEntityMappings ormRoot;

	private String defaultHbmAccess;
	private String defaultHbmCascade;
	private Boolean defaultHbmLazy;

	public HbmXmlTransformer(
			JaxbHbmHibernateMapping hbmXmlMapping,
			Origin origin,
			ResourceLocator resourceLocator) {
		this.hbmXmlMapping = hbmXmlMapping;
		this.origin = origin;
		this.resourceLocator = resourceLocator;

		this.ormRoot = new JaxbEntityMappings();
		this.ormRoot.setDescription(
				"Hibernate orm.xml document auto-generated from legacy hbm.xml format via transformation " +
						"(generated at " + new Date().toString() + ")"
		);
	}

	private JaxbEntityMappings doTransform() {
		final JaxbPersistenceUnitMetadata metadata = new JaxbPersistenceUnitMetadata();
		ormRoot.setPersistenceUnitMetadata( metadata );
		metadata.setDescription(
				"Defines information which applies to the persistence unit overall, not just to this mapping file.\n\n" +
						"This transformation only specifies xml-mapping-metadata-complete."
		);
		metadata.setXmlMappingMetadataComplete( new JaxbEmptyType() );

		defaultHbmAccess = hbmXmlMapping.getDefaultAccess();
		defaultHbmCascade = hbmXmlMapping.getDefaultCascade();
		defaultHbmLazy = hbmXmlMapping.isDefaultLazy();

		ormRoot.setPackage( hbmXmlMapping.getPackage() );
		ormRoot.setSchema( hbmXmlMapping.getSchema() );
		ormRoot.setCatalog( hbmXmlMapping.getCatalog() );

		transferToolingHints();
		transferTypeDefs();
		transferIdentifierGenerators();
		transferFilterDefs();
		transferFetchProfiles();
		transferImports();
		transferResultSetMappings();
		transferNamedQueries();
		transferNamedNativeQueries();
		transferDatabaseObjects();
		transferEntities();

		return ormRoot;
	}

	private void logUnhandledContent(String description) {
		log.warnf(
				"Transformation of hbm.xml [%s] encountered unsupported content : %s",
				origin.toString(),
				description
		);
	}

	private void transferToolingHints() {
		for ( JaxbHbmToolingHintType hbmToolingHintType : hbmXmlMapping.getToolingHints() ) {
			final JaxbHbmToolingHint toolingHint = new JaxbHbmToolingHint();
			ormRoot.getToolingHint().add( toolingHint );
			toolingHint.setName( hbmToolingHintType.getName() );
			toolingHint.setInheritable( hbmToolingHintType.isInheritable() );
			toolingHint.setValue( hbmToolingHintType.getValue() );
		}
	}

	private void transferTypeDefs() {
		for ( JaxbHbmTypeDefinitionType hbmXmlTypeDef : hbmXmlMapping.getTypedef() ) {
			final JaxbHbmTypeDef typeDef = new JaxbHbmTypeDef();
			ormRoot.getHbmTypeDef().add( typeDef );
			typeDef.setName( hbmXmlTypeDef.getName() );
			typeDef.setClazz( hbmXmlTypeDef.getClazz() );

			for ( JaxbHbmConfigParameterType hbmParam : hbmXmlTypeDef.getConfigParameters() ) {
				final JaxbHbmParam param = new JaxbHbmParam();
				typeDef.getParam().add( param );
				param.setName( hbmParam.getName() );
				param.setValue( hbmParam.getValue() );
			}
		}
	}

	private void transferIdentifierGenerators() {
		for ( JaxbHbmIdentifierGeneratorDefinitionType hbmGenerator : hbmXmlMapping.getIdentifierGenerator() ) {
			final JaxbHbmIdGeneratorDef generatorDef = new JaxbHbmIdGeneratorDef();
			ormRoot.getHbmIdentifierGeneratorDef().add( generatorDef );
			generatorDef.setName( hbmGenerator.getName() );
			generatorDef.setClazz( hbmGenerator.getClazz() );
		}
	}

	@SuppressWarnings("unchecked")
	private void transferFilterDefs() {
		for ( JaxbHbmFilterDefinitionType hbmFilterDef : hbmXmlMapping.getFilterDef() ) {
			final JaxbHbmFilterDef filterDef = new JaxbHbmFilterDef();
			ormRoot.getHbmFilterDef().add( filterDef );
			filterDef.setName( hbmFilterDef.getName() );

			boolean foundCondition = false;
			for ( Object content : hbmFilterDef.getContent() ) {
				if ( String.class.isInstance( content ) ) {
					final String condition = ( (String) content ).trim();
					if (! StringHelper.isEmpty( condition )) {
						foundCondition = true;
						filterDef.setCondition( condition );
					}
				}
				else {
					final JaxbHbmFilterParameterType hbmFilterParam = ( (JAXBElement<JaxbHbmFilterParameterType>) content ).getValue();
					final JaxbHbmFilterDef.JaxbFilterParam param = new JaxbHbmFilterDef.JaxbFilterParam();
					filterDef.getFilterParam().add( param );
					param.setName( hbmFilterParam.getParameterName() );
					param.setType( hbmFilterParam.getParameterValueTypeName() );
				}
			}

			if ( !foundCondition ) {
				filterDef.setCondition( hbmFilterDef.getCondition() );
			}
		}
	}

	private void transferImports() {
		// todo : rename this to "HQL import"
		for ( JaxbHbmClassRenameType hbmImport : hbmXmlMapping.getImport() ) {
			final JaxbEntityMappings.JaxbImport ormImport = new JaxbEntityMappings.JaxbImport();
			ormRoot.getImport().add( ormImport );
			ormImport.setClazz( hbmImport.getClazz() );
			ormImport.setRename( hbmImport.getRename() );
		}
	}

	private void transferResultSetMappings() {
		for ( JaxbHbmResultSetMappingType hbmResultSet : hbmXmlMapping.getResultset() ) {
			final String resultMappingName = hbmResultSet.getName();

			final JaxbSqlResultSetMapping mapping = new JaxbSqlResultSetMapping();
			mapping.setName( resultMappingName );
			mapping.setDescription( "SQL ResultSet mapping - " + resultMappingName );

			for ( Serializable hbmReturn : hbmResultSet.getValueMappingSources() ) {
				if ( hbmReturn instanceof JaxbHbmNativeQueryReturnType ) {
					mapping.getEntityResult().add(
							transferEntityReturnElement(
									resultMappingName,
									(JaxbHbmNativeQueryReturnType) hbmReturn
							)
					);
				}
				else if ( hbmReturn instanceof JaxbHbmNativeQueryScalarReturnType ) {
					mapping.getColumnResult().add(
							transferScalarReturnElement(
									resultMappingName,
									(JaxbHbmNativeQueryScalarReturnType) hbmReturn
							)
					);
				}
				else if ( hbmReturn instanceof JaxbHbmNativeQueryJoinReturnType ) {
					logUnhandledContent(
							String.format(
									"SQL ResultSet mapping [name=%s] contained a <return-join/> element, " +
											"which is not supported for transformation",
									resultMappingName
							)
					);
				}
				else if ( hbmReturn instanceof JaxbHbmNativeQueryCollectionLoadReturnType ) {
					logUnhandledContent(
							String.format(
									"SQL ResultSet mapping [name=%s] contained a <collection-load/> element, " +
											"which is not supported for transformation",
									resultMappingName
							)
					);
				}
				else {
					// should never happen thanks to XSD
					logUnhandledContent(
							String.format(
									"SQL ResultSet mapping [name=%s] contained an unexpected element type",
									resultMappingName
							)
					);
				}
			}

			ormRoot.getSqlResultSetMapping().add( mapping );
		}
	}

	private JaxbSqlResultSetMappingEntityResult transferEntityReturnElement(
			String resultMappingName,
			JaxbHbmNativeQueryReturnType hbmReturn) {
		final JaxbSqlResultSetMappingEntityResult entityResult = new JaxbSqlResultSetMappingEntityResult();
		entityResult.setEntityClass( getFullyQualifiedClassName( hbmReturn.getClazz() ) );

		for ( JaxbHbmNativeQueryPropertyReturnType propertyReturn : hbmReturn.getReturnProperty() ) {
			final JaxbSqlResultSetMappingFieldResult field = new JaxbSqlResultSetMappingFieldResult();
			final List<String> columns = new ArrayList<String>();
			if (! StringHelper.isEmpty( propertyReturn.getColumn() )) {
				columns.add( propertyReturn.getColumn() );
			}

			for ( JaxbHbmReturnColumn returnColumn : propertyReturn.getReturnColumn() ) {
				columns.add( returnColumn.getName() );
			}

			if ( columns.size() > 1 ) {
				logUnhandledContent(
						String.format(
								"SQL ResultSet mapping [name=%s] contained a <return-property name='%s'/> element " +
										"declaring multiple 1 column mapping, which is not supported for transformation;" +
										"skipping that return-property mapping",
								resultMappingName,
								propertyReturn.getName()
						)
				);
				continue;
			}

			field.setColumn( columns.get( 0 ) );
			field.setName( propertyReturn.getName() );
			entityResult.getFieldResult().add( field );
		}
		return entityResult;
	}

	private JaxbSqlResultSetMappingColumnResult transferScalarReturnElement(
			String resultMappingName,
			JaxbHbmNativeQueryScalarReturnType hbmReturn) {
		final JaxbSqlResultSetMappingColumnResult columnResult = new JaxbSqlResultSetMappingColumnResult();
		columnResult.setName( hbmReturn.getColumn() );
		columnResult.setClazz( hbmReturn.getType() );
		logUnhandledContent(
				String.format(
						"SQL ResultSet mapping [name=%s] contained a <return-scalar column='%s'/> element; " +
								"transforming type->class likely requires manual adjustment",
						resultMappingName,
						hbmReturn.getColumn()
				)
		);
		return columnResult;
	}

	private void transferFetchProfiles() {
		for ( JaxbHbmFetchProfileType hbmFetchProfile : hbmXmlMapping.getFetchProfile() ) {
			ormRoot.getHbmFetchProfile().add( transferFetchProfile( hbmFetchProfile ) );
		}
	}

	private JaxbHbmFetchProfile transferFetchProfile(JaxbHbmFetchProfileType hbmFetchProfile) {
		final JaxbHbmFetchProfile fetchProfile = new JaxbHbmFetchProfile();
		fetchProfile.setName( hbmFetchProfile.getName() );
		for ( JaxbHbmFetchProfileType.JaxbHbmFetch hbmFetch : hbmFetchProfile.getFetch() ) {
			final JaxbHbmFetchProfile.JaxbFetch fetch = new JaxbHbmFetchProfile.JaxbFetch();
			fetchProfile.getFetch().add( fetch );
			fetch.setEntity( hbmFetch.getEntity() );
			fetch.setAssociation( hbmFetch.getAssociation() );
			fetch.setStyle( hbmFetch.getStyle().value() );
		}
		return fetchProfile;
	}

	private void transferNamedQueries() {
		for ( JaxbHbmNamedQueryType hbmQuery : hbmXmlMapping.getQuery() ) {
			ormRoot.getNamedQuery().add( transferNamedQuery( hbmQuery, hbmQuery.getName() ) );
		}
	}
	
	private JaxbNamedQuery transferNamedQuery(JaxbHbmNamedQueryType hbmQuery, String name) {
		// todo : may be better to handle the "extension" attributes here via hints
		final JaxbNamedQuery query = new JaxbNamedQuery();
		query.setName( name );
		query.setCacheable( hbmQuery.isCacheable() );
		query.setCacheMode( hbmQuery.getCacheMode() );
		query.setCacheRegion( hbmQuery.getCacheRegion() );
		query.setComment( hbmQuery.getComment() );
		query.setFetchSize( hbmQuery.getFetchSize() );
		query.setFlushMode( hbmQuery.getFlushMode() );
		query.setFetchSize( hbmQuery.getFetchSize() );
		query.setReadOnly( hbmQuery.isReadOnly() );
		query.setTimeout( hbmQuery.getTimeout() );

		for ( Object content : hbmQuery.getContent() ) {
			if ( String.class.isInstance( content ) ) {
				String s = (String) content;
				s = s.trim();
				query.setQuery( s );
			}
			else {
				@SuppressWarnings("unchecked") final JAXBElement<JaxbHbmQueryParamType> element = (JAXBElement<JaxbHbmQueryParamType>) content;
				final JaxbHbmQueryParamType hbmQueryParam = element.getValue();
				final JaxbQueryParamType queryParam = new JaxbQueryParamType();
				query.getQueryParam().add( queryParam );
				queryParam.setName( hbmQueryParam.getName() );
				queryParam.setType( hbmQueryParam.getType() );
			}
		}
		
		return query;
	}

	private void transferNamedNativeQueries() {
		for ( JaxbHbmNamedNativeQueryType hbmQuery : hbmXmlMapping.getSqlQuery() ) {
			ormRoot.getNamedNativeQuery().add( transferNamedNativeQuery( hbmQuery, hbmQuery.getName() ) );
		}
	}
	
	private JaxbNamedNativeQuery transferNamedNativeQuery(JaxbHbmNamedNativeQueryType hbmQuery, String queryName) {
		final String implicitResultSetMappingName = queryName + "-implicitResultSetMapping";

		// todo : may be better to handle the "extension" attributes here via hints
		final JaxbNamedNativeQuery query = new JaxbNamedNativeQuery();
		query.setName( queryName );
		query.setCacheable( hbmQuery.isCacheable() );
		query.setCacheMode( hbmQuery.getCacheMode() );
		query.setCacheRegion( hbmQuery.getCacheRegion() );
		query.setComment( hbmQuery.getComment() );
		query.setFetchSize( hbmQuery.getFetchSize() );
		query.setFlushMode( hbmQuery.getFlushMode() );
		query.setFetchSize( hbmQuery.getFetchSize() );
		query.setReadOnly( hbmQuery.isReadOnly() );
		query.setTimeout( hbmQuery.getTimeout() );

		JaxbSqlResultSetMapping implicitResultSetMapping = null;

		// JaxbQueryElement#content elements can be either the query or parameters
		for ( Object content : hbmQuery.getContent() ) {
			if ( String.class.isInstance( content ) ) {
				String s = (String) content;
				s = s.trim();
				query.setQuery( s );
			}
			else if ( content instanceof JAXBElement ) {
				final Object element = ( (JAXBElement) content ).getValue();
				if ( element instanceof JaxbHbmQueryParamType ) {
					final JaxbHbmQueryParamType hbmQueryParam = (JaxbHbmQueryParamType) element;
					final JaxbQueryParamType queryParam = new JaxbQueryParamType();
					queryParam.setName( hbmQueryParam.getName() );
					queryParam.setType( hbmQueryParam.getType() );
					query.getQueryParam().add( queryParam );
				}
				else if ( element instanceof JaxbHbmNativeQueryScalarReturnType ) {
					if ( implicitResultSetMapping == null ) {
						implicitResultSetMapping = new JaxbSqlResultSetMapping();
						implicitResultSetMapping.setName( implicitResultSetMappingName );
						implicitResultSetMapping.setDescription(
								String.format(
										Locale.ROOT,
										"ResultSet mapping implicitly created for named native query `%s` during hbm.xml transformation",
										queryName
								)
						);
						ormRoot.getSqlResultSetMapping().add( implicitResultSetMapping );
					}
					implicitResultSetMapping.getColumnResult().add(
							transferScalarReturnElement(
									implicitResultSetMappingName,
									(JaxbHbmNativeQueryScalarReturnType) element
							)
					);
				}
				else if ( element instanceof JaxbHbmNativeQueryReturnType ) {
					if ( implicitResultSetMapping == null ) {
						implicitResultSetMapping = new JaxbSqlResultSetMapping();
						implicitResultSetMapping.setName( implicitResultSetMappingName );
						implicitResultSetMapping.setDescription(
								String.format(
										Locale.ROOT,
										"ResultSet mapping implicitly created for named native query `%s` during hbm.xml transformation",
										queryName
								)
						);
						ormRoot.getSqlResultSetMapping().add( implicitResultSetMapping );
					}
					implicitResultSetMapping.getEntityResult().add(
							transferEntityReturnElement(
									implicitResultSetMappingName,
									(JaxbHbmNativeQueryReturnType) element
							)
					);
				}
				else if ( element instanceof JaxbHbmNativeQueryCollectionLoadReturnType ) {
					logUnhandledContent(
							String.format(
									"Named native query [name=%s] contained a <collection-load/> element, " +
											"which is not supported for transformation",
									queryName
							)
					);
				}
				else if ( element instanceof JaxbHbmNativeQueryJoinReturnType ) {
					logUnhandledContent(
							String.format(
									"Named native query [name=%s] contained a <return-join/> element, " +
											"which is not supported for transformation",
									queryName
							)
					);
				}
				else if ( element instanceof JaxbHbmSynchronizeType ) {
					final JaxbHbmSynchronizeType hbmSynchronize = (JaxbHbmSynchronizeType) element;
					final JaxbSynchronizeType synchronize = new JaxbSynchronizeType();
					synchronize.setTable( hbmSynchronize.getTable() );
					query.getSynchronize().add( synchronize );
				}
				else {
					// should never happen thanks to XSD
					logUnhandledContent(
							String.format(
									"Named native query [name=%s] contained an unexpected element type",
									queryName
							)
					);
				}
			}
		}
		
		return query;
	}

	private void transferDatabaseObjects() {
		// todo : implement
		for ( JaxbHbmAuxiliaryDatabaseObjectType jaxbHbmAuxiliaryDatabaseObject : hbmXmlMapping.getDatabaseObject() ) {
			log.warn(
					"Encountered auxiliary database object definition in hbm.xml; " +
							"transformation of these is not yet implemented"
			);
		}
	}

	private void transferEntities() {
		// thoughts...
		//		1) We only need to transfer the "extends" attribute if the model is dynamic (map mode),
		//			otherwise it will be discovered via jandex
		//		2) ?? Have abstract hbm class mappings become MappedSuperclass mappings ??

		for ( JaxbHbmRootEntityType hbmClass : hbmXmlMapping.getClazz() ) {
			final JaxbEntity entity = new JaxbEntity();
			ormRoot.getEntity().add( entity );
			transferRootEntity( hbmClass, entity );
		}

		for ( JaxbHbmDiscriminatorSubclassEntityType hbmSubclass : hbmXmlMapping.getSubclass() ) {
			final JaxbEntity entity = new JaxbEntity();
			ormRoot.getEntity().add( entity );

			transferDiscriminatorSubclass( hbmSubclass, entity );
		}

		for ( JaxbHbmJoinedSubclassEntityType hbmSubclass : hbmXmlMapping.getJoinedSubclass() ) {
			final JaxbEntity entity = new JaxbEntity();
			ormRoot.getEntity().add( entity );

			transferJoinedSubclass( hbmSubclass, entity );
		}

		for ( JaxbHbmUnionSubclassEntityType hbmSubclass : hbmXmlMapping.getUnionSubclass() ) {
			final JaxbEntity entity = new JaxbEntity();
			ormRoot.getEntity().add( entity );

			transferUnionSubclass( hbmSubclass, entity );
		}

	}

	private void transferRootEntity(JaxbHbmRootEntityType hbmClass, JaxbEntity entity) {
		transferBaseEntityInformation( hbmClass, entity );
		
		entity.setMutable( hbmClass.isMutable() );

		entity.setTable( new JaxbTable() );
		entity.getTable().setCatalog( hbmClass.getCatalog() );
		entity.getTable().setSchema( hbmClass.getSchema() );
		entity.getTable().setName( hbmClass.getTable() );
		entity.getTable().setComment( hbmClass.getComment() );
		entity.getTable().setCheck( hbmClass.getCheck() );
		entity.setSubselect( hbmClass.getSubselect() );
		
		for ( JaxbHbmSynchronizeType hbmSync : hbmClass.getSynchronize() ) {
			final JaxbSynchronizeType sync = new JaxbSynchronizeType();
			sync.setTable( hbmSync.getTable() );
			entity.getSynchronize().add( sync );
		}

		if ( hbmClass.getLoader() != null ) {
			entity.setLoader( new JaxbHbmLoader() );
			entity.getLoader().setQueryRef( hbmClass.getLoader().getQueryRef() );
		}
		if ( hbmClass.getSqlInsert() != null ) {
			entity.setSqlInsert( new JaxbHbmCustomSql() );
			entity.getSqlInsert().setValue( hbmClass.getSqlInsert().getValue() );
			entity.getSqlInsert().setCheck( hbmClass.getSqlInsert().getCheck() );
			entity.getSqlInsert().setValue( hbmClass.getSqlInsert().getValue() );
		}
		if ( hbmClass.getSqlUpdate() != null ) {
			entity.setSqlUpdate( new JaxbHbmCustomSql() );
			entity.getSqlUpdate().setValue( hbmClass.getSqlUpdate().getValue() );
			entity.getSqlUpdate().setCheck( hbmClass.getSqlUpdate().getCheck() );
			entity.getSqlUpdate().setValue( hbmClass.getSqlUpdate().getValue() );
		}
		if ( hbmClass.getSqlDelete() != null ) {
			entity.setSqlDelete( new JaxbHbmCustomSql() );
			entity.getSqlDelete().setValue( hbmClass.getSqlDelete().getValue() );
			entity.getSqlDelete().setCheck( hbmClass.getSqlDelete().getCheck() );
			entity.getSqlDelete().setValue( hbmClass.getSqlDelete().getValue() );
		}
		entity.setRowid( hbmClass.getRowid() );
		entity.setWhere( hbmClass.getWhere() );

		if ( !hbmClass.getTuplizer().isEmpty() ) {
			if ( hbmClass.getTuplizer().size() > 1 ) {
				throw new MappingException( "HBM transformation: More than one entity-mode per entity not supported" );
			}
			final JaxbHbmTuplizerType tuplizerElement = hbmClass.getTuplizer().get( 0 );
			entity.setEntityMode( tuplizerElement.getEntityMode().getExternalName() );
			entity.setTuplizer( tuplizerElement.getClazz() );
		}

		entity.setOptimisticLock( hbmClass.getOptimisticLock() );

		transferDiscriminator( hbmClass, entity );
		entity.setDiscriminatorValue( hbmClass.getDiscriminatorValue() );
		entity.setPolymorphism( hbmClass.getPolymorphism().value() );

		if ( hbmClass.getMultiTenancy() != null ) {
			entity.setMultiTenancy( new JaxbHbmMultiTenancy() );
			if ( hbmClass.getMultiTenancy().getColumn() != null ) {
				entity.getMultiTenancy().setColumn( new JaxbColumn() );
				transferColumn(
						new SourceColumnAdapterJaxbHbmColumnType( hbmClass.getMultiTenancy().getColumn() ),
						new TargetColumnAdapterJaxbColumn( entity.getMultiTenancy().getColumn(), ColumnDefaultsBasicImpl.INSTANCE )
				);
			}
			entity.getMultiTenancy().setFormula( hbmClass.getMultiTenancy().getFormula() );
			entity.getMultiTenancy().setBindAsParam( hbmClass.getMultiTenancy().isBindAsParam() );
			entity.getMultiTenancy().setShared( hbmClass.getMultiTenancy().isShared() );
		}

		if ( hbmClass.getCache() != null ) {
			entity.setCache( new JaxbCacheElement() );
			entity.getCache().setRegion( hbmClass.getCache().getRegion() );
			entity.getCache().setAccess( hbmClass.getCache().getUsage() );
			entity.getCache().setInclude( hbmClass.getCache().getInclude().value() );
		}
		
		for ( JaxbHbmNamedQueryType hbmQuery : hbmClass.getQuery() ) {
			entity.getNamedQuery().add( transferNamedQuery( hbmQuery, entity.getName() + "." + hbmQuery.getName() ) );
		}
		
		for ( JaxbHbmNamedNativeQueryType hbmQuery : hbmClass.getSqlQuery() ) {
			entity.getNamedNativeQuery().add(
					transferNamedNativeQuery(
							hbmQuery,
							entity.getName() + "." + hbmQuery.getName()
					)
			);
		}
		
		for ( JaxbHbmFilterType hbmFilter : hbmClass.getFilter()) {
			entity.getFilter().add( convert( hbmFilter ) );
		}
		
		for ( JaxbHbmFetchProfileType hbmFetchProfile : hbmClass.getFetchProfile() ) {
			entity.getFetchProfile().add( transferFetchProfile( hbmFetchProfile ) );
		}

		transferAttributes( hbmClass, entity );
		
		for ( JaxbHbmJoinedSubclassEntityType hbmSubclass : hbmClass.getJoinedSubclass() ) {
			entity.setInheritance( new JaxbInheritance() );
			entity.getInheritance().setStrategy( JaxbInheritanceType.JOINED );
			
			final JaxbEntity subclassEntity = new JaxbEntity();
			ormRoot.getEntity().add( subclassEntity );
			transferJoinedSubclass( hbmSubclass, subclassEntity );
		}
		
		for (JaxbHbmUnionSubclassEntityType hbmSubclass : hbmClass.getUnionSubclass()) {
			entity.setInheritance( new JaxbInheritance() );
			entity.getInheritance().setStrategy( JaxbInheritanceType.UNION_SUBCLASS );
			
			final JaxbEntity subclassEntity = new JaxbEntity();
			ormRoot.getEntity().add( subclassEntity );
			transferUnionSubclass( hbmSubclass, subclassEntity );
		}
		
		for ( JaxbHbmDiscriminatorSubclassEntityType hbmSubclass : hbmClass.getSubclass()) {
			final JaxbEntity subclassEntity = new JaxbEntity();
			ormRoot.getEntity().add( subclassEntity );
			transferDiscriminatorSubclass( hbmSubclass, subclassEntity );
		}
		
		for ( JaxbHbmNamedQueryType hbmQuery : hbmClass.getQuery() ) {
			// Tests implied this was the case...
			final String name = hbmClass.getName() + "." + hbmQuery.getName();
			ormRoot.getNamedQuery().add( transferNamedQuery( hbmQuery, name ) );
		}
		
		for ( JaxbHbmNamedNativeQueryType hbmQuery : hbmClass.getSqlQuery() ) {
			// Tests implied this was the case...
			final String name = hbmClass.getName() + "." + hbmQuery.getName();
			ormRoot.getNamedNativeQuery().add( transferNamedNativeQuery( hbmQuery, name ) );
		}
	}

	private void transferBaseEntityInformation(JaxbHbmEntityBaseDefinition hbmClass, JaxbEntity entity) {
		entity.setMetadataComplete( true );
		entity.setName( hbmClass.getEntityName() );
		entity.setClazz( hbmClass.getName() );
		entity.setAbstract( hbmClass.isAbstract() );
		entity.setLazy( hbmClass.isLazy() );
		entity.setProxy( hbmClass.getProxy() );

		entity.setBatchSize( hbmClass.getBatchSize() );

		entity.setDynamicInsert( hbmClass.isDynamicInsert() );
		entity.setDynamicUpdate( hbmClass.isDynamicUpdate() );
		entity.setSelectBeforeUpdate( hbmClass.isSelectBeforeUpdate() );

		entity.setPersister( hbmClass.getPersister() );
	}

	private void transferDiscriminatorSubclass(JaxbHbmDiscriminatorSubclassEntityType hbmSubclass, JaxbEntity subclassEntity) {
		transferBaseEntityInformation( hbmSubclass, subclassEntity );
		if (! StringHelper.isEmpty( hbmSubclass.getDiscriminatorValue() )) {
			subclassEntity.setDiscriminatorValue( hbmSubclass.getDiscriminatorValue() );
		}
		transferEntityElementAttributes( hbmSubclass, subclassEntity );
	}

	private void transferJoinedSubclass(JaxbHbmJoinedSubclassEntityType hbmSubclass, JaxbEntity subclassEntity) {
		transferBaseEntityInformation( hbmSubclass, subclassEntity );
		transferEntityElementAttributes( hbmSubclass, subclassEntity );
		
		subclassEntity.setTable( new JaxbTable() );
		subclassEntity.getTable().setCatalog( hbmSubclass.getCatalog() );
		subclassEntity.getTable().setSchema( hbmSubclass.getSchema() );
		subclassEntity.getTable().setName( hbmSubclass.getTable() );
		subclassEntity.getTable().setComment( hbmSubclass.getComment() );
		subclassEntity.getTable().setCheck( hbmSubclass.getCheck() );
		
		if ( hbmSubclass.getKey() != null ) {
			final JaxbPrimaryKeyJoinColumn joinColumn = new JaxbPrimaryKeyJoinColumn();
			// TODO: multiple columns?
			joinColumn.setName( hbmSubclass.getKey().getColumnAttribute() );
			subclassEntity.getPrimaryKeyJoinColumn().add( joinColumn );
		}
		
		if ( !hbmSubclass.getJoinedSubclass().isEmpty() ) {
			subclassEntity.setInheritance( new JaxbInheritance() );
			subclassEntity.getInheritance().setStrategy( JaxbInheritanceType.JOINED );
			for ( JaxbHbmJoinedSubclassEntityType nestedHbmSubclass : hbmSubclass.getJoinedSubclass() ) {
				final JaxbEntity nestedSubclassEntity = new JaxbEntity();
				ormRoot.getEntity().add( nestedSubclassEntity );
				transferJoinedSubclass( nestedHbmSubclass, nestedSubclassEntity );
			}
		}
	}

	private void transferColumnsAndFormulas(
			ColumnAndFormulaSource source,
			ColumnAndFormulaTarget target,
			ColumnDefaults columnDefaults,
			String tableName) {
		if ( isNotEmpty( source.getFormulaAttribute() ) ) {
			target.addFormula( source.getFormulaAttribute() );
		}
		else if ( isNotEmpty( source.getColumnAttribute() ) ) {
			final TargetColumnAdapter column = target.makeColumnAdapter( columnDefaults );
			column.setName( source.getColumnAttribute() );
			column.setTable( tableName );
			target.addColumn( column );
		}
		else {
			for ( Serializable columnOrFormula : source.getColumnOrFormula() ) {
				if ( columnOrFormula instanceof String ) {
					target.addFormula( (String) columnOrFormula );
				}
				else {
					final JaxbHbmColumnType hbmColumn = (JaxbHbmColumnType) columnOrFormula;
					final TargetColumnAdapter column = target.makeColumnAdapter( columnDefaults );
					column.setTable( tableName );
					transferColumn( source.wrap( hbmColumn ), column );
					target.addColumn( column );
				}
			}
		}
	}

	private void transferColumn(
			SourceColumnAdapter source,
			TargetColumnAdapter target) {
		target.setName( source.getName() );

		target.setNullable( invert( source.isNotNull() ) );
		target.setUnique( source.isUnique() );

		target.setLength( source.getLength() );
		target.setScale( source.getScale() );
		target.setPrecision( source.getPrecision() );

		target.setComment( source.getComment() );

		target.setCheck( source.getCheck() );
		target.setDefault( source.getDefault() );

		target.setColumnDefinition( source.getSqlType() );

		target.setRead( source.getRead() );
		target.setWrite( source.getWrite() );

	}

	private void transferColumn(
			SourceColumnAdapter source,
			TargetColumnAdapter target,
			String tableName,
			ColumnDefaults columnDefaults) {
		target.setName( source.getName() );
		target.setTable( tableName );

		target.setNullable( invert( source.isNotNull(), columnDefaults.isNullable() ) );

		if ( source.getLength() != null ) {
			target.setLength( source.getLength() );
		}
		else {
			target.setLength( columnDefaults.getLength() );
		}

		if ( source.getScale() != null ) {
			target.setScale( source.getScale() );
		}
		else {
			target.setScale( columnDefaults.getScale() );
		}

		if ( source.getPrecision() != null ) {
			target.setPrecision( source.getPrecision() );
		}
		else {
			target.setPrecision( columnDefaults.getPrecision() );
		}

		if ( source.isUnique() != null ) {
			target.setUnique( source.isUnique() );
		}
		else {
			target.setUnique( columnDefaults.isUnique() );
		}

		target.setInsertable( columnDefaults.isInsertable() );
		target.setUpdatable( columnDefaults.isUpdateable() );

		target.setComment( source.getComment() );

		target.setCheck( source.getCheck() );
		target.setDefault( source.getDefault() );

		target.setColumnDefinition( source.getSqlType() );

		target.setRead( source.getRead() );
		target.setWrite( source.getWrite() );
	}

	private void transferDiscriminator(final JaxbHbmRootEntityType hbmClass, final JaxbEntity entity) {
		if ( hbmClass.getDiscriminator() == null ) {
			return;
		}

		if ( isNotEmpty( hbmClass.getDiscriminator().getColumnAttribute() ) ) {
			entity.setDiscriminatorColumn( new JaxbDiscriminatorColumn() );
			entity.getDiscriminatorColumn().setName( hbmClass.getDiscriminator().getColumnAttribute() );
		}
		else if ( StringHelper.isEmpty( hbmClass.getDiscriminator().getFormulaAttribute() ) ) {
			entity.setDiscriminatorFormula( hbmClass.getDiscriminator().getFormulaAttribute() );
		}
		else if ( StringHelper.isEmpty( hbmClass.getDiscriminator().getFormula() ) ) {
			entity.setDiscriminatorFormula( hbmClass.getDiscriminator().getFormulaAttribute().trim() );
		}
		else {
			entity.setDiscriminatorColumn( new JaxbDiscriminatorColumn() );
			entity.getDiscriminatorColumn().setName( hbmClass.getDiscriminator().getColumn().getName() );
			entity.getDiscriminatorColumn().setColumnDefinition( hbmClass.getDiscriminator().getColumn().getSqlType() );
			entity.getDiscriminatorColumn().setLength( hbmClass.getDiscriminator().getColumn().getLength() );
			entity.getDiscriminatorColumn().setForceInSelect( hbmClass.getDiscriminator().isForce() );
		}
	}

	private void transferAttributes(JaxbHbmRootEntityType source, JaxbEntity target) {
		transferEntityElementAttributes( source, target );

		transferIdentifier( source, target );
		transferNaturalIdentifiers( source, target );
		transferVersion( source, target );
		transferTimestamp( source, target );

		transferJoins( source, target );
	}


	private void transferEntityElementAttributes(EntityInfo hbmClass, JaxbEntity entity) {
		entity.setAttributes( new JaxbAttributes() );
		transferAttributes( hbmClass.getAttributes(), entity.getAttributes() );
	}

	private void transferAttributes(List hbmAttributeMappings, AttributesContainer attributes) {
		for ( Object hbmAttributeMapping : hbmAttributeMappings ) {
			if ( hbmAttributeMapping instanceof JaxbHbmBasicAttributeType ) {
				final JaxbHbmBasicAttributeType basic = (JaxbHbmBasicAttributeType) hbmAttributeMapping;
				attributes.getBasic().add( convertBasicAttribute( basic ) );
			}
			else if ( hbmAttributeMapping instanceof JaxbHbmCompositeAttributeType ) {
				final JaxbHbmCompositeAttributeType hbmComponent = (JaxbHbmCompositeAttributeType) hbmAttributeMapping;
				ormRoot.getEmbeddable().add( convertEmbeddable( hbmComponent ) );
				attributes.getEmbedded().add( convertEmbedded( hbmComponent ) );
			}
			else if ( hbmAttributeMapping instanceof JaxbHbmPropertiesType ) {
				final JaxbHbmPropertiesType hbmProperties = (JaxbHbmPropertiesType) hbmAttributeMapping;
				transferAttributes( hbmProperties.getAttributes(), attributes );
			}
			else if ( hbmAttributeMapping instanceof JaxbHbmDynamicComponentType ) {
				final String name = ( (JaxbHbmDynamicComponentType) hbmAttributeMapping ).getName();
				logUnhandledContent(
						String.format(
								Locale.ROOT,
								"<dynamic-component/> mappings not supported for transformation [name=%s]",
								name
						)
				);
			}
			else if ( hbmAttributeMapping instanceof JaxbHbmOneToOneType ) {
				final JaxbHbmOneToOneType o2o = (JaxbHbmOneToOneType) hbmAttributeMapping;
				attributes.getOneToOne().add( convertOneToOne( o2o ) );
			}
			else if ( hbmAttributeMapping instanceof JaxbHbmManyToOneType ) {
				final JaxbHbmManyToOneType m2o = (JaxbHbmManyToOneType) hbmAttributeMapping;
				attributes.getManyToOne().add( convertManyToOne( m2o ) );
			}
			else if ( hbmAttributeMapping instanceof JaxbHbmAnyAssociationType ) {
				final JaxbHbmAnyAssociationType any = (JaxbHbmAnyAssociationType) hbmAttributeMapping;
				attributes.getAny().add( convertAnyAttribute( any ) );

			}
			else if ( hbmAttributeMapping instanceof PluralAttributeInfo ) {
				final PluralAttributeInfo pluralAttributeInfo = (PluralAttributeInfo) hbmAttributeMapping;

				if ( pluralAttributeInfo.getElement() != null
						|| pluralAttributeInfo.getCompositeElement() != null ) {
					attributes.getElementCollection().add( convertElementCollection( pluralAttributeInfo ) );
				}
				else if ( pluralAttributeInfo.getOneToMany() != null ) {
					attributes.getOneToMany().add( convertOneToManyCollection( pluralAttributeInfo ) );
				}
				else if ( pluralAttributeInfo.getManyToMany() != null ) {
					attributes.getManyToMany().add( convertManyToManyCollection( pluralAttributeInfo ) );
				}
				else if ( pluralAttributeInfo.getManyToAny() != null ) {
					attributes.getManyToAny().add( convertManyToAnyCollection( pluralAttributeInfo ) );
				}
			}
		}
	}

	private JaxbBasic convertBasicAttribute(final JaxbHbmBasicAttributeType hbmProp) {
		final JaxbBasic basic = new JaxbBasic();
		basic.setName( hbmProp.getName() );
		basic.setOptional( hbmProp.isNotNull() == null || !hbmProp.isNotNull() );
		basic.setFetch( FetchType.EAGER );
		basic.setAttributeAccessor( hbmProp.getAccess() );
		basic.setOptimisticLock( hbmProp.isOptimisticLock() );

		if ( isNotEmpty( hbmProp.getTypeAttribute() ) ) {
			basic.setType( new JaxbHbmType() );
			basic.getType().setName( hbmProp.getTypeAttribute() );
		}
		else {
			if ( hbmProp.getType() != null ) {
				basic.setType( new JaxbHbmType() );
				basic.getType().setName( hbmProp.getType().getName() );
				for ( JaxbHbmConfigParameterType hbmParam : hbmProp.getType().getConfigParameters() ) {
					final JaxbHbmParam param = new JaxbHbmParam();
					param.setName( hbmParam.getName() );
					param.setValue( hbmParam.getValue() );
					basic.getType().getParam().add( param );
				}
			}
		}

		transferColumnsAndFormulas(
				new ColumnAndFormulaSource() {
					@Override
					public String getColumnAttribute() {
						return hbmProp.getColumnAttribute();
					}

					@Override
					public String getFormulaAttribute() {
						return hbmProp.getFormulaAttribute();
					}

					@Override
					public List<Serializable> getColumnOrFormula() {
						return hbmProp.getColumnOrFormula();
					}

					@Override
					public SourceColumnAdapter wrap(Serializable column) {
						return new SourceColumnAdapterJaxbHbmColumnType( (JaxbHbmColumnType) column );
					}
				},
				new ColumnAndFormulaTarget() {
					@Override
					public TargetColumnAdapter makeColumnAdapter(ColumnDefaults columnDefaults) {
						return new TargetColumnAdapterJaxbColumn( columnDefaults );
					}

					@Override
					public void addColumn(TargetColumnAdapter column) {
						basic.getColumnOrFormula().add( ( (TargetColumnAdapterJaxbColumn) column ).getJaxbColumn() );
					}

					@Override
					public void addFormula(String formula) {
						basic.getColumnOrFormula().add( formula );
					}
				},
				new ColumnDefaults() {
					@Override
					public Boolean isNullable() {
						return invert( hbmProp.isNotNull() );
					}

					@Override
					public Integer getLength() {
						return hbmProp.getLength();
					}

					@Override
					public Integer getScale() {
						return isNotEmpty( hbmProp.getScale() )
								? Integer.parseInt( hbmProp.getScale() )
								: null;
					}

					@Override
					public Integer getPrecision() {
						return isNotEmpty( hbmProp.getPrecision() )
								? Integer.parseInt( hbmProp.getPrecision() )
								: null;
					}

					@Override
					public Boolean isUnique() {
						return hbmProp.isUnique();
					}

					@Override
					public Boolean isInsertable() {
						return hbmProp.isInsert();
					}

					@Override
					public Boolean isUpdateable() {
						return hbmProp.isUpdate();
					}
				},
				// todo : need to push the table name down into this method to pass along
				null
		);

		return basic;
	}

	private JaxbEmbeddable convertEmbeddable(JaxbHbmCompositeAttributeType hbmComponent) {
		final JaxbEmbeddable embeddable = new JaxbEmbeddable();
		embeddable.setClazz( hbmComponent.getClazz() );
		embeddable.setAttributes( new JaxbEmbeddableAttributes() );

		transferAttributes( hbmComponent.getAttributes(), embeddable.getAttributes() );

		return embeddable;
	}

	private JaxbEmbedded convertEmbedded(JaxbHbmCompositeAttributeType hbmComponent) {
		final JaxbEmbedded embedded = new JaxbEmbedded();
		embedded.setName( hbmComponent.getName() );
		embedded.setAttributeAccessor( hbmComponent.getAccess() );
		return embedded;
	}

	private JaxbOneToOne convertOneToOne(JaxbHbmOneToOneType hbmO2O) {
		if (!hbmO2O.getFormula().isEmpty() || !StringHelper.isEmpty( hbmO2O.getFormulaAttribute() )) {
			throw new MappingException( "HBM transformation: Formulas within one-to-ones are not yet supported." );
		}

		final JaxbOneToOne o2o = new JaxbOneToOne();
		o2o.setAttributeAccessor( hbmO2O.getAccess() );
		o2o.setHbmCascade( convertCascadeType( hbmO2O.getCascade() ) );
		o2o.setOrphanRemoval( isOrphanRemoval( hbmO2O.getCascade() ) );
		o2o.setForeignKey( new JaxbForeignKey() );
		o2o.getForeignKey().setName( hbmO2O.getForeignKey() );
		if (! StringHelper.isEmpty( hbmO2O.getPropertyRef() )) {
			final JaxbJoinColumn joinColumn = new JaxbJoinColumn();
			joinColumn.setReferencedColumnName( hbmO2O.getPropertyRef() );
			o2o.getJoinColumn().add( joinColumn );
		}
		o2o.setName( hbmO2O.getName() );
		if ( isNotEmpty( hbmO2O.getEntityName() ) ) {
			o2o.setTargetEntity( hbmO2O.getEntityName() );
		}
		else {
			o2o.setTargetEntity( hbmO2O.getClazz() );
		}

		transferFetchable( hbmO2O.getLazy(), hbmO2O.getFetch(), hbmO2O.getOuterJoin(), hbmO2O.isConstrained(), o2o );

		return o2o;
	}

	private JaxbManyToOne convertManyToOne(final JaxbHbmManyToOneType hbmM2O) {
		final JaxbManyToOne m2o = new JaxbManyToOne();
		m2o.setAttributeAccessor( hbmM2O.getAccess() );
		m2o.setHbmCascade( convertCascadeType( hbmM2O.getCascade() ) );
		m2o.setForeignKey( new JaxbForeignKey() );
		m2o.getForeignKey().setName( hbmM2O.getForeignKey() );

		transferColumnsAndFormulas(
				new ColumnAndFormulaSource() {
					@Override
					public String getColumnAttribute() {
						return hbmM2O.getColumnAttribute();
					}

					@Override
					public String getFormulaAttribute() {
						return hbmM2O.getFormulaAttribute();
					}

					@Override
					public List<Serializable> getColumnOrFormula() {
						return hbmM2O.getColumnOrFormula();
					}

					@Override
					public SourceColumnAdapter wrap(Serializable column) {
						return new SourceColumnAdapterJaxbHbmColumnType( (JaxbHbmColumnType) column );
					}
				},
				new ColumnAndFormulaTarget() {
					@Override
					public TargetColumnAdapter makeColumnAdapter(ColumnDefaults columnDefaults) {
						return new TargetColumnAdapterJaxbJoinColumn( columnDefaults );
					}

					@Override
					public void addColumn(TargetColumnAdapter column) {
						m2o.getJoinColumn().add( ( (TargetColumnAdapterJaxbJoinColumn) column ).getJaxbColumn() );
					}

					@Override
					public void addFormula(String formula) {
						logUnhandledContent(
								"<many-to-one/> [name=" + hbmM2O.getName() + "] specified formula [" + formula +
										"] which is not supported for transformation; skipping"
						);
					}
				},
				ColumnDefaultsBasicImpl.INSTANCE,
				null
		);

		m2o.setName( hbmM2O.getName() );
		m2o.setOptional( hbmM2O.isNotNull() == null || !hbmM2O.isNotNull() );
		if ( isNotEmpty( hbmM2O.getEntityName() ) ) {
			m2o.setTargetEntity( hbmM2O.getEntityName() );
		}
		else {
			m2o.setTargetEntity( hbmM2O.getClazz() );
		}
		transferFetchable( hbmM2O.getLazy(), hbmM2O.getFetch(), hbmM2O.getOuterJoin(), null, m2o );
		return m2o;
	}


	private JaxbAny convertAnyAttribute(JaxbHbmAnyAssociationType hbmAny) {
		throw new NotYetImplementedException( "<any/> transformation not yet implemented" );
	}

	private JaxbElementCollection convertElementCollection(final PluralAttributeInfo source) {
		final JaxbElementCollection target = new JaxbElementCollection();
		transferCollectionTable( source, target );
		transferCollectionBasicInfo( source, target );

		if ( source instanceof JaxbHbmMapType ) {
			transferMapKey( (JaxbHbmMapType) source, target );
		}

		if ( source.getElement() != null ) {
			transferColumnsAndFormulas(
					new ColumnAndFormulaSource() {
						@Override
						public String getColumnAttribute() {
							return source.getElement().getColumnAttribute();
						}

						@Override
						public String getFormulaAttribute() {
							return source.getElement().getFormulaAttribute();
						}

						@Override
						public List<Serializable> getColumnOrFormula() {
							return source.getElement().getColumnOrFormula();
						}

						@Override
						public SourceColumnAdapter wrap(Serializable column) {
							return new SourceColumnAdapterJaxbHbmColumnType( (JaxbHbmColumnType) column );
						}
					},
					new ColumnAndFormulaTarget() {
						@Override
						public TargetColumnAdapter makeColumnAdapter(ColumnDefaults columnDefaults) {
							return new TargetColumnAdapterJaxbColumn( columnDefaults );
						}

						@Override
						public void addColumn(TargetColumnAdapter column) {
							target.setColumn( ( (TargetColumnAdapterJaxbColumn) column ).getJaxbColumn() );
						}

						@Override
						public void addFormula(String formula) {
							target.setFormula( formula );
						}
					},
					ColumnDefaultsBasicImpl.INSTANCE,
					null
			);
		}
		else {
			target.setTargetClass( source.getCompositeElement().getClazz() );

			// todo : account for same embeddable used multiple times
			final JaxbEmbeddable embeddedable = new JaxbEmbeddable();
			embeddedable.setClazz( source.getCompositeElement().getClazz() );
			embeddedable.setAttributes( new JaxbEmbeddableAttributes() );
			transferAttributes(
					source.getCompositeElement().getAttributes(),
					embeddedable.getAttributes()
			);
			ormRoot.getEmbeddable().add( embeddedable );
		}

		return target;
	}

	private void transferCollectionTable(
			final PluralAttributeInfo source,
			final JaxbElementCollection target) {
		target.setCollectionTable( new JaxbCollectionTable() );

		if ( isNotEmpty( source.getTable() ) ) {
			target.getCollectionTable().setName( source.getTable() );
			target.getCollectionTable().setCatalog( source.getCatalog() );
			target.getCollectionTable().setSchema( source.getSchema() );
		}

		transferColumnsAndFormulas(
				new ColumnAndFormulaSource() {
					@Override
					public String getColumnAttribute() {
						return source.getKey().getColumnAttribute();
					}

					@Override
					public String getFormulaAttribute() {
						return null;
					}

					@Override
					public List<Serializable> getColumnOrFormula() {
						return new ArrayList<Serializable>( source.getKey().getColumn() );
					}

					@Override
					public SourceColumnAdapter wrap(Serializable column) {
						return new SourceColumnAdapterJaxbHbmColumnType( (JaxbHbmColumnType) column );
					}
				},
				new ColumnAndFormulaTarget() {
					@Override
					public TargetColumnAdapter makeColumnAdapter(ColumnDefaults columnDefaults) {
						return new TargetColumnAdapterJaxbJoinColumn( columnDefaults );
					}

					@Override
					public void addColumn(TargetColumnAdapter column) {

					}

					@Override
					public void addFormula(String formula) {
						logUnhandledContent(
								"formula as part of element-collection key is not supported for transformation; skipping"
						);
					}
				},
				ColumnDefaultsBasicImpl.INSTANCE,
				source.getTable()

		);

		if ( isNotEmpty( source.getKey().getPropertyRef() ) ) {
			logUnhandledContent(
					"Foreign-key (<key/>) for persistent collection (name=" + source.getName() +
							") specified property-ref which is not supported for transformation; " +
							"transformed <join-column/> will need manual adjustment of referenced-column-name"
			);
		}
	}


	private void transferCollectionBasicInfo(
			PluralAttributeInfo source,
			CollectionAttribute target) {
		target.setName( source.getName() );
		target.setAttributeAccessor( source.getAccess() );
		target.setHbmFetchMode( convert( source.getFetch() ) );
		if ( source.getCollectionType() != null ) {
			target.setCollectionType( new JaxbHbmType() );
			target.getCollectionType().setName( source.getCollectionType() );
		}

		if ( source instanceof JaxbHbmSetType ) {
			final JaxbHbmSetType set = (JaxbHbmSetType) source;
			target.setSort( set.getSort() );
			target.setOrderBy( set.getOrderBy() );
		}
		else if ( source instanceof JaxbHbmMapType ) {
			final JaxbHbmMapType map = (JaxbHbmMapType) source;
			target.setSort( map.getSort() );
			target.setOrderBy( map.getOrderBy() );
		}
		else if ( source instanceof JaxbHbmIdBagCollectionType ) {
			// todo : collection-id
		}
		else if ( source instanceof JaxbHbmListType ) {
			transferListIndex(
					( (JaxbHbmListType) source ).getIndex(),
					( (JaxbHbmListType) source ).getListIndex(),
					target
			);
		}
		else if ( source instanceof JaxbHbmArrayType ) {
			transferListIndex(
					( (JaxbHbmArrayType) source ).getIndex(),
					( (JaxbHbmArrayType) source ).getListIndex(),
					target
			);
		}
		else if ( source instanceof JaxbHbmPrimitiveArrayType ) {
			transferListIndex(
					( (JaxbHbmPrimitiveArrayType) source ).getIndex(),
					( (JaxbHbmPrimitiveArrayType) source ).getListIndex(),
					target
			);
		}
	}

	private void transferListIndex(
			JaxbHbmIndexType index,
			JaxbHbmListIndexType listIndex,
			CollectionAttribute target) {
		final JaxbOrderColumn orderColumn = new JaxbOrderColumn();
		target.setOrderColumn( orderColumn );
		if ( index != null ) {
			// todo : base on order-column
			if ( isNotEmpty( index.getColumnAttribute() ) ) {
				orderColumn.setName( index.getColumnAttribute() );
			}
			else if ( index.getColumn().size() == 1 ) {
				final JaxbHbmColumnType hbmColumn = index.getColumn().get( 0 );
				orderColumn.setName( hbmColumn.getName() );
				orderColumn.setNullable( invert( hbmColumn.isNotNull() ) );
				orderColumn.setColumnDefinition( hbmColumn.getSqlType() );
			}
		}
		else if ( listIndex != null ) {
			// todo : base on order-column
			if ( isNotEmpty( listIndex.getColumnAttribute() ) ) {
				orderColumn.setName( listIndex.getColumnAttribute() );
			}
			else if ( listIndex.getColumn() != null ) {
				orderColumn.setName( listIndex.getColumn().getName() );
				orderColumn.setNullable( invert( listIndex.getColumn().isNotNull() ) );
				orderColumn.setColumnDefinition( listIndex.getColumn().getSqlType() );
			}
		}
	}

	private void transferMapKey(JaxbHbmMapType source, CollectionAttribute target) {
		if ( source.getIndex() != null ) {
			final JaxbMapKeyColumn mapKey = new JaxbMapKeyColumn();
			// TODO: multiple columns?
			mapKey.setName( source.getIndex().getColumnAttribute() );
			target.setMapKeyColumn( mapKey );
			if ( ! StringHelper.isEmpty( source.getIndex().getType() ) ) {
				final JaxbHbmType type = new JaxbHbmType();
				type.setName( source.getIndex().getType() );
				target.setMapKeyType( type );
			}
		}
		else if ( source.getMapKey() != null ) {
			if (! StringHelper.isEmpty( source.getMapKey().getFormulaAttribute() )) {
				throw new MappingException( "HBM transformation: Formulas within map keys are not yet supported." );
			}
			final JaxbMapKeyColumn mapKey = new JaxbMapKeyColumn();
			// TODO: multiple columns?
			mapKey.setName( source.getMapKey().getColumnAttribute() );
			target.setMapKeyColumn( mapKey );
			// TODO: #getType w/ attributes?
			if ( ! StringHelper.isEmpty( source.getMapKey().getTypeAttribute() ) ) {
				final JaxbHbmType type = new JaxbHbmType();
				type.setName( source.getMapKey().getTypeAttribute() );
				target.setMapKeyType( type );
			}
		}
	}

	private Boolean invert(Boolean value) {
		return invert( value, null );
	}

	private Boolean invert(Boolean value, Boolean defaultValue) {
		if ( value == null ) {
			return defaultValue;
		}
		return !value;
	}

	private FetchMode convert(JaxbHbmFetchStyleWithSubselectEnum fetch) {
		return fetch == null
				? null
				: FetchMode.valueOf( fetch.value() );
	}

	private JaxbOneToMany convertOneToManyCollection(PluralAttributeInfo pluralAttributeInfo) {
		throw new NotYetImplementedException( "OneToMany transformation not yet implemented" );
	}

	private JaxbManyToMany convertManyToManyCollection(PluralAttributeInfo pluralAttributeInfo) {
		throw new NotYetImplementedException( "ManyToMany transformation not yet implemented" );
	}

	private JaxbManyToAny convertManyToAnyCollection(PluralAttributeInfo pluralAttributeInfo) {
		throw new NotYetImplementedException( "ManyToAny transformation not yet implemented" );
	}

	private void transferIdentifier(JaxbHbmRootEntityType source, JaxbEntity target) {
		if ( source.getId() != null ) {
			target.getAttributes().getId().add( convertSimpleId( source.getId() ) );
		}
		else {
			final JaxbHbmCompositeIdType hbmCompositeId = source.getCompositeId();
			assert hbmCompositeId != null;

			final boolean isAggregate;
			if ( isNotEmpty( hbmCompositeId.getClazz() ) ) {
				// we have  <composite-id class="XYZ">.
				if ( hbmCompositeId.isMapped() ) {
					// user explicitly said the class is an "IdClass"
					isAggregate = false;
				}
				else {
					isAggregate = true;
				}
			}
			else {
				// there was no class specified, can only be non-aggregated
				isAggregate = false;
			}

			if ( isAggregate ) {
				target.getAttributes().setEmbeddedId( new JaxbEmbeddedId() );
				target.getAttributes().getEmbeddedId().setName( hbmCompositeId.getName() );
				target.getAttributes().getEmbeddedId().setAttributeAccessor( hbmCompositeId.getAccess() );

				final JaxbEmbeddable embeddable = new JaxbEmbeddable();
				embeddable.setClazz( hbmCompositeId.getClazz() );
				embeddable.setAttributes( new JaxbEmbeddableAttributes() );
				for ( Object hbmCompositeAttribute : hbmCompositeId.getKeyPropertyOrKeyManyToOne() ) {
					if ( hbmCompositeAttribute instanceof JaxbHbmCompositeKeyBasicAttributeType ) {
						final JaxbHbmCompositeKeyBasicAttributeType keyProp = (JaxbHbmCompositeKeyBasicAttributeType) hbmCompositeAttribute;
						final JaxbBasic basic = new JaxbBasic();
						basic.setName( keyProp.getName() );
						basic.setAttributeAccessor( keyProp.getAccess() );
						if ( isNotEmpty( keyProp.getColumnAttribute() ) ) {
							final JaxbColumn column = new JaxbColumn();
							column.setName( keyProp.getColumnAttribute() );
							basic.getColumnOrFormula().add( column );
						}
						else {
							for ( JaxbHbmColumnType hbmColumn : keyProp.getColumn() ) {
								final JaxbColumn column = new JaxbColumn();
								transferColumn(
										new SourceColumnAdapterJaxbHbmColumnType( hbmColumn ),
										new TargetColumnAdapterJaxbColumn( column, ColumnDefaultsInsertableNonUpdateableImpl.INSTANCE )
								);
								basic.getColumnOrFormula().add( column );
							}
						}
						embeddable.getAttributes().getBasic().add( basic );
					}
					else {
						final JaxbHbmCompositeKeyManyToOneType keyManyToOne = (JaxbHbmCompositeKeyManyToOneType) hbmCompositeAttribute;
						final JaxbManyToOne manyToOne = transferManyToOneAttribute( keyManyToOne );
						embeddable.getAttributes().getManyToOne().add( manyToOne );
					}
				}
				ormRoot.getEmbeddable().add( embeddable );
			}
			else {
				final JaxbIdClass idClass = new JaxbIdClass();
				idClass.setClazz( hbmCompositeId.getClazz() );
				target.setIdClass( idClass );
				for ( Object hbmCompositeAttribute : hbmCompositeId.getKeyPropertyOrKeyManyToOne() ) {
					if ( hbmCompositeAttribute instanceof JaxbHbmCompositeKeyBasicAttributeType ) {
						final JaxbHbmCompositeKeyBasicAttributeType keyProp = (JaxbHbmCompositeKeyBasicAttributeType) hbmCompositeAttribute;
						final JaxbId id = new JaxbId();
						id.setName( keyProp.getName() );
						id.setAttributeAccessor( keyProp.getAccess() );
						if ( isNotEmpty( keyProp.getColumnAttribute() ) ) {
							final JaxbColumn column = new JaxbColumn();
							column.setName( keyProp.getColumnAttribute() );
							id.setColumn( column );
						}
						else {
							if ( keyProp.getColumn().size() == 1 ) {
								id.setColumn( new JaxbColumn() );
								transferColumn(
										new SourceColumnAdapterJaxbHbmColumnType( keyProp.getColumn().get( 0 ) ),
										new TargetColumnAdapterJaxbColumn( id.getColumn(), ColumnDefaultsInsertableNonUpdateableImpl.INSTANCE )
								);
							}
						}
						target.getAttributes().getId().add( id );
					}
					else {
						final JaxbHbmCompositeKeyManyToOneType keyManyToOne = (JaxbHbmCompositeKeyManyToOneType) hbmCompositeAttribute;
						final JaxbManyToOne manyToOne = transferManyToOneAttribute( keyManyToOne );
						target.getAttributes().getManyToOne().add( manyToOne );
					}
				}
			}
		}
	}

	private JaxbId convertSimpleId(JaxbHbmSimpleIdType source) {
		final JaxbId target = new JaxbId();
		target.setName( source.getName() );
		target.setAttributeAccessor( source.getAccess() );

		if ( source.getGenerator() != null ) {
			final JaxbHbmIdGenerator generator = new JaxbHbmIdGenerator();
			generator.setStrategy( source.getGenerator().getClazz() );
			for ( JaxbHbmConfigParameterType param : source.getGenerator().getConfigParameters() ) {
				JaxbHbmParam hbmParam = new JaxbHbmParam();
				hbmParam.setName( param.getName() );
				hbmParam.setValue( param.getValue() );
				generator.getParam().add( hbmParam );
			}
			target.setGenerator( generator );
		}

		if ( isNotEmpty( source.getTypeAttribute() ) ) {
			target.setType( new JaxbHbmType() );
			target.getType().setName( source.getTypeAttribute() );
		}
		else {
			if ( source.getType() != null ) {
				target.setType( new JaxbHbmType() );
				target.getType().setName( source.getType().getName() );
				for ( JaxbHbmConfigParameterType hbmParam : source.getType().getConfigParameters() ) {
					final JaxbHbmParam param = new JaxbHbmParam();
					param.setName( hbmParam.getName() );
					param.setValue( hbmParam.getValue() );
					target.getType().getParam().add( param );
				}
			}
		}

		target.setUnsavedValue( source.getUnsavedValue() );

		if ( isNotEmpty( source.getColumnAttribute() ) ) {
			target.setColumn( new JaxbColumn() );
			target.getColumn().setName( source.getColumnAttribute() );
		}
		else {
			if ( source.getColumn() != null ) {
				if ( source.getColumn().size() == 1 ) {
					target.setColumn( new JaxbColumn() );
					transferColumn(
							new SourceColumnAdapterJaxbHbmColumnType( source.getColumn().get( 0 ) ),
							new TargetColumnAdapterJaxbColumn( target.getColumn(), ColumnDefaultsInsertableNonUpdateableImpl.INSTANCE )
					);
				}
			}
		}

		return target;
	}


	private void transferNaturalIdentifiers(JaxbHbmRootEntityType source, JaxbEntity target) {
		if ( source.getNaturalId() == null ) {
			return;
		}

		final JaxbNaturalId naturalId = new JaxbNaturalId();
		transferAttributes(
				source.getNaturalId().getAttributes(),
				new AttributesContainer() {
					@Override
					public List<JaxbTransient> getTransient() {
						return null;
					}

					@Override
					public List<JaxbBasic> getBasic() {
						return naturalId.getBasic();
					}

					@Override
					public List<JaxbEmbedded> getEmbedded() {
						return naturalId.getEmbedded();
					}

					@Override
					public List<JaxbOneToOne> getOneToOne() {
						return null;
					}

					@Override
					public List<JaxbManyToOne> getManyToOne() {
						return naturalId.getManyToOne();
					}

					@Override
					public List<JaxbAny> getAny() {
						return naturalId.getAny();
					}

					@Override
					public List<JaxbElementCollection> getElementCollection() {
						return null;
					}

					@Override
					public List<JaxbManyToMany> getManyToMany() {
						return null;
					}

					@Override
					public List<JaxbOneToMany> getOneToMany() {
						return null;
					}

					@Override
					public List<JaxbManyToAny> getManyToAny() {
						return null;
					}
				}
		);

		naturalId.setMutable( source.getNaturalId().isMutable() );
		target.getAttributes().setNaturalId( naturalId );
	}
	
	private void transferVersion(JaxbHbmRootEntityType source, JaxbEntity target) {
		final JaxbHbmVersionAttributeType hbmVersion = source.getVersion();
		if ( hbmVersion != null ) {
			final JaxbVersion version = new JaxbVersion();
			version.setName( hbmVersion.getName() );
			// TODO: multiple columns?
			if ( isNotEmpty( hbmVersion.getColumnAttribute() ) ) {
				version.setColumn( new JaxbColumn() );
				version.getColumn().setName( hbmVersion.getColumnAttribute() );
			}
			target.getAttributes().getVersion().add( version );
		}
	}
	
	private void transferTimestamp(JaxbHbmRootEntityType source, JaxbEntity target) {
		final JaxbHbmTimestampAttributeType hbmTimestamp = source.getTimestamp();
		if ( hbmTimestamp != null ) {
			final JaxbVersion version = new JaxbVersion();
			version.setName( hbmTimestamp.getName() );
			// TODO: multiple columns?
			if ( isNotEmpty( hbmTimestamp.getColumnAttribute() ) ) {
				version.setColumn( new JaxbColumn() );
				version.getColumn().setName( hbmTimestamp.getColumnAttribute() );
			}
			version.setTemporal( TemporalType.TIMESTAMP );
			target.getAttributes().getVersion().add( version );
		}
	}
	
	private void transferJoins(JaxbHbmRootEntityType source, JaxbEntity target) {
		for ( JaxbHbmSecondaryTableType hbmJoin : source.getJoin() ) {
			if ( !hbmJoin.isInverse() ) {
				final JaxbSecondaryTable secondaryTable = new JaxbSecondaryTable();
				secondaryTable.setCatalog( hbmJoin.getCatalog() );
				secondaryTable.setComment( hbmJoin.getComment() );
				secondaryTable.setName( hbmJoin.getTable() );
				secondaryTable.setSchema( hbmJoin.getSchema() );
				secondaryTable.setOptional( hbmJoin.isOptional() );
				if (hbmJoin.getKey() != null) {
					final JaxbPrimaryKeyJoinColumn joinColumn = new JaxbPrimaryKeyJoinColumn();
					// TODO: multiple columns?
					joinColumn.setName( hbmJoin.getKey().getColumnAttribute() );
					secondaryTable.getPrimaryKeyJoinColumn().add( joinColumn );
				}
				target.getSecondaryTable().add( secondaryTable );
			}
			
			for ( Serializable attributeMapping : hbmJoin.getAttributes() ) {
				if ( attributeMapping instanceof JaxbHbmBasicAttributeType ) {
					final JaxbBasic prop = convertBasicAttribute( (JaxbHbmBasicAttributeType) attributeMapping );
					for ( Serializable columnOrFormula : prop.getColumnOrFormula() ) {
						if ( columnOrFormula instanceof JaxbColumn ) {
							( (JaxbColumn) columnOrFormula ).setTable( hbmJoin.getTable() );
						}
					}
					target.getAttributes().getBasic().add( prop );
				}
				else if ( attributeMapping instanceof JaxbHbmCompositeAttributeType ) {
					throw new NotYetImplementedException(
							"transformation of <component/> as part of <join/> (secondary-table) not yet implemented"
					);
				}
				else if ( attributeMapping instanceof JaxbHbmManyToOneType ) {
					throw new NotYetImplementedException(
							"transformation of <many-to-one/> as part of <join/> (secondary-table) not yet implemented"
					);
				}
				else if ( attributeMapping instanceof JaxbHbmAnyAssociationType ) {
					throw new NotYetImplementedException(
							"transformation of <any/> as part of <join/> (secondary-table) not yet implemented"
					);
				}
				else if ( attributeMapping instanceof JaxbHbmDynamicComponentType ) {
					logUnhandledContent(
							"<dynamic-component/> mappings not supported; skipping"
					);
				}
			}
		}
	}

	private JaxbManyToOne transferManyToOneAttribute(JaxbHbmCompositeKeyManyToOneType hbmM2O) {
		final JaxbManyToOne m2o = new JaxbManyToOne();
		m2o.setId( true );
		m2o.setAttributeAccessor( hbmM2O.getAccess() );
		m2o.setFetch( convert( hbmM2O.getLazy() ) );
		m2o.setForeignKey( new JaxbForeignKey() );
		m2o.getForeignKey().setName( hbmM2O.getForeignKey() );
		if (! hbmM2O.getColumn().isEmpty()) {
			for ( JaxbHbmColumnType hbmColumn : hbmM2O.getColumn() ) {
				final JaxbJoinColumn joinColumn = new JaxbJoinColumn();
				joinColumn.setName( hbmColumn.getName() );
				joinColumn.setNullable( hbmColumn.isNotNull() == null ? null : !hbmColumn.isNotNull() );
				joinColumn.setUnique( hbmColumn.isUnique() );
				m2o.getJoinColumn().add( joinColumn );
			}
		}
		else {
			final JaxbJoinColumn joinColumn = new JaxbJoinColumn();
			if ( StringHelper.isEmpty( hbmM2O.getColumnAttribute() )) {
				// AbstractBasicBindingTests seems to imply this was the case
				joinColumn.setName( hbmM2O.getName() );
			}
			else {
				joinColumn.setName( hbmM2O.getColumnAttribute() );
			}
			m2o.getJoinColumn().add( joinColumn );
		}
		m2o.setName( hbmM2O.getName() );
		if ( isNotEmpty( hbmM2O.getEntityName() ) ) {
			m2o.setTargetEntity( hbmM2O.getEntityName() );
		}
		else {
			m2o.setTargetEntity( hbmM2O.getClazz() );
		}
		if (hbmM2O.getOnDelete() != null) {
			m2o.setOnDelete( convert( hbmM2O.getOnDelete() ) );
		}
		return m2o;
	}

	private void transferUnionSubclass(JaxbHbmUnionSubclassEntityType hbmSubclass, JaxbEntity subclassEntity) {
		if (! StringHelper.isEmpty( hbmSubclass.getProxy() )) {
			// TODO
			throw new MappingException( "HBM transformation: proxy attributes not yet supported" );
		}
		transferBaseEntityInformation( hbmSubclass, subclassEntity );
		transferEntityElementAttributes( hbmSubclass, subclassEntity );
		
		subclassEntity.setTable( new JaxbTable() );
		subclassEntity.getTable().setCatalog( hbmSubclass.getCatalog() );
		subclassEntity.getTable().setSchema( hbmSubclass.getSchema() );
		subclassEntity.getTable().setName( hbmSubclass.getTable() );
		subclassEntity.getTable().setComment( hbmSubclass.getComment() );
		subclassEntity.getTable().setCheck( hbmSubclass.getCheck() );
		
		if ( !hbmSubclass.getUnionSubclass().isEmpty() ) {
			subclassEntity.setInheritance( new JaxbInheritance() );
			subclassEntity.getInheritance().setStrategy( JaxbInheritanceType.UNION_SUBCLASS );
			for ( JaxbHbmUnionSubclassEntityType nestedHbmSubclass : hbmSubclass.getUnionSubclass() ) {
				final JaxbEntity nestedSubclassEntity = new JaxbEntity();
				ormRoot.getEntity().add( nestedSubclassEntity );
				transferUnionSubclass( nestedHbmSubclass, nestedSubclassEntity );
			}
		}
	}

	
	// ToOne
	private void transferFetchable(
			JaxbHbmLazyWithNoProxyEnum hbmLazy,
			JaxbHbmFetchStyleEnum hbmFetch,
			JaxbHbmOuterJoinEnum hbmOuterJoin,
			Boolean constrained,
			FetchableAttribute fetchable) {
		FetchType laziness = FetchType.LAZY;
		FetchMode fetch = FetchMode.SELECT;
		
		if (hbmLazy != null) {
			if (hbmLazy.equals( JaxbHbmLazyWithNoProxyEnum.FALSE )) {
				laziness = FetchType.EAGER;
			}
			else if (hbmLazy.equals( JaxbHbmLazyWithNoProxyEnum.NO_PROXY )) {
				// TODO: @LazyToOne(LazyToOneOption.PROXY) or @LazyToOne(LazyToOneOption.NO_PROXY)
			}
		}
		
		// allow fetch style to override laziness, if necessary
		if (constrained != null && ! constrained) {
			// NOTE SPECIAL CASE: one-to-one constrained=false cannot be proxied, so default to join and non-lazy
			laziness = FetchType.EAGER;
			fetch = FetchMode.JOIN;
		}
		else {
			if (hbmFetch == null) {
				if (hbmOuterJoin != null && hbmOuterJoin.equals( JaxbHbmOuterJoinEnum.TRUE )) {
					laziness = FetchType.EAGER;
					fetch = FetchMode.JOIN;
				}
			}
			else {
				if (hbmFetch.equals( JaxbHbmFetchStyleEnum.JOIN )) {
					laziness = FetchType.EAGER;
					fetch = FetchMode.JOIN;
				}
			}
		}
		
		fetchable.setFetch( laziness );
		fetchable.setHbmFetchMode( fetch );
	}
	
	// ToMany
	private void transferFetchable(
			JaxbHbmLazyWithExtraEnum hbmLazy,
			JaxbHbmFetchStyleWithSubselectEnum hbmFetch,
			JaxbHbmOuterJoinEnum hbmOuterJoin,
			FetchableAttribute fetchable) {
		FetchType laziness = FetchType.LAZY;
		FetchMode fetch = FetchMode.SELECT;
		
		if (hbmLazy != null) {
			if (hbmLazy.equals( JaxbHbmLazyWithExtraEnum.EXTRA )) {
				throw new MappingException( "HBM transformation: extra lazy not yet supported." );
			}
			else if (hbmLazy.equals( JaxbHbmLazyWithExtraEnum.FALSE )) {
				laziness = FetchType.EAGER;
			}
		}
		
		// allow fetch style to override laziness, if necessary
		if (hbmFetch == null) {
			if (hbmOuterJoin != null && hbmOuterJoin.equals( JaxbHbmOuterJoinEnum.TRUE )) {
				laziness = FetchType.EAGER;
				fetch = FetchMode.JOIN;
			}
		}
		else {
			if (hbmFetch.equals( JaxbHbmFetchStyleWithSubselectEnum.JOIN )) {
				laziness = FetchType.EAGER;
				fetch = FetchMode.JOIN;
			}
			else if (hbmFetch.equals( JaxbHbmFetchStyleWithSubselectEnum.SUBSELECT )) {
				fetch = FetchMode.SUBSELECT;
			}
		}
		
		fetchable.setFetch( laziness );
		fetchable.setHbmFetchMode( fetch );
	}
	
	// KeyManyToOne
	private FetchType convert(JaxbHbmLazyEnum hbmLazy) {
		if ( hbmLazy != null && "false".equalsIgnoreCase( hbmLazy.value() ) ) {
			return FetchType.EAGER;
		}
		else {
			// proxy is HBM default
			return FetchType.LAZY;
		}
	}
	
	private JaxbOnDeleteType convert(JaxbHbmOnDeleteEnum hbmOnDelete) {
		switch (hbmOnDelete) {
			case CASCADE:
				return JaxbOnDeleteType.CASCADE;
			default:
				return JaxbOnDeleteType.NO_ACTION;
		}
	}
	
	private JaxbHbmFilter convert(JaxbHbmFilterType hbmFilter) {
		final JaxbHbmFilter filter = new JaxbHbmFilter();
		filter.setName( hbmFilter.getName() );

		final boolean shouldAutoInjectAliases = hbmFilter.getAutoAliasInjection() == null
				|| hbmFilter.getAutoAliasInjection().equalsIgnoreCase( "true" );

		filter.setAutoAliasInjection( shouldAutoInjectAliases );
		filter.setCondition( hbmFilter.getCondition() );

		for ( Serializable content : hbmFilter.getContent() ) {
			if ( content instanceof String ) {
				filter.setCondition( (String) content );
			}
			else {
				final JaxbHbmFilterAliasMappingType hbmAliasMapping = (JaxbHbmFilterAliasMappingType) content;
				final JaxbHbmFilter.JaxbAliasMapping aliasMapping = new JaxbHbmFilter.JaxbAliasMapping();
				aliasMapping.setAlias( hbmAliasMapping.getAlias() );
				aliasMapping.setEntity( hbmAliasMapping.getEntity() );
				aliasMapping.setTable( hbmAliasMapping.getTable() );
				filter.getAliasMapping().add( aliasMapping );
			}
		}

		return filter;
	}
	
	private JaxbHbmCascadeType convertCascadeType(String s) {
		final JaxbHbmCascadeType cascadeType = new JaxbHbmCascadeType();
		
		if ( isNotEmpty( s ) ) {
			s = s.toLowerCase( Locale.ROOT ).replaceAll( " ", "" );
			final String[] split = s.split( "," );
			for (String hbmCascade : split) {
				if (hbmCascade.contains( "all" )) {
					cascadeType.setCascadeAll( new JaxbEmptyType() );
				}
				if (hbmCascade.contains( "persist" )) {
					cascadeType.setCascadePersist( new JaxbEmptyType() );
				}
				if (hbmCascade.contains( "merge" )) {
					cascadeType.setCascadeMerge( new JaxbEmptyType() );
				}
				if (hbmCascade.contains( "refresh" )) {
					cascadeType.setCascadeRefresh( new JaxbEmptyType() );
				}
				if (hbmCascade.contains( "save-update" )) {
					cascadeType.setCascadeSaveUpdate( new JaxbEmptyType() );
				}
				if (hbmCascade.contains( "evict" ) || hbmCascade.contains( "detach" )) {
					cascadeType.setCascadeDetach( new JaxbEmptyType() );
				}
				if (hbmCascade.contains( "replicate" )) {
					cascadeType.setCascadeReplicate( new JaxbEmptyType() );
				}
				if (hbmCascade.contains( "lock" )) {
					cascadeType.setCascadeLock( new JaxbEmptyType() );
				}
				if (hbmCascade.contains( "delete" )) {
					cascadeType.setCascadeDelete( new JaxbEmptyType() );
				}
			}
		}
		return cascadeType;
	}
	
	private boolean isOrphanRemoval(String s) {
		return isNotEmpty( s )
				&& s.toLowerCase( Locale.ROOT ).contains( "orphan" );
	}
	
	private String getFullyQualifiedClassName(String className) {
		// todo : right now we do both, we set the package into the XML and qualify the names; pick one...
		//		1) pass the names through as-is and set the package into the XML; the orm.xml reader
		//			would apply the package as needed
		//		2) qualify the name that we write into the XML, but the do not set the package into the XML;
		//			if going this route, would be better to leverage the normal hierarchical lookup for package
		// 			names which would mean passing along MappingDefaults (or maybe even the full "binding context")

		final String defaultPackageName = ormRoot.getPackage();
		if ( isNotEmpty( className )
				&& className.indexOf( '.' ) < 0
				&& isNotEmpty( defaultPackageName ) ) {
			className = StringHelper.qualify( defaultPackageName, className );
		}
		return className;
	}

}
