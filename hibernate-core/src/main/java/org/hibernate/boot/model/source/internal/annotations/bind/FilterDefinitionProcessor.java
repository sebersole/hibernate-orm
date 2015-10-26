/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.bind;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.util.AnnotationBindingHelper;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.type.Type;

import org.jboss.jandex.AnnotationInstance;

/**
 * @author Steve Ebersole
 */
public class FilterDefinitionProcessor {
	public static void bind(RootAnnotationBindingContext bindingContext) {
		final List<AnnotationInstance> filterDefAnnotations = AnnotationBindingHelper.collectionAnnotations(
				bindingContext.getJandexIndex(),
				HibernateDotNames.FILTER_DEF,
				HibernateDotNames.FILTER_DEFS
		);
		for ( AnnotationInstance filterDefAnnotation : filterDefAnnotations ) {
			bindingContext.getMetadataCollector().addFilterDefinition(
					makeFilterDefinition( filterDefAnnotation, bindingContext )
			);
		}
	}

	private static FilterDefinition makeFilterDefinition(AnnotationInstance filterDefAnnotation, RootAnnotationBindingContext bindingContext) {
		final String filterName = bindingContext.stringExtractor.extract( filterDefAnnotation, "name" );
		final String defaultCondition = bindingContext.stringExtractor.extract( filterDefAnnotation, "defaultCondition" );

		final Map<String, Type> parameterTypeMap;
		final AnnotationInstance[] paramAnnotations =
				bindingContext.nestedArrayExtractor.extract( filterDefAnnotation, "parameters" );
		if ( paramAnnotations == null || paramAnnotations.length == 0 ) {
			parameterTypeMap = Collections.emptyMap();
		}
		else {
			parameterTypeMap = new HashMap<String, Type>(  );
			for ( AnnotationInstance paramAnnotation : paramAnnotations ) {
				final String name = bindingContext.stringExtractor.extract( paramAnnotation, "name" );
				final String typeName = bindingContext.stringExtractor.extract( paramAnnotation, "type" );
				final Type type = bindingContext.getMetadataCollector().getTypeResolver().heuristicType(
						typeName
				);
				parameterTypeMap.put( name, type );
			}
		}

		return new FilterDefinition( filterName, defaultCondition, parameterTypeMap );

	}
}
