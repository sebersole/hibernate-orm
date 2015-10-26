/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations.metadata.attribute;

import org.hibernate.AssertionFailure;
import org.hibernate.boot.model.source.internal.annotations.metadata.type.EntityBindingContext;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.compare.EqualsHelper;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

/**
 * @author Strong Liu <stliu@hibernate.org>
 */
public abstract class AbstractOverrideDefinition {

	protected static final String PROPERTY_PATH_SEPARATOR = ".";
	protected final String attributePath;
	protected final EntityBindingContext bindingContext;

	private boolean isApplied;

	public AbstractOverrideDefinition(String prefix, AnnotationInstance attributeOverrideAnnotation,
			EntityBindingContext bindingContext) {
		if ( attributeOverrideAnnotation == null ) {
			throw new IllegalArgumentException( "AnnotationInstance passed cannot be null" );
		}

		if ( !getTargetAnnotation().equals( attributeOverrideAnnotation.name() ) ) {
			throw new AssertionFailure( "Unexpected annotation passed to the constructor" );
		}

		this.attributePath = createAttributePath(
				prefix,
				bindingContext.getTypedValueExtractor( String.class ).extract(
						attributeOverrideAnnotation,
						"name"
				)
		);
		this.bindingContext = bindingContext;
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

	public abstract void apply(AbstractPersistentAttribute persistentAttribute);

	protected abstract DotName getTargetAnnotation();

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
