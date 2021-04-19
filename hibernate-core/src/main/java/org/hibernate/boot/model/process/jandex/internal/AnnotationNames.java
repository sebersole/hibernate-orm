/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.process.jandex.internal;

import org.hibernate.annotations.BatchSize;

import org.jboss.jandex.DotName;

/**
 * @author Steve Ebersole
 */
public interface AnnotationNames {
	DotName BatchSize = DotName.createSimple( org.hibernate.annotations.BatchSize.class.getName() );
}
