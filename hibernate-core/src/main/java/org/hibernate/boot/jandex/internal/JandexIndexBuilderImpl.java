/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.jandex.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.boot.jandex.spi.JandexIndexBuilder;
import org.hibernate.boot.jandex.spi.JpaDotNames;
import org.hibernate.boot.spi.MetadataBuildingOptions;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
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
public class JandexIndexBuilderImpl implements JandexIndexBuilder {
	private static final Logger log = Logger.getLogger( JandexIndexBuilderImpl.class );

	private final DotName OBJECT_DOT_NAME = DotName.createSimple( Object.class.getName() );
	private final DotName VOID_DOT_NAME = DotName.createSimple( Void.class.getName() );
	private final DotName VOID_DOT_NAME2 = DotName.createSimple( "void" );

	private final Map<DotName, ClassInfo> inflightClassInfoMap;
	private final Indexer indexer;
	private final ResourceLocator resourceLocator;
	private final boolean autoIndexMembers;

	public JandexIndexBuilderImpl(
			MetadataBuildingOptions options,
			ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;

		this.indexer = new Indexer();
		this.inflightClassInfoMap = new HashMap<DotName, ClassInfo>();

		this.autoIndexMembers = options.autoIndexMemberTypes();
	}

	@Override
	public ClassInfo indexPackage(String packageName) {
		log.tracef( "Starting Jandex indexing of package : %s", packageName );

		try {
			final String packageInfoClassName = packageName + ".package-info";
			return indexClass( DotName.createSimple( packageInfoClassName ) );
		}
		catch (HibernateException ignore) {
			// Generally indicates that there is no package-info.class defined for the package
			// which is usually ok to ignore
			return null;
		}
	}

	@Override
	public ClassInfo indexClass(String className) {
		log.tracef( "Starting Jandex indexing of class-name : %s", className );
		return indexClass( DotName.createSimple( className ) );
	}

	private ClassInfo indexClass(DotName classDotName) {
		final ClassInfo existingEntry = inflightClassInfoMap.get( classDotName );
		if ( existingEntry != null ) {
			return existingEntry;
		}

		// NOTE : DotNode does have a #toString method accepting a deliminator, but in my experience that does not work
		final String resourceName = classDotName.toString().replace( '.', '/' ) + ".class";
		final URL resourceUrl = resourceLocator.locateResource( resourceName );
		if ( resourceUrl == null ) {
			throw new HibernateException( "Could not locate .class file for Class [" + classDotName.toString() + "] via resource lookup" );
		}

		try {
			final InputStream stream = resourceUrl.openStream();
			try {
				try {
					final ClassInfo classInfo = indexer.index( stream );
					inflightClassInfoMap.put( classInfo.name(), classInfo );
					furtherProcess( classInfo );
					return classInfo;
				}
				catch ( IOException e ) {
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
			finally {
				try {
					stream.close();
				}
				catch (IOException ignore) {
				}
			}
		}
		catch (IOException e) {
			throw new HibernateException( "Unable to open InputStream for .class file : " + resourceUrl.toExternalForm() );
		}
	}

	private void furtherProcess(ClassInfo classInfo) {
		if ( classInfo.superName() != null ) {
			indexClass( classInfo.superName() );
		}

		for ( DotName interfaceDotName : classInfo.interfaceNames() ) {
			indexClass( interfaceDotName );
		}

		// todo : nested classes do not seem to be available from Jandex ClassInfo

		final List<AnnotationInstance> entityListenerAnnotations = classInfo.annotations().get( JpaDotNames.ENTITY_LISTENERS );
		if ( entityListenerAnnotations != null ) {
			for ( AnnotationInstance entityListenerAnnotation : entityListenerAnnotations ) {
				final Type[] entityListenerClassTypes = entityListenerAnnotation.value().asClassArray();
				for ( Type entityListenerClassType : entityListenerClassTypes ) {
					indexClass( entityListenerClassType.name() );
				}
			}
		}

		if ( autoIndexMembers ) {
			indexMemberTypes( classInfo );
		}

		// todo : others?
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
	public ClassInfo indexClass(Class classReference) {
		log.tracef( "Starting Jandex indexing of class-reference : %s", classReference );
		return indexClass( DotName.createSimple( classReference.getName() ) );
	}

	@Override
	public IndexView buildIndexView() {
		return indexer.complete();
	}
}
