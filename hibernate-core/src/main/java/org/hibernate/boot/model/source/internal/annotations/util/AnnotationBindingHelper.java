/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.boot.model.process.jandex.spi.HibernateDotNames;
import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.CustomSql;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.annotations.AnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.IdentifiableTypeMetadata;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.ManagedTypeMetadata;
import org.hibernate.boot.model.source.spi.NaturalIdMutability;
import org.hibernate.engine.spi.ExecuteUpdateResultCheckStyle;
import org.hibernate.internal.util.collections.CollectionHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

/**
 * @author Hardy Ferentschik
 * @author Steve Ebersole
 */
public class AnnotationBindingHelper {
	private static final Logger log = Logger.getLogger( AnnotationBindingHelper.class );

	public static List<AnnotationInstance> collectionAnnotations(
			IndexView indexView,
			DotName singularDotName,
			DotName pluralDotName) {
		final List<AnnotationInstance> results = new ArrayList<AnnotationInstance>();

		results.addAll( indexView.getAnnotations( singularDotName ) );

		for ( AnnotationInstance pluralForm : indexView.getAnnotations( pluralDotName ) ) {
			if ( pluralForm.value() != null ) {
				results.addAll( Arrays.asList( pluralForm.value().asNestedArray() ) );
			}
		}
		return results;
	}

