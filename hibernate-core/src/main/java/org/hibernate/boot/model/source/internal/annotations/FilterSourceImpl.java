/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.model.source.spi.FilterSource;
import org.hibernate.internal.util.StringHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

/**
 * @author Steve Ebersole
 */
public class FilterSourceImpl implements FilterSource {
	private final String name;
	private final String condition;
	private final boolean autoAliasInjection;
	private final Map<String, String> aliasTableMap = new HashMap<String, String>();
	private final Map<String, String> aliasEntityMap = new HashMap<String, String>();

	public FilterSourceImpl(AnnotationInstance filterAnnotation) {
		this.name = extractString( filterAnnotation, "name" );
		this.condition = extractString( filterAnnotation, "condition" );
		this.autoAliasInjection = extractBoolean( filterAnnotation, "deduceAliasInjectionPoints", true );

		final AnnotationValue aliasesValue = filterAnnotation.value( "aliases" );
		if ( aliasesValue == null ) {
			return;
		}

		final AnnotationInstance[] aliasAnnotations = aliasesValue.asNestedArray();
		for ( AnnotationInstance aliasAnnotation : aliasAnnotations ) {
			final String alias = extractString( aliasAnnotation, "alias" );
			final String table = extractString( aliasAnnotation, "table" );
			final String entity = extractString( aliasAnnotation, "entity" );

			assert StringHelper.isNotEmpty( alias );

			if ( StringHelper.isNotEmpty( table ) ) {
				aliasTableMap.put( alias, table );
			}
			else if ( StringHelper.isNotEmpty( entity ) ) {
				aliasEntityMap.put( alias, entity );
			}
			else {
				// todo : throw a mapping exception
			}
		}
	}

	private static String extractString(AnnotationInstance annotation, String name) {
		final AnnotationValue value = annotation.value( name );
		if ( value == null ) {
			return null;
		}
		return StringHelper.nullIfEmpty( value.asString() );
	}

	private static boolean extractBoolean(AnnotationInstance annotation, String name, boolean defaultValue) {
		final AnnotationValue value = annotation.value( name );
		if ( value == null ) {
			return defaultValue;
		}
		return value.asBoolean();
	}

	public String getName() {
		return name;
	}

	public String getCondition() {
		return condition;
	}

	public boolean shouldAutoInjectAliases() {
		return autoAliasInjection;
	}

	public Map<String, String> getAliasToTableMap() {
		return aliasTableMap;
	}

	public Map<String, String> getAliasToEntityMap() {
		return aliasEntityMap;
	}
}
