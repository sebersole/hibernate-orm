/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package org.hibernate.boot.annotations.parts.spi;

import org.hibernate.boot.annotations.source.spi.ManagedClass;

/**
 * Information about an attribute conversion as defined by explicit {@link jakarta.persistence.Convert}
 * and {@link jakarta.persistence.Converts} annotations.
 * <p/>
 * Auto-applied converters are handled separately later.
 *
 * @author Steve Ebersole
 */
public class ConversionMetadata {
	private final boolean conversionEnabled;
	private final ManagedClass converterClass;

	public ConversionMetadata(ManagedClass converterClass, boolean conversionEnabled) {
		this.conversionEnabled = conversionEnabled;
		this.converterClass = converterClass;
	}

	public boolean isConversionEnabled() {
		return conversionEnabled;
	}

	public ManagedClass getConverterClass() {
		return converterClass;
	}
}
