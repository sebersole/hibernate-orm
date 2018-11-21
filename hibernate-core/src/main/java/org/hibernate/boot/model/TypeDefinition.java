/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.boot.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Supplier;

import org.hibernate.MappingException;
import org.hibernate.boot.model.domain.ResolutionContext;
import org.hibernate.boot.model.type.spi.BasicTypeResolver;
import org.hibernate.boot.model.type.spi.TypeResolverTemplate;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.BasicValue;
import org.hibernate.metamodel.model.convert.spi.BasicValueConverter;
import org.hibernate.metamodel.model.domain.spi.BasicValueMapper;
import org.hibernate.resource.beans.spi.ManagedBean;
import org.hibernate.resource.beans.spi.ManagedBeanRegistry;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.spi.BasicJavaDescriptor;
import org.hibernate.type.descriptor.sql.spi.SqlTypeDescriptor;
import org.hibernate.type.spi.BasicType;
import org.hibernate.type.spi.ParameterizedType;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Models the information pertaining to a custom type definition supplied by the user.  Used
 * to delay instantiation of the actual {@link Type} instance.
 *
 * Generally speaking this information would come from annotations
 * ({@link org.hibernate.annotations.TypeDef}) or XML mappings.
 *
 * @author Steve Ebersole
 * @author John Verhaeg
 */
public class TypeDefinition implements TypeResolverTemplate, Serializable {
	private final String name;
	private final Class typeImplementorClass;
	private final String[] registrationKeys;
	private final Map<String, String> parameters;

	private BasicTypeResolver typeResolver;

	public TypeDefinition(
			String name,
			Class typeImplementorClass,
			String[] registrationKeys,
			Map<String, String> parameters,
			TypeConfiguration typeConfiguration) {
		this.name = name;
		this.typeImplementorClass = typeImplementorClass;
		this.registrationKeys= registrationKeys;
		this.parameters = parameters == null
				? Collections.<String, String>emptyMap()
				: Collections.unmodifiableMap( parameters );
	}

	public TypeDefinition(
			String name,
			Class typeImplementorClass,
			String[] registrationKeys,
			Properties parameters,
			TypeConfiguration typeConfiguration) {
		this.name = name;
		this.typeImplementorClass = typeImplementorClass;
		this.registrationKeys= registrationKeys;
		this.parameters = parameters == null
				? Collections.emptyMap()
				: extractStrings( parameters );
	}

	private Map<String, String> extractStrings(Properties properties) {
		final Map<String, String> parameters = new HashMap<>();

		for ( Map.Entry entry : properties.entrySet() ) {
			if ( entry.getKey() instanceof String
					&& entry.getValue() instanceof String ) {
				parameters.put(
						(String) entry.getKey(),
						(String) entry.getValue()
				);
			}
		}

		return parameters;
	}

	public String getName() {
		return name;
	}

	public Class getTypeImplementorClass() {
		return typeImplementorClass;
	}

	public String[] getRegistrationKeys() {
		return registrationKeys;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public Properties getParametersAsProperties() {
		Properties properties = new Properties();
		properties.putAll( parameters );
		return properties;
	}

	@SuppressWarnings("unchecked")
	public BasicValueMapper resolve(
			BasicJavaDescriptor explicitJtd,
			SqlTypeDescriptor explicitStd,
			Map localConfigParameters,
			Supplier<BasicValueConverter> converterSupplier,
			MutabilityPlan explicitMutabilityPlan,
			MetadataBuildingContext context) {
		final ManagedBean typeBean = context.getBootstrapContext()
				.getServiceRegistry()
				.getService( ManagedBeanRegistry.class )
				.getBean( typeImplementorClass );

		final Object typeInstance = typeBean.getBeanInstance();

		injectParameters(
				typeInstance,
				() -> {
					final Map<String, String> mergedParameters = new HashMap<>( parameters );
					if ( localConfigParameters != null && ! localConfigParameters.isEmpty() ) {
						mergedParameters.putAll( localConfigParameters );
					}

					return mergedParameters;
				}
		);

		return BasicValue.toValueMapper(
				name,
				typeInstance,
				explicitJtd,
				explicitStd,
				converterSupplier,
				explicitMutabilityPlan,
				context
		);
	}

	@SuppressWarnings("unchecked")
	private void injectParameters(Object customType, Supplier<Map> parameterSupplier) {
		if ( customType instanceof ParameterizedType ) {
			( (ParameterizedType) customType ).setParameters( parameterSupplier.get() );
		}
	}


	@Override
	public BasicTypeResolver resolveTypeResolver(Map<String, String> localConfigParameters) {
		// the config parameters are local that come from @Type
		// They win over any parameters internally represented.
		//
		if ( localConfigParameters.isEmpty() ) {
			if ( typeResolver == null ) {
				typeResolver = buildTypeResolver( parameters );
			}
			return typeResolver;
		}

		// merge incoming and type definition parameters
		// incoming wins over existing values
		Map<String, String> mergedParameters = new HashMap<>( parameters );
		mergedParameters.putAll( localConfigParameters );

		return buildTypeResolver( mergedParameters );
	}

	private BasicTypeResolver buildTypeResolver(Map<String, String> parameters) {
		return new BasicTypeResolver() {
			private BasicType basicType;

			@Override
			public <T> BasicType<T> resolveBasicType(ResolutionContext context) {
				if ( basicType == null ) {
					basicType = instantiateBasicType();
					injectParameters( basicType, parameters );
				}
				return basicType;
			}

			private <T> BasicType<T> instantiateBasicType() {
				BasicType <T> basicType;
				try {
					basicType = (BasicType) typeImplementorClass.newInstance();
				}
				catch ( Exception e ) {
					throw new MappingException(
							"Unable to instantiate custom type: " + typeImplementorClass.getName(),
							e
					);
				}
				return basicType;
			}

			private void injectParameters(Type<?> type, Map<String, String> parameters) {
				if ( parameters != null && !parameters.isEmpty() ) {
					if ( ParameterizedType.class.isInstance( type ) ) {
						( (ParameterizedType) type ).setParameters( parameters );
					}
				}
			}
		};
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( !( o instanceof TypeDefinition ) ) {
			return false;
		}

		final TypeDefinition that = (TypeDefinition) o;
		return Objects.equals( this.name, that.name )
				&& Objects.equals( this.typeImplementorClass, that.typeImplementorClass )
				&& Arrays.equals( this.registrationKeys, that.registrationKeys )
				&& Objects.equals( this.parameters, that.parameters );
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + ( typeImplementorClass != null ? typeImplementorClass.hashCode() : 0 );
		result = 31 * result + ( registrationKeys != null ? Arrays.hashCode( registrationKeys ) : 0 );
		result = 31 * result + ( parameters != null ? parameters.hashCode() : 0 );
		return result;
	}

	@Override
	public String toString() {
		return "TypeDefinition{" +
				"name='" + name + '\'' +
				", typeImplementorClass=" + typeImplementorClass +
				", registrationKeys=" + Arrays.toString( registrationKeys ) +
				", parameters=" + parameters +
				'}';
	}
}
