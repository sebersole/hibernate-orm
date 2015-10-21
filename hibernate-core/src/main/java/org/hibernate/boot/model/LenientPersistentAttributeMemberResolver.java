/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */

/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2014, Red Hat Inc. or third-party contributors as
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
package org.hibernate.boot.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import javax.persistence.AccessType;

import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.internal.util.ReflectHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

/**
 * PersistentAttributeMemberResolver implementation that is more lenient in some cases
 * than the JPA specification with the idea of being more user friendly.
 *
 * @author Steve Ebersole
 */
public class LenientPersistentAttributeMemberResolver extends AbstractPersistentAttributeMemberResolver {
	private static final Logger LOG = Logger.getLogger( LenientPersistentAttributeMemberResolver.class );

	/**
	 * Singleton access
	 */
	public static final LenientPersistentAttributeMemberResolver INSTANCE = new LenientPersistentAttributeMemberResolver();

	@Override
	protected List<MemberDescriptor> resolveAttributesMembers(
			Set<String> transientFieldNames,
			Set<String> transientMethodNames,
			ClassInfo classInfo,
			AccessType classLevelAccessType,
			EntityBindingContext bindingContext) {
		final LinkedHashMap<String,MemberDescriptor> attributeMemberMap = new LinkedHashMap<String, MemberDescriptor>();

		collectAnnotatedMembers(
				attributeMemberMap,
				transientFieldNames,
				transientMethodNames,
				classInfo,
				bindingContext
		);

		collectNonAnnotatedMembers(
				attributeMemberMap,
				transientFieldNames,
				transientMethodNames,
				classInfo,
				bindingContext
		);

		return new ArrayList<MemberDescriptor>( attributeMemberMap.values() );
	}

	private void collectAnnotatedMembers(
			LinkedHashMap<String,MemberDescriptor> attributeMemberMap,
			Set<String> transientFieldNames,
			Set<String> transientMethodNames,
			ClassInfo classInfo,
			EntityBindingContext bindingContext) {
		for ( List<AnnotationInstance> annotationInstances : classInfo.annotations().values() ) {
			for ( AnnotationInstance annotationInstance : annotationInstances ) {
				final String annotationTypeName = annotationInstance.name().toString();
				if ( !annotationTypeName.startsWith( "javax.persistence." )
						&& !annotationTypeName.startsWith( "org.hibernate.annotations." ) ) {
					continue;
				}

				final AnnotationTarget target = annotationInstance.target();
				if ( FieldInfo.class.isInstance( target ) ) {
					final FieldInfo field = (FieldInfo) target;

					if ( transientFieldNames.contains( field.name() ) ) {
						continue;
					}

					if ( !isPersistable( field ) ) {
						continue;
					}

					final MemberDescriptor existing = attributeMemberMap.get( field.name() );
					if ( existing != null ) {
						if ( existing.kind() != MemberDescriptor.Kind.FIELD ) {
							LOG.warnf(
									"Found annotations split between field [%s] and method [%s]",
									field.name(),
									existing.sourceName()
							);
						}
						continue;
					}

					attributeMemberMap.put( field.name(), makeMemberDescriptor( field, bindingContext ) );
				}
				else if ( MethodInfo.class.isInstance( target ) ) {
					final MethodInfo method = (MethodInfo) target;
					if ( transientMethodNames.contains( method.name() ) ) {
						continue;
					}

					final String attributeName = ReflectHelper.getPropertyNameFromGetterMethod( method.name() );
					final MemberDescriptor existing = attributeMemberMap.get( attributeName );

					if ( !isPersistable( method ) ) {
						 continue;
					}

					if ( existing != null ) {
						if ( existing.kind() != MemberDescriptor.Kind.METHOD ) {
							LOG.warnf(
									"Found annotations split between field [%s] and method [%s]",
									existing.sourceName(),
									method.name()
							);
						}
						continue;
					}

					attributeMemberMap.put(
							attributeName,
							makeMemberDescriptor( method, attributeName, bindingContext )
					);
				}
			}
		}
	}

	private void collectNonAnnotatedMembers(
			LinkedHashMap<String, MemberDescriptor> attributeMemberMap,
			Set<String> transientFieldNames,
			Set<String> transientMethodNames,
			ClassInfo classInfo,
			EntityBindingContext bindingContext) {
		for ( MethodInfo method : classInfo.methods() ) {
			if ( !isPersistable( method ) ) {
				continue;
			}

			if ( transientMethodNames.contains( method.name() ) ) {
				continue;
			}

			final String attributeName = ReflectHelper.getPropertyNameFromGetterMethod( method.name() );
			if ( attributeMemberMap.containsKey( attributeName ) ) {
				continue;
			}

			attributeMemberMap.put( attributeName, makeMemberDescriptor( method, attributeName, bindingContext ) );
		}

		for ( FieldInfo field : classInfo.fields() ) {
			if ( !isPersistable( field ) ) {
				continue;
			}

			if ( transientFieldNames.contains( field.name() ) ) {
				continue;
			}

			if ( attributeMemberMap.containsKey( field.name() ) ) {
				continue;
			}

			attributeMemberMap.put( field.name(), makeMemberDescriptor( field, bindingContext ) );
		}
	}
}
