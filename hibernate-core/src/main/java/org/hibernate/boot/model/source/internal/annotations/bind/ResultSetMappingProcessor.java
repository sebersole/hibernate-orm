/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.bind;

import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;

/**
 * @author Steve Ebersole
 */
public class ResultSetMappingProcessor {
	public static void bind(RootAnnotationBindingContext bindingContext) {
		// here we just add the second pass.  All real binding happens in the second pass
		bindingContext.getMetadataCollector().addSecondPass(
				new AggregatedResultSetMappingSecondPass( bindingContext ),
				true
		);
	}
}
