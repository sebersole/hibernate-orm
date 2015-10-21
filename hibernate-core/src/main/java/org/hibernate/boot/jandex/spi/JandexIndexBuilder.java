/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.jandex.spi;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;

/**
 * When we are not passed a Jandex index during bootstrap we will manually build one
 * from any resources we know about.
 * <p/>
 * NOTE that this is different from the {@code orm.xml} augmentation process which
 * will happen later.  This is just building a Jandex index from known annotation
 * sources (package-info and classes).
 *
 * @author Steve Ebersole
 */
public interface JandexIndexBuilder {
	ClassInfo indexPackage(String packageName);
	ClassInfo indexClass(String className);
	ClassInfo indexClass(Class classReference);

	IndexView buildIndexView();
}
