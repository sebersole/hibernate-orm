/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.bind;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.AnnotationException;
import org.hibernate.boot.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.TypeDefinition;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;

/**
 * @author Steve Ebersole
 */
public class TypeDefinitionProcessor {
	public static void bind(RootAnnotationBindingContext bindingContext) {
		final List<AnnotationInstance> typeDefAnnotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				HibernateDotNames.TYPE_DEF,
				HibernateDotNames.TYPE_DEFS
		);
		for ( AnnotationInstance typeDefAnnotation : typeDefAnnotations ) {
			bindingContext.getMetadataCollector().addTypeDefinition(
					makeTypeDefinition( typeDefAnnotation, bindingContext )
			);
		}
	}

	private static TypeDefinition makeTypeDefinition(AnnotationInstance typeDefAnnotation, RootAnnotationBindingContext bindingContext) {
		final String typeImplName = bindingContext.stringExtractor.extract(
				typeDefAnnotation,
				"typeClass"
		);

		final String typeDefName = bindingContext.stringExtractor.extract( typeDefAnnotation, "name" );
		final String defaultForType = bindingContext.stringExtractor.extract( typeDefAnnotation, "defaultForType" );

		if ( StringHelper.isEmpty( typeDefName ) && defaultForType == null ) {
			throw new AnnotationException(
					String.format(
							Locale.ROOT,
							"@TypeDefinition [typeClass=%s] did not define name nor defaultForType; one or both must be set",
							typeImplName
					)
			);
		}

		return new TypeDefinition(
				typeDefName,
				bindingContext.getBuildingOptions().getServiceRegistry()
						.getService( ClassLoaderService.class )
						.classForName( typeImplName ),
				defaultForType == null ? StringHelper.EMPTY_STRINGS : new String[] { defaultForType },
				extractParameterValues( typeDefAnnotation, bindingContext )
		);
	}

	private static Map<String, String> extractParameterValues(AnnotationInstance typeDefAnnotation, RootAnnotationBindingContext bindingContext) {
		final Map<String, String> parameterMaps = new HashMap<String, String>();
		final AnnotationInstance[] parameterAnnotations = bindingContext.nestedArrayExtractor.extract(
				typeDefAnnotation,
				"parameters"
		);
		for ( AnnotationInstance parameterAnnotation : parameterAnnotations ) {
			parameterMaps.put(
					bindingContext.stringExtractor.extract( parameterAnnotation, "name" ),
					bindingContext.stringExtractor.extract( parameterAnnotation, "value" )
			);
		}
		return parameterMaps;
	}
}
