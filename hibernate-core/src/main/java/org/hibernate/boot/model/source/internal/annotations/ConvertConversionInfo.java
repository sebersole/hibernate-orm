/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model.source.internal.annotations;

import org.jboss.jandex.ClassInfo;

/**
 * Information about an attribute conversion as defined by explicit {@link javax.persistence.Convert}
 * and {@link javax.persistence.Converts} annotations.
 * <p/>
 * Auto-applied converters are handled separately later.
 *
 * @author Steve Ebersole
 */
public class ConvertConversionInfo {
	private final boolean conversionEnabled;
	private final ClassInfo converterClass;

	public ConvertConversionInfo(
			boolean conversionEnabled,
			ClassInfo converterClass) {
		this.conversionEnabled = conversionEnabled;
		this.converterClass = converterClass;
	}

	public boolean isConversionEnabled() {
		return conversionEnabled;
	}

	public ClassInfo getConverterTypeDescriptor() {
		return converterClass;
	}
}
