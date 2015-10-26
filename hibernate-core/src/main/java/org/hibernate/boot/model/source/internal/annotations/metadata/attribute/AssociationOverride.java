/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.boot.model.process.jandex.spi.JpaDotNames;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

/**
 * @author Strong Liu <stliu@hibernate.org>
 */
public class AssociationOverride extends AbstractOverrideDefinition {

	public AssociationOverride(
			String prefix,
			AnnotationInstance attributeOverrideAnnotation,
			EntityBindingContext bindingContext) {
		super( prefix, attributeOverrideAnnotation, bindingContext );
	}

	@Override
	protected DotName getTargetAnnotation() {
		return JpaDotNames.ASSOCIATION_OVERRIDE;
	}

	@Override
	public void apply(AbstractPersistentAttribute persistentAttribute) {
	}

}
