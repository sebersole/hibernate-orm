/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.jandex.internal;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.UUID;

import org.hibernate.HibernateException;
import org.hibernate.boot.ResourceLocator;
import org.hibernate.boot.internal.SimpleResourceLocator;
import org.hibernate.boot.jandex.spi.AnnotationDetails;
import org.hibernate.boot.jandex.spi.HibernateAnnotations;
import org.hibernate.boot.jandex.spi.JandexIndexBuilder;
import org.hibernate.boot.jandex.spi.JpaAnnotations;
import org.hibernate.boot.spi.MetadataBuildingOptions;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassSummary;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

/**
 * JandexIndexBuilder implementation indexing all known annotation sources ourselves.
 *
 * @author Steve Ebersole
 */
public class JandexIndexBuilderStandard implements JandexIndexBuilder {
	private static final Logger log = Logger.getLogger( JandexIndexBuilderStandard.class );

	private final Map<DotName, ClassSummary> inFlightClassInfoMap;
	private final Indexer indexer;
	private final ResourceLocator resourceLocator;
	private final boolean autoIndexMembers;

	public JandexIndexBuilderStandard() {
		this( new SimpleResourceLocator(), false );
	}

	public JandexIndexBuilderStandard(MetadataBuildingOptions options, ResourceLocator resourceLocator) {
		this( resourceLocator, options.autoIndexMemberTypes() );
	}

	public JandexIndexBuilderStandard(ResourceLocator resourceLocator, boolean autoIndexMembers) {
		this.resourceLocator = resourceLocator;

		this.indexer = new Indexer();
		this.inFlightClassInfoMap = new HashMap<>();

		this.autoIndexMembers = autoIndexMembers;

		primeJandexIndexer();
	}

	private void primeJandexIndexer() {
		indexClass( Boolean.class );
		indexClass( Byte.class );
		indexClass( Short.class );
		indexClass( Integer.class );
		indexClass( Long.class );
		indexClass( Float.class );
		indexClass( Double.class );
		indexClass( BigInteger.class );
		indexClass( BigDecimal.class );
		indexClass( Class.class );
		indexClass( Enum.class );
		indexClass( Locale.class );
		indexClass( Currency.class );
		indexClass( URL.class );
		indexClass( UUID.class );
		indexClass( Collection.class );
		indexClass( List.class );
		indexClass( Set.class );
		indexClass( SortedSet.class );
		indexClass( Map.class );
		indexClass( SortedMap.class );
		indexClass( Comparable.class );
		indexClass( Comparator.class );

		JpaAnnotations.forEachAnnotation( this::indexAnnotation );
		HibernateAnnotations.forEachAnnotation( this::indexAnnotation );
	}

	private void indexAnnotation(AnnotationDetails details) {
		indexClass( details.getJavaType() );
	}

	@Override
	public void indexPackage(String packageName) {
		log.tracef( "Starting Jandex indexing of package : %s", packageName );

		try {
			final String packageInfoClassName = packageName + ".package-info";
			indexClass( DotName.createSimple( packageInfoClassName ) );
		}
		catch (HibernateException ignore) {
			// Generally indicates that there is no package-info.class defined for the package
			// which is usually ok to ignore
		}
	}

	@Override
	public void indexClass(String className) {
		log.tracef( "Starting Jandex indexing of class-name : %s", className );
		indexClass( DotName.createSimple( className ) );
	}

	private void indexClass(DotName classDotName) {
		final ClassSummary existingEntry = inFlightClassInfoMap.get( classDotName );
		if ( existingEntry != null ) {
			return;
		}

		// NOTE : DotName does have a #toString method accepting a deliminator, but that only
		// works when using componentized names; we exclusively use simple names
		final String resourceName = classDotName.toString().replace( '.', '/' ) + ".class";
		final URL resourceUrl = resourceLocator.locateResource( resourceName );
		if ( resourceUrl == null ) {
			throw new HibernateException( "Could not locate .class file for Class [" + classDotName + "] via resource lookup" );
		}

		try {
			try ( final InputStream stream = resourceUrl.openStream() ) {
				try {
					final ClassSummary classSummary = indexer.indexWithSummary( stream );
					inFlightClassInfoMap.put( classSummary.name(), classSummary );
					furtherProcess( classSummary );
				}
				catch (IOException e) {
					throw new HibernateException(
							String.format(
									Locale.ROOT,
									"Unable to index Class [%s] from resource stream [%s]",
									classDotName.toString(),
									resourceUrl.toExternalForm()
							),
							e
					);
				}
			}
		}
		catch (IOException e) {
			throw new HibernateException( "Unable to open InputStream for .class file : " + resourceUrl.toExternalForm() );
		}
	}

	private void furtherProcess(ClassSummary classSummary) {
		if ( classSummary.superclassName() != null ) {
			indexClass( classSummary.superclassName() );
		}
	}

	private void indexMemberTypes(ClassInfo classInfo) {
		for ( FieldInfo fieldInfo : classInfo.fields() ) {
			indexMemberType( fieldInfo.type() );
		}

		for ( MethodInfo methodInfo : classInfo.methods() ) {
			// todo : ultimately we should maybe limit this to just getters (starts with "get" or "is")

			indexMemberType( methodInfo.returnType() );
		}
	}

	private void indexMemberType(Type type) {
		switch ( type.kind() ) {
			case PRIMITIVE:
			case VOID: {
				break;
			}
			case ARRAY: {
				indexMemberType( type.asArrayType().component() );
				break;
			}
			default: {
				indexClass( type.name() );
				break;
			}
		}
	}

	@Override
	public void indexClass(Class<?> classReference) {
		log.tracef( "Starting Jandex indexing of class-reference : %s", classReference );
		indexClass( DotName.createSimple( classReference.getName() ) );
	}

	@Override
	public IndexView buildIndex() {
		return indexer.complete();
	}
}
