/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.model.internal;

import org.hibernate.AssertionFailure;
import org.hibernate.boot.annotations.model.spi.AttributeMetadata;
import org.hibernate.boot.annotations.source.spi.AnnotationDescriptor;
import org.hibernate.boot.annotations.source.spi.AnnotationUsage;
import org.hibernate.boot.annotations.spi.AnnotationProcessingContext;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.compare.EqualsHelper;

/**
 * @author Strong Liu <stliu@hibernate.org>
 */
public abstract class AbstractOverrideDefinition {

	protected static final String PROPERTY_PATH_SEPARATOR = ".";
	protected final String attributePath;
	protected final AnnotationProcessingContext processingContext;

	private boolean isApplied;

	public AbstractOverrideDefinition(
			String prefix,
			AnnotationUsage<?> overrideAnnotation,
			AnnotationProcessingContext processingContext) {
		if ( overrideAnnotation == null ) {
			throw new IllegalArgumentException( "AnnotationInstance passed cannot be null" );
		}

		if ( !getTargetAnnotation().equals( overrideAnnotation.getAnnotationDescriptor() ) ) {
			throw new AssertionFailure( "Unexpected annotation passed to the constructor" );
		}

		this.attributePath = createAttributePath(
				prefix,
				overrideAnnotation.getAttributeValue( "name" ).getValue()
		);
		this.processingContext = processingContext;
	}

	protected static String createAttributePath(String prefix, String name) {
		if ( StringHelper.isEmpty( name ) ) {
			throw new AssertionFailure( "name attribute in @AttributeOverride can't be empty" );
		}
		String path = "";
		if ( StringHelper.isNotEmpty( prefix ) ) {
			path += prefix;
		}
		if ( StringHelper.isNotEmpty( path ) && !path.endsWith( PROPERTY_PATH_SEPARATOR ) ) {
			path += PROPERTY_PATH_SEPARATOR;
		}
		path += name;
		return path;
	}

	public String getAttributePath(){
		return attributePath;
	}

	public abstract void apply(AttributeMetadata persistentAttribute);

	protected abstract AnnotationDescriptor<?> getTargetAnnotation();

	public boolean isApplied() {
		return isApplied;
	}

	public void setApplied(boolean applied) {
		isApplied = applied;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof AbstractOverrideDefinition ) ) {
			return false;
		}

		AbstractOverrideDefinition that = (AbstractOverrideDefinition) o;
		return EqualsHelper.equals( this.attributePath, that.attributePath );
	}

	@Override
	public int hashCode() {
		return attributePath != null ? attributePath.hashCode() : 0;
	}

	@Override
	public String toString() {
		return "AbstractOverrideDefinition{attributePath='" + attributePath + "'}";
	}
}
