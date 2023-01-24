/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.annotations.model.internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.boot.annotations.model.spi.LocalAnnotationProcessingContext;
import org.hibernate.boot.annotations.model.spi.PersistentAttributeMemberResolver;
import org.hibernate.boot.annotations.source.spi.ClassDetails;
import org.hibernate.boot.annotations.source.spi.FieldDetails;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.source.spi.MemberDetails;
import org.hibernate.boot.annotations.source.spi.MethodDetails;

import jakarta.persistence.AccessType;

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
	 * @param transientFieldChecker Check whether a field is annotated as @Transient
	 * @param transientMethodChecker Check whether a method is annotated as @Transient
	 * @param classDetails The Jandex ClassInfo describing the type for which to resolve members
	 * @param classLevelAccessType The AccessType determined for the class default
	 * @param processingContext The local context
	 */
	protected abstract List<MemberDetails> resolveAttributesMembers(
			Function<FieldDetails,Boolean> transientFieldChecker,
			Function<MethodDetails,Boolean> transientMethodChecker,
			ClassDetails classDetails,
			AccessType classLevelAccessType,
			LocalAnnotationProcessingContext processingContext);

	@Override
	public List<MemberDetails> resolveAttributesMembers(
			ClassDetails classDetails,
			AccessType classLevelAccessType,
			LocalAnnotationProcessingContext processingContext) {

		final Set<FieldDetails> transientFields = new HashSet<>();
		final Set<MethodDetails> transientMethods = new HashSet<>();
		collectMembersMarkedTransient(
				transientFields::add,
				transientMethods::add,
				classDetails,
				processingContext
		);

		return resolveAttributesMembers(
				transientFields::contains,
				transientMethods::contains,
				classDetails,
				classLevelAccessType,
				processingContext
		);
	}

	protected void collectMembersMarkedTransient(
			final Consumer<FieldDetails> transientFieldConsumer,
			final Consumer<MethodDetails> transientMethodConsumer,
			ClassDetails classDetails,
			@SuppressWarnings("unused") LocalAnnotationProcessingContext processingContext) {
		final List<FieldDetails> fields = classDetails.getFields();
		for ( int i = 0; i < fields.size(); i++ ) {
			final FieldDetails fieldDetails = fields.get( i );
			if ( fieldDetails.getAnnotation( JpaAnnotations.TRANSIENT ) != null ) {
				transientFieldConsumer.accept( fieldDetails );
			}
		}

		final List<MethodDetails> methods = classDetails.getMethods();
		for ( int i = 0; i < methods.size(); i++ ) {
			final MethodDetails methodDetails = methods.get( i );
			if ( methodDetails.getAnnotation( JpaAnnotations.TRANSIENT ) != null ) {
				transientMethodConsumer.accept( methodDetails );
			}
		}
	}

}
