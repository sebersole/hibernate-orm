/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2012, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.boot.model.source.internal.annotations.impl;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.model.source.internal.annotations.FilterSource;
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
