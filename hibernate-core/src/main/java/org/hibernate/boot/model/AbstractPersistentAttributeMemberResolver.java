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

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.AccessType;

import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.source.internal.ModifierUtils;
import org.hibernate.boot.model.source.internal.annotations.AnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

/**
 * "Template" support for writing PersistentAttributeMemberResolver
 * implementations.
 *
 * @author Steve Ebersole
 */
public abstract class AbstractPersistentAttributeMemberResolver implements PersistentAttributeMemberResolver {

	/**
	 * This is the call that represents the bulk of the work needed to resolve
	 * the persistent attribute members.  It is the strategy specific portion
	 * for sure.
	 * <p/>
	 * The expectation is to
	 * Here is the call that most likely changes per strategy.  This occurs
	 * immediately after we have determined all the fields and methods marked as
	 * transient.  The expectation is to
	 *
	 * @param transientFieldNames The set of all field names found to have been
	 * annotated as @Transient
	 * @param transientMethodNames The set of all method names found to have been
	 * annotated as @Transient
	 * @param classInfo The Jandex ClassInfo describing the type for which to resolve members
	 * @param classLevelAccessType The AccessType determined for the class default
	 * @param bindingContext The local binding context
	 */
	protected abstract List<MemberDescriptor> resolveAttributesMembers(
			Set<String> transientFieldNames,
			Set<String> transientMethodNames,
			ClassInfo classInfo,
			AccessType classLevelAccessType,
			EntityBindingContext bindingContext);

	@Override
	public List<MemberDescriptor> resolveAttributesMembers(
			ClassInfo classInfo,
			AccessType classLevelAccessType,
			EntityBindingContext bindingContext) {

		final Set<String> transientFieldNames = new HashSet<String>();
		final Set<String> transientMethodNames = new HashSet<String>();
		collectMembersMarkedTransient(
				transientFieldNames,
				transientMethodNames,
				classInfo,
				bindingContext
		);

		return resolveAttributesMembers(
				transientFieldNames,
				transientMethodNames,
				classInfo,
				classLevelAccessType,
				bindingContext
		);
	}

	protected void collectMembersMarkedTransient(
			Set<String> transientFieldNames,
			Set<String> transientMethodNames,
			ClassInfo classInfo,
			EntityBindingContext bindingContext) {
		final List<AnnotationInstance> transientAnnotationInstances = classInfo.annotations().get( JpaDotNames.TRANSIENT );
		if ( transientAnnotationInstances == null || transientAnnotationInstances.isEmpty() ) {
			return;
		}

		for ( AnnotationInstance transientAnnotationInstance : transientAnnotationInstances ) {
			final AnnotationTarget transientMember = transientAnnotationInstance.target();

			// todo : we could limit these to "persistable" fields/methods, but not sure its worth the processing to check..

			if ( transientMember instanceof FieldInfo ) {
				transientFieldNames.add( ( (FieldInfo) transientMember ).name() );
			}
			else if ( transientMember instanceof MethodInfo ) {
				transientMethodNames.add( ( (MethodInfo) transientMember ).name() );
			}
			else {
				throw bindingContext.makeMappingException(
						"@Transient should only be defined on field or method : " + transientMember
				);
			}
		}
	}

	@SuppressWarnings("RedundantIfStatement")
	public static boolean isPersistable(FieldInfo fieldInfo) {
		if ( Modifier.isTransient( fieldInfo.flags() ) ) {
			return false;
		}

		if ( ModifierUtils.isSynthetic( fieldInfo ) ) {
			return false;
		}

		return true;
	}

	@SuppressWarnings("RedundantIfStatement")
	public static boolean isPersistable(MethodInfo methodInfo) {
		if ( !methodInfo.parameters().isEmpty() ) {
			return false;
		}

		if ( methodInfo.returnType() == null
				|| methodInfo.returnType().kind() == Type.Kind.VOID ) {
			return false;
		}

		if ( !methodInfo.name().startsWith( "get" )
				&& !methodInfo.name().startsWith( "is"  ) ) {

			return false;
		}

		if ( Modifier.isStatic( methodInfo.flags() ) ) {
			return false;
		}

		if ( ModifierUtils.isBridge( methodInfo ) ) {
			return false;
		}

		if ( ModifierUtils.isSynthetic( methodInfo ) ) {
			return false;
		}

		return true;
	}

	protected MemberDescriptor makeMemberDescriptor(final FieldInfo fieldInfo, final AnnotationBindingContext context) {
		return new MemberDescriptor() {
			@Override
			public Kind kind() {
				return Kind.FIELD;
			}

			@Override
			public String attributeName() {
				return fieldInfo.name();
			}

			@Override
			public String sourceName() {
				return fieldInfo.name();
			}

			@Override
			public ClassInfo declaringClass() {
				return fieldInfo.declaringClass();
			}

			@Override
			public Type type() {
				return fieldInfo.type();
			}

			@Override
			public List<AnnotationInstance> annotations() {
				return fieldInfo.annotations();
			}

			@Override
			public MethodInfo asMethod() {
				throw new IllegalStateException( "Underlying member is a field, not a method" );
			}

			@Override
			public FieldInfo asField() {
				return fieldInfo;
			}

			@Override
			public String toString() {
				return fieldInfo.toString();
			}
		};
	}

	protected MemberDescriptor makeMemberDescriptor(final MethodInfo methodInfo, final String attributeName, final AnnotationBindingContext context) {
		return new MemberDescriptor() {
			@Override
			public Kind kind() {
				return Kind.METHOD;
			}

			@Override
			public String attributeName() {
				return attributeName;
			}

			@Override
			public String sourceName() {
				return methodInfo.name();
			}

			@Override
			public ClassInfo declaringClass() {
				return methodInfo.declaringClass();
			}

			@Override
			public Type type() {
				return methodInfo.returnType();
			}

			@Override
			public List<AnnotationInstance> annotations() {
				return methodInfo.annotations();
			}

			@Override
			public MethodInfo asMethod() {
				return methodInfo;
			}

			@Override
			public FieldInfo asField() {
				throw new IllegalStateException( "Underlying member is a method, not a field" );
			}

			@Override
			public String toString() {
				return methodInfo.toString();
			}
		};
	}
}
