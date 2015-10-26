/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

/**
 * @author Strong Liu <stliu@hibernate.org>
 */
public class PrimaryKeyJoinColumn extends Column {
	private String referencedColumnName;

	public PrimaryKeyJoinColumn(AnnotationInstance columnAnnotation) {
		super( columnAnnotation );
	}

	@Override
	public void applyColumnValues(AnnotationInstance columnAnnotation) {
		super.applyColumnValues( columnAnnotation );
		if ( columnAnnotation != null ) {
			AnnotationValue nameValue = columnAnnotation.value( "referencedColumnName" );
			if ( nameValue != null ) {
				this.referencedColumnName = nameValue.asString();
			}
		}
	}

	public String getReferencedColumnName() {
		return referencedColumnName;
	}
}
