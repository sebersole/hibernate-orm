/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * Support for leveraging Jandex for annotation processing
 *
 * @implNote At the moment we want to continue to use HCANN.  Thus, for the
 * moment this code simply integrates Jandex into HCANN
 *
 * @author Steve Ebersole
 */
@Internal @Incubating
package org.hibernate.boot.jandex;

import org.hibernate.Incubating;
import org.hibernate.Internal;
