/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.bind;

import java.util.List;
import java.util.Locale;

import org.hibernate.MappingException;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.FetchProfiles;
import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.mapping.FetchProfile;
import org.hibernate.mapping.MetadataSource;

import org.jboss.jandex.AnnotationInstance;

/**
 * Binds fetch profiles found in annotations.
 *
 * @author Hardy Ferentschik
 */
public class FetchProfileProcessor {

	/**
	 * Binds all {@link FetchProfiles} and {@link org.hibernate.annotations.FetchProfile} annotations to the supplied metadata.
	 *
	 * @param bindingContext the context for annotation binding
	 */
	public static void bind(RootAnnotationBindingContext bindingContext) {
		final List<AnnotationInstance> profileAnnotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				HibernateDotNames.FETCH_PROFILE,
				HibernateDotNames.FETCH_PROFILES
		);
		for ( AnnotationInstance profileAnnotation : profileAnnotations ) {
			bindingContext.getMetadataCollector().addFetchProfile(
					makeFetchProfile( profileAnnotation, bindingContext )
			);
		}
	}

	private static FetchProfile makeFetchProfile(
			AnnotationInstance profileAnnotation,
			RootAnnotationBindingContext bindingContext) {
		final String name = bindingContext.stringExtractor.extract( profileAnnotation, "name" );
		final FetchProfile profile = new FetchProfile( name, MetadataSource.ANNOTATIONS );

		for ( AnnotationInstance fetchOverride :
				bindingContext.nestedArrayExtractor.extract( profileAnnotation, "fetchOverrides" ) ) {
			final FetchMode fetchMode = bindingContext.getTypedValueExtractor( FetchMode.class ).extract(
					fetchOverride,
					"mode"
			);
			if ( !fetchMode.equals( org.hibernate.annotations.FetchMode.JOIN ) ) {
				throw new MappingException( "Only FetchMode.JOIN is currently supported" );
			}

			final String entityName = bindingContext.stringExtractor.extract( fetchOverride, "entity" );
			final String associationName = bindingContext.stringExtractor.extract( fetchOverride, "association" );

//			// this same check actually already occurs during second pass
//
//			// these checks assume this happens after binding
//			// another option is to pre-build the metadata/Source model and validate using that
//			final PersistentClass entityBinding = bindingContext.getMetadataCollector().getEntityBinding( entityName );
//			if ( entityBinding == null ) {
//				throw new MappingException( "FetchProfile " + name + " references an unknown entity: " + entityName );
//			}
//			final Property attribute = entityBinding.getProperty( associationName );
//			if ( attribute == null ) {
//				throw new MappingException( "FetchProfile " + name + " references an unknown association: " + associationName );
//			}

			profile.addFetch( entityName, associationName, fetchMode.toString().toLowerCase( Locale.ROOT ) );
		}

		return profile;
	}


	private FetchProfileProcessor() {
	}
}
