/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import java.util.List;
import java.util.Map;

import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.spi.JpaCallbackSource;
import org.hibernate.boot.spi.MetadataBuildingContext;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;

/**
 * @author Steve Ebersole
 */
public interface AnnotationBindingContext extends MetadataBuildingContext {
	RootAnnotationBindingContext getRootAnnotationBindingContext();

	IndexView getJandexIndex();
	Map<DotName,AnnotationInstance> getTypeAnnotationInstances(DotName name);
	Map<DotName,AnnotationInstance> getMemberAnnotationInstances(MemberDescriptor memberDescriptor);
	<T> TypedValueExtractor<T> getTypedValueExtractor(Class<T> type);

	IdentifierGeneratorDefinition getIdentifierGeneratorDefinition(String name);

	List<JpaCallbackSource> getDefaultEntityListeners();
}
