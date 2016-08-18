/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.spi.java;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.metamodel.Type;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.internal.java.JavaTypeDescriptorBasicAdaptorImpl;
import org.hibernate.type.descriptor.spi.MutabilityPlan;
import org.hibernate.type.descriptor.spi.TypeDescriptorRegistryAccess;
import org.hibernate.type.descriptor.spi.java.basic.EnumJavaTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.basic.JavaTypeDescriptorBasicImplementor;
import org.hibernate.type.descriptor.spi.java.basic.SerializableTypeDescriptor;
import org.hibernate.type.descriptor.spi.java.managed.JavaTypeDescriptorEmbeddableImplementor;
import org.hibernate.type.descriptor.spi.java.managed.JavaTypeDescriptorEntityImplementor;
import org.hibernate.type.descriptor.spi.java.managed.JavaTypeDescriptorMappedSuperclassImplementor;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.type.spi.TypeConfigurationAware;

import org.jboss.logging.Logger;

/**
 * Basically a map from {@link Class} -> {@link JavaTypeDescriptor}
 *
 * @author Steve Ebersole
 */
public class JavaTypeDescriptorRegistry implements JavaTypeDescriptorBaseline.BaselineTarget {
	private static final Logger log = Logger.getLogger( JavaTypeDescriptorRegistry.class );

	private final TypeDescriptorRegistryAccess typeConfiguration;
	private final ConcurrentHashMap<String,JavaTypeDescriptor> descriptorsByName = new ConcurrentHashMap<>();

	public JavaTypeDescriptorRegistry(TypeConfiguration typeConfiguration) {
		this.typeConfiguration = typeConfiguration;
		JavaTypeDescriptorBaseline.prime( this );
	}

	/**
	 * Adds the given descriptor to this registry
	 *
	 * @param descriptor The descriptor to add.
	 */
	public void addDescriptor(JavaTypeDescriptor descriptor) {
		addDescriptorInternal( descriptor.getTypeName(), descriptor );
	}

	private void addDescriptorInternal(JavaTypeDescriptor descriptor) {
		addDescriptorInternal( descriptor.getTypeName(), descriptor );
	}

	private void addDescriptorInternal(Class javaType, JavaTypeDescriptor descriptor) {
		addDescriptorInternal( javaType.getName(), descriptor );
	}

	private void addDescriptorInternal(String registrationKey, JavaTypeDescriptor descriptor) {
		performInjections( descriptor );

		final JavaTypeDescriptor old = descriptorsByName.put( registrationKey, descriptor );
		if ( old != null && old!= descriptor ) {
			log.debugf(
					"JavaTypeDescriptorRegistry entry replaced : %s -> %s (was %s)",
					descriptor.getJavaTypeClass(),
					descriptor,
					old
			);
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// basic type descriptor factory methods

	@SuppressWarnings("unchecked")
	public <T> JavaTypeDescriptorBasicImplementor<T> makeBasicTypeDescriptor(Class<T> javaType) {
		return makeBasicTypeDescriptor( javaType, null, null );
	}

	@SuppressWarnings("unchecked")
	public <T> JavaTypeDescriptorBasicImplementor<T> makeBasicTypeDescriptor(
			Class<T> javaType,
			MutabilityPlan<T> mutabilityPlan,
			Comparator comparator) {
		if ( javaType == null ) {
			throw new IllegalArgumentException( "Class passed to locate Java type descriptor cannot be null" );
		}

		JavaTypeDescriptor<T> typeDescriptor = descriptorsByName.get( javaType.getName() );
		if ( typeDescriptor != null ) {
			if ( typeDescriptor.getPersistenceType() != Type.PersistenceType.BASIC ) {
				throw new HibernateException(
						"JavaTypeDescriptor was already registered for " + javaType.getName() +
								" as a non-BasicType (" + typeDescriptor.getPersistenceType().name() + ")"
				);
			}
		}
		else {
			typeDescriptor = new JavaTypeDescriptorBasicAdaptorImpl( javaType, mutabilityPlan, comparator );
			addDescriptorInternal( javaType, typeDescriptor );
		}

		return (JavaTypeDescriptorBasicImplementor<T>) typeDescriptor;
	}

	@SuppressWarnings("unchecked")
	public <T> JavaTypeDescriptor<T> getDescriptor(String typeName) {
		return descriptorsByName.get( typeName );
	}

	@SuppressWarnings("unchecked")
	public <T> JavaTypeDescriptor<T> getDescriptor(Class<T> cls) {
		if ( cls == null ) {
			throw new IllegalArgumentException( "Class passed to locate Java type descriptor cannot be null" );
		}

		JavaTypeDescriptor<T> descriptor = descriptorsByName.get( cls.getName() );
		if ( descriptor != null ) {
			return descriptor;
		}

		if ( cls.isEnum() ) {
			descriptor = new EnumJavaTypeDescriptor( cls );
			descriptorsByName.put( cls.getName(), descriptor );
			return descriptor;
		}

		if ( Serializable.class.isAssignableFrom( cls ) ) {
			return new SerializableTypeDescriptor( cls );
		}

		// find the first "assignable" match
		for ( Map.Entry<String,JavaTypeDescriptor> entry : descriptorsByName.entrySet() ) {
			if ( entry.getValue().getJavaType() == null ) {
				continue;
			}

			if ( entry.getValue().getJavaType().isAssignableFrom( cls ) ) {
				log.debugf( "Using  cached JavaTypeDescriptor instance for Java class [%s]", cls.getName() );
				return entry.getValue();
			}
		}

		log.warnf( "Could not find matching type descriptor for requested Java class [%s]; using fallback", cls.getName() );
		return new JavaTypeDescriptorBasicAdaptorImpl<>( cls );
	}


	private void performInjections(JavaTypeDescriptor descriptor) {
		if ( descriptor instanceof TypeConfigurationAware ) {
			// would be nice to make the JavaTypeDescriptor for an entity, e.g., aware of the the TypeConfiguration
			( (TypeConfigurationAware) descriptor ).setTypeConfiguration( typeConfiguration.getTypeConfiguration() );
		}
	}

	private JavaTypeDescriptorEntityImplementor getEntityDescriptor(String typeName) {

	}


	private JavaTypeDescriptorMappedSuperclassImplementor getMappedSuperclassDescriptor(String typeName) {

	}


	private JavaTypeDescriptorEmbeddableImplementor getEmbeddableDescriptor(String typeName) {

	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// baseline descriptors

	@Override
	public void addBaselineDescriptor(JavaTypeDescriptorBasicImplementor descriptor) {
		addBaselineDescriptor( descriptor.getJavaType(), descriptor );
	}

	@Override
	public void addBaselineDescriptor(Class describedJavaType, JavaTypeDescriptorBasicImplementor descriptor) {
		performInjections( descriptor );
		descriptorsByName.put( describedJavaType.getName(), descriptor );
	}
}