	/**
	 * Is class represented by `target` assignable from class represented by `source`?
	 *
	 * @return {@code true} if `target` is assignable from `source`; {@code false} otherwise.
	 */
	public static boolean isAssignableFrom(ClassInfo target, ClassInfo source, AnnotationBindingContext bindingContext) {
		if ( target == null ) {
			throw new NullPointerException( "Passed `target` cannot be null" );
		}

		if ( source == null ) {
			log.debug( "`source` passed to AnnotationBindingHelper#isAssignableFrom was null" );
			return false;
		}

		// first check the types directly
		if ( target.name().equals( source.name() ) ) {
			return true;
		}

		// next look at interfaces
		for ( DotName dotName : source.interfaceNames() ) {
			final ClassInfo interfaceInfo = bindingContext.getJandexIndex().getClassByName( dotName );
			if ( isAssignableFrom( target, interfaceInfo, bindingContext ) ) {
				return true;
			}
		}

		// and then the supertype
		if ( source.superName() != null ) {
			final ClassInfo superInfo = bindingContext.getJandexIndex().getClassByName( source.superName() );
			if ( isAssignableFrom( target, superInfo, bindingContext ) ) {
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("SimplifiableIfStatement" )
	public static boolean isEmbeddableType(ClassInfo classInfo, AnnotationBindingContext bindingContext) {
		if ( classInfo == null ) {
			return false;
		}
		return isEmbeddableType( classInfo.name(), bindingContext );
	}

	@SuppressWarnings("SimplifiableIfStatement" )
	public static boolean isEmbeddableType(Type type, AnnotationBindingContext bindingContext) {
		if ( type == null ) {
			return false;
		}
		return isEmbeddableType( type.name(), bindingContext );
	}

	@SuppressWarnings("SimplifiableIfStatement" )
	public static boolean isEmbeddableType(DotName name, AnnotationBindingContext bindingContext) {
		final Map<DotName, AnnotationInstance> annotationMap = bindingContext.getTypeAnnotationInstances( name );
		if ( annotationMap == null ) {
			return false;
		}
		return annotationMap.containsKey( JpaDotNames.EMBEDDABLE );
	}

	public static java.util.Collection<AnnotationInstance> getCombinedAnnotations(
			Map<DotName, AnnotationInstance> annotations,
			DotName singularDotName,
			DotName pluralDotName,
			AnnotationBindingContext bindingContext) {
		List<AnnotationInstance> annotationInstances = new ArrayList<AnnotationInstance>();

		boolean foundSingular = false;
		final AnnotationInstance singular = annotations.get( singularDotName );
		if ( singular != null ) {
			foundSingular = true;
			annotationInstances.add( singular );
		}

		final AnnotationInstance plural = annotations.get( pluralDotName );
		if ( plural != null ) {
			if ( foundSingular ) {
				log.debugf( "Found both singular [%s] and plural [%s] annotations" );
			}
			annotationInstances.addAll(
					Arrays.asList(
							bindingContext.getTypedValueExtractor( AnnotationInstance[].class ).extract(
									plural,
									"value"
							)
					)
			);
		}

		return annotationInstances;
	}

	public static Map<String, String> extractTypeParameters(AnnotationInstance type, EntityBindingContext context) {
		final AnnotationInstance[] paramAnnotations = context.getTypedValueExtractor( AnnotationInstance[].class ).extract(
				type,
				"parameters"
		);
		if ( paramAnnotations == null || paramAnnotations.length == 0 ) {
			return Collections.emptyMap();
		}
		else if ( paramAnnotations.length == 1 ) {
			return Collections.singletonMap(
					paramAnnotations[0].value( "name" ).asString(),
					paramAnnotations[0].value( "value" ).asString()
			);
		}

		final Map<String,String> parameters = CollectionHelper.mapOfSize( paramAnnotations.length );
		for ( AnnotationInstance paramAnnotation : paramAnnotations ) {
			parameters.put(
					paramAnnotation.value( "name" ).asString(),
					paramAnnotation.value( "value" ).asString()
			);
		}
		return parameters;
	}

	/**
	 * Performs a first-level extraction of the type parameters
	 *
	 * @param type
	 * @return
	 */
	public static List<ClassInfo> extractTypeParameters(Type type, AnnotationBindingContext bindingContext) {
		final List<ClassInfo> resolvedParameterTypes = new ArrayList<ClassInfo>();
		switch ( type.kind() ) {
			case PARAMETERIZED_TYPE: {
				ParameterizedType parameterizedType = type.asParameterizedType();
				for ( Type parameterType : parameterizedType.arguments() ) {
					resolvedParameterTypes.add(
							bindingContext.getJandexIndex().getClassByName( parameterType.name() )
					);
				}
				break;
			}
			default: {
				// nothing to do
			}
		}

		return resolvedParameterTypes;
	}

	public static NaturalIdMutability determineNaturalIdMutability(
			ManagedTypeMetadata container,
			MemberDescriptor member) {
		final AnnotationInstance naturalIdAnnotation = container.getLocalBindingContext().getMemberAnnotationInstances(
				member
		).get( HibernateDotNames.NATURAL_ID );
		if ( naturalIdAnnotation == null ) {
			return container.getContainerNaturalIdMutability();
		}

		final boolean mutable = naturalIdAnnotation.value( "mutable" ) != null
				&& naturalIdAnnotation.value( "mutable" ).asBoolean();
		return mutable
				? NaturalIdMutability.MUTABLE
				: NaturalIdMutability.IMMUTABLE;
	}

	public static CustomSql extractCustomSql(AnnotationInstance customSqlAnnotation) {
		if ( customSqlAnnotation == null ) {
			return null;
		}

		final String sql = customSqlAnnotation.value( "sql" ).asString();
		final boolean isCallable = customSqlAnnotation.value( "callable" ) != null
				&& customSqlAnnotation.value( "callable" ).asBoolean();

		final ExecuteUpdateResultCheckStyle checkStyle = customSqlAnnotation.value( "check" ) == null
				? isCallable
				? ExecuteUpdateResultCheckStyle.NONE
				: ExecuteUpdateResultCheckStyle.COUNT
				: ExecuteUpdateResultCheckStyle.valueOf( customSqlAnnotation.value( "check" ).asEnum() );

		return new CustomSql( sql, isCallable, checkStyle );
	}

	public static AnnotationInstance findFirstNonNull(AnnotationInstance... annotationInstances) {
		if ( annotationInstances == null || annotationInstances.length == 0 ) {
			return null;
		}

		for ( AnnotationInstance annotationInstance : annotationInstances ) {
			if ( annotationInstance != null ) {
				return annotationInstance;
			}
		}

		return null;
	}

	public static AnnotationInstance findTypeAnnotation(DotName annotationName, IdentifiableTypeMetadata typeMetadata) {
		// check locally
		final AnnotationInstance local = typeMetadata.typeAnnotationMap().get( annotationName );
		if ( local != null ) {
			return local;
		}

		// check super, if one
		if ( typeMetadata.getSuperType() != null ) {
			return findTypeAnnotation( annotationName, typeMetadata.getSuperType() );
		}

		// otherwise, there is not one.
		return null;
	}
}
