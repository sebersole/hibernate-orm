/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.util;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.GenerationType;

import org.hibernate.AssertionFailure;
import org.hibernate.CacheMode;
import org.hibernate.FetchMode;
import org.hibernate.FlushMode;
import org.hibernate.annotations.CacheModeType;
import org.hibernate.annotations.FlushModeType;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.CascadeStyles;
import org.hibernate.id.MultipleHiLoPerTableGenerator;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.CollectionHelper;

import org.jboss.jandex.AnnotationInstance;

/**
 * Helper class which converts between different enum types.
 *
 * @author Hardy Ferentschik
 * @author Brett Meyer
 */
public class EnumConversionHelper {
	private EnumConversionHelper() {
	}

	public static String generationTypeToGeneratorStrategyName(GenerationType generatorEnum, boolean useNewGeneratorMappings) {
		switch ( generatorEnum ) {
			case IDENTITY:
				return "identity";
			case AUTO:
				return useNewGeneratorMappings
						? "enhanced-sequence"
						: "native";
			case TABLE:
				return useNewGeneratorMappings
						? "enhanced-table"
						: MultipleHiLoPerTableGenerator.class.getName();
			case SEQUENCE:
				return useNewGeneratorMappings
						? "enhanced-sequence"
						: "seqhilo";
		}
		throw new AssertionFailure( "Unknown GeneratorType: " + generatorEnum );
	}

	public static CascadeStyle cascadeTypeToCascadeStyle(CascadeType cascadeType) {
		switch ( cascadeType ) {
			case ALL: {
				return CascadeStyles.ALL;
			}
			case PERSIST: {
				return CascadeStyles.PERSIST;
			}
			case MERGE: {
				return CascadeStyles.MERGE;
			}
			case REMOVE: {
				return CascadeStyles.DELETE;
			}
			case REFRESH: {
				return CascadeStyles.REFRESH;
			}
			case DETACH: {
				return CascadeStyles.EVICT;
			}
			default: {
				throw new AssertionFailure( "Unknown cascade type" );
			}
		}
	}
	
	public static CascadeStyle cascadeTypeToCascadeStyle(
			org.hibernate.annotations.CascadeType cascadeType) {
		switch ( cascadeType ) {
			case ALL: {
				return CascadeStyles.ALL;
			}
			case PERSIST: {
				return CascadeStyles.PERSIST;
			}
			case MERGE: {
				return CascadeStyles.MERGE;
			}
			case REMOVE: {
				return CascadeStyles.DELETE;
			}
			case REFRESH: {
				return CascadeStyles.REFRESH;
			}
			case DETACH: {
				return CascadeStyles.EVICT;
			}
			case DELETE: {
				return CascadeStyles.DELETE;
			}
			case SAVE_UPDATE: {
				return CascadeStyles.UPDATE;
			}
			case REPLICATE: {
				return CascadeStyles.REPLICATE;
			}
			case LOCK: {
				return CascadeStyles.LOCK;
			}
			default: {
				throw new AssertionFailure( "Unknown cascade type: " + cascadeType );
			}
		}
	}

	public static FetchMode annotationFetchModeToHibernateFetchMode(org.hibernate.annotations.FetchMode annotationFetchMode) {
		switch ( annotationFetchMode ) {
			case JOIN: {
				return FetchMode.JOIN;
			}
			case SELECT: {
				return FetchMode.SELECT;
			}
			case SUBSELECT: {
				// todo - is this correct? can the conversion be made w/o any additional information, eg
				// todo - association nature
				return FetchMode.SELECT;
			}
			default: {
				throw new AssertionFailure( "Unknown fetch mode" );
			}
		}
	}

	public static FetchStyle annotationFetchModeToFetchStyle(org.hibernate.annotations.FetchMode annotationFetchMode) {
		switch ( annotationFetchMode ) {
			case JOIN: {
				return FetchStyle.JOIN;
			}
			case SELECT: {
				return FetchStyle.SELECT;
			}
			case SUBSELECT: {
				return FetchStyle.SUBSELECT;
			}
			default: {
				throw new AssertionFailure( "Unknown fetch mode" );
			}
		}
	}

	public static Set<CascadeStyle> cascadeTypeToCascadeStyleSet(
			Set<CascadeType> cascadeTypes,
			Set<org.hibernate.annotations.CascadeType> hibernateCascadeTypes,
			EntityBindingContext context) {
		Set<CascadeStyle> cascadeStyleSet = new HashSet<CascadeStyle>();
		if ( CollectionHelper.isEmpty( cascadeTypes )
				&& CollectionHelper.isEmpty( hibernateCascadeTypes ) ) {
			String cascades = context.getMappingDefaults().getImplicitCascadeStyleName();
			for ( String cascade : StringHelper.split( ",", cascades ) ) {
				cascadeStyleSet.add( CascadeStyles.getCascadeStyle( cascade ) );
			}
		}
		else {
			for ( CascadeType cascadeType : cascadeTypes ) {
				cascadeStyleSet.add( cascadeTypeToCascadeStyle( cascadeType ) );
			}
			for ( org.hibernate.annotations.CascadeType cascadeType : hibernateCascadeTypes ) {
				cascadeStyleSet.add( cascadeTypeToCascadeStyle( cascadeType ) );
			}
		}
		return cascadeStyleSet;
	}

	public static CacheMode cacheModeTypeToCacheMode(CacheModeType cacheModeType, AnnotationInstance location) {
		switch ( cacheModeType ) {
			case GET: {
				return CacheMode.GET;
			}
			case IGNORE: {
				return CacheMode.IGNORE;
			}
			case NORMAL: {
				return CacheMode.NORMAL;
			}
			case PUT: {
				return CacheMode.PUT;
			}
			case REFRESH: {
				return CacheMode.REFRESH;
			}
			default: {
				throw new AssertionFailure(
						"Unknown CacheModeType [" + cacheModeType + "] at " + location.target().toString()
				);
			}
		}
	}

	public static FlushMode flushModeTypeToFlushMode(FlushModeType flushModeType, AnnotationInstance location) {
		switch ( flushModeType ) {
			case ALWAYS: {
				return FlushMode.ALWAYS;
			}
			case AUTO: {
				return FlushMode.AUTO;
			}
			case COMMIT: {
				return FlushMode.COMMIT;
			}
			case MANUAL: {
				return FlushMode.MANUAL;
			}
			case PERSISTENCE_CONTEXT: {
				return null;
			}
			default: {
				throw new AssertionFailure( "Unknown FlushModeType [" + flushModeType + "] at " + location.target().toString() );
			}
		}
	}
}


