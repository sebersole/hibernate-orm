/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.bind.internal;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.boot.annotations.source.spi.AnnotationTarget;
import org.hibernate.boot.annotations.source.spi.HibernateAnnotations;
import org.hibernate.boot.annotations.source.spi.JpaAnnotations;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;

import static org.hibernate.boot.annotations.source.spi.HibernateAnnotations.GENERIC_GENERATOR;
import static org.hibernate.boot.annotations.source.spi.JpaAnnotations.NAMED_ENTITY_GRAPH;
import static org.hibernate.boot.annotations.source.spi.JpaAnnotations.SEQUENCE_GENERATOR;
import static org.hibernate.boot.annotations.source.spi.JpaAnnotations.TABLE_GENERATOR;

/**
 * Processes "global" annotations which can be applied at a number of levels,
 * but are always considered global in scope
 *
 * @author Steve Ebersole
 */
public class GlobalAnnotationProcessor {
	private final AnnotationProcessingContext processingContext;
	private final Set<String> processedGlobalAnnotationSources = new HashSet<>();

	public GlobalAnnotationProcessor(AnnotationProcessingContext processingContext) {
		this.processingContext = processingContext;
	}

	public void processGlobalAnnotation(AnnotationTarget annotationTarget) {
		if ( processedGlobalAnnotationSources.contains( annotationTarget.getName() ) ) {
			return;
		}
		processedGlobalAnnotationSources.add( annotationTarget.getName() );

		TypeContributionProcessor.processTypeContributions( annotationTarget, processingContext );
		processGenerators( annotationTarget );
		processNamedQueries( annotationTarget );
		processNamedEntityGraphs( annotationTarget );
		processFilterDefinitions( annotationTarget );
	}

	private void processGenerators(AnnotationTarget annotationTarget) {
		processSequenceGenerators( annotationTarget );
		processTableGenerators( annotationTarget );
		processGenericGenerators( annotationTarget );
	}

	private void processSequenceGenerators(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( SEQUENCE_GENERATOR, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processTableGenerators(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( TABLE_GENERATOR, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processGenericGenerators(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( GENERIC_GENERATOR, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processNamedQueries(AnnotationTarget annotationTarget) {
		processNamedQuery( annotationTarget );
		processNamedNativeQuery( annotationTarget );
		processNamedProcedureQuery( annotationTarget );
	}

	private void processNamedQuery(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JpaAnnotations.NAMED_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );

		annotationTarget.forEachAnnotation( HibernateAnnotations.NAMED_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processNamedNativeQuery(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JpaAnnotations.NAMED_NATIVE_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );

		annotationTarget.forEachAnnotation( HibernateAnnotations.NAMED_NATIVE_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}


	private void processNamedProcedureQuery(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( JpaAnnotations.NAMED_STORED_PROCEDURE_QUERY, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processNamedEntityGraphs(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( NAMED_ENTITY_GRAPH, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}

	private void processFilterDefinitions(AnnotationTarget annotationTarget) {
		annotationTarget.forEachAnnotation( HibernateAnnotations.FILTER_DEF, (usage) -> {
			// todo (annotation-source) : implement
		} );
	}
}
