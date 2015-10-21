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
package org.hibernate.boot.model.source.internal.annotations.metadata.type;

import java.util.List;
import java.util.Map;

import org.hibernate.boot.MappingException;
import org.hibernate.boot.jaxb.Origin;
import org.hibernate.boot.jaxb.SourceType;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.model.MemberDescriptor;
import org.hibernate.boot.model.source.internal.DelegatingMetadataBuildingContextImpl;
import org.hibernate.boot.model.source.internal.annotations.AnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.RootAnnotationBindingContext;
import org.hibernate.boot.model.source.internal.annotations.TypedValueExtractor;
import org.hibernate.boot.model.source.spi.JpaCallbackSource;
import org.hibernate.boot.model.source.spi.LocalMetadataBuildingContext;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;


/**
 * Annotation version of a local binding context.
 * 
 * @author Steve Ebersole
 */
public class EntityBindingContext
		extends DelegatingMetadataBuildingContextImpl
		implements LocalMetadataBuildingContext, AnnotationBindingContext {
	private final RootAnnotationBindingContext rootAnnotationBindingContext;
	private final Origin origin;

	public EntityBindingContext(RootAnnotationBindingContext rootAnnotationBindingContext, ManagedTypeMetadata source) {
		super( rootAnnotationBindingContext );
		this.rootAnnotationBindingContext = rootAnnotationBindingContext;
		this.origin = new Origin( SourceType.ANNOTATION, source.getName() );
	}

	@Override
	public RootAnnotationBindingContext getRootAnnotationBindingContext() {
		return rootAnnotationBindingContext;
	}

	@Override
	public Origin getOrigin() {
		return origin;
	}

	public MappingException makeMappingException(String message) {
		return new MappingException( message, getOrigin() );
	}

	public MappingException makeMappingException(String message, Exception cause) {
		return new MappingException( message, cause, getOrigin() );
	}

	@Override
	public IdentifierGeneratorDefinition getIdentifierGeneratorDefinition(String name) {
		return rootAnnotationBindingContext.getIdentifierGeneratorDefinition( name );
	}

	@Override
	public List<JpaCallbackSource> getDefaultEntityListeners() {
		return rootAnnotationBindingContext.getDefaultEntityListeners();
	}

	@Override
	public IndexView getJandexIndex() {
		return rootAnnotationBindingContext.getJandexIndex();
	}

	@Override
	public Map<DotName, AnnotationInstance> getTypeAnnotationInstances(DotName name) {
		return getRootAnnotationBindingContext().getTypeAnnotationInstances( name );
	}

	@Override
	public Map<DotName, AnnotationInstance> getMemberAnnotationInstances(MemberDescriptor memberDescriptor) {
		return getRootAnnotationBindingContext().getMemberAnnotationInstances( memberDescriptor );
	}

	@Override
	public <T> TypedValueExtractor<T> getTypedValueExtractor(Class<T> type) {
		return getRootAnnotationBindingContext().getTypedValueExtractor( type );
	}

	public String getLoggableContextName() {
		return origin.getName();
	}
}
