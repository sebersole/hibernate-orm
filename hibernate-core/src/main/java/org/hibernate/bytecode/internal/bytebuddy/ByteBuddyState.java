/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.bytecode.internal.bytebuddy;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.hibernate.HibernateException;
import org.hibernate.bytecode.enhance.internal.bytebuddy.EnhancerImplConstants;
import org.hibernate.bytecode.enhance.spi.EnhancerConstants;
import org.hibernate.bytecode.spi.BasicProxyFactory;
import org.hibernate.engine.spi.PrimeAmongSecondarySupertypes;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.proxy.ProxyConfiguration;
import org.hibernate.proxy.ProxyFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.TypeCache;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.pool.TypePool;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.isFinalizer;
import static net.bytebuddy.matcher.ElementMatchers.isSynthetic;
import static net.bytebuddy.matcher.ElementMatchers.isVirtual;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static net.bytebuddy.matcher.ElementMatchers.returns;
import static net.bytebuddy.matcher.ElementMatchers.takesNoArguments;
import static org.hibernate.internal.CoreLogging.messageLogger;

/**
 * A utility to hold all ByteBuddy related state, as in the current version of
 * Hibernate the Bytecode Provider state is held in a static field, yet ByteBuddy
 * is able to benefit from some caching and general state reuse.
 */
public final class ByteBuddyState {

	private static final CoreMessageLogger LOG = messageLogger( ByteBuddyState.class );

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

	private static final boolean DEBUG = false;

	private final ByteBuddy byteBuddy;

	private static final ProxyDefinitionHelpers proxyDefinitionHelpers = new ProxyDefinitionHelpers();

	private final ClassRewriter classRewriter;

	final EnhancerImplConstants enhancerConstants = new EnhancerImplConstants();

	/**
	 * It will be easier to maintain the cache and its state when it will no longer be static
	 * in Hibernate ORM 6+.
	 * Opted for WEAK keys to avoid leaking the classloader in case the SessionFactory isn't closed.
	 * Avoiding Soft keys as they are prone to cause issues with unstable performance.
	 */
	private final TypeCache<TypeCache.SimpleKey> proxyCache;
	private final TypeCache<TypeCache.SimpleKey> basicProxyCache;

	public ByteBuddyState() {
		this( ClassFileVersion.ofThisVm( ClassFileVersion.JAVA_V11 ) );
	}

	ByteBuddyState(ClassFileVersion classFileVersion) {
		this.byteBuddy = new ByteBuddy( classFileVersion ).with( TypeValidation.DISABLED );
		this.proxyCache = new TypeCache( TypeCache.Sort.WEAK );
		this.basicProxyCache = new TypeCache( TypeCache.Sort.WEAK );
		this.classRewriter = new StandardClassRewriter();
	}

	/**
	 * Load a proxy as generated by the {@link ProxyFactory}.
	 *
	 * @param referenceClass The main class to proxy - might be an interface.
	 * @param cacheKey The cache key.
	 * @param makeProxyFunction A function building the proxy.
	 * @return The loaded proxy class.
	 */
	public Class<?> loadProxy(Class<?> referenceClass, TypeCache.SimpleKey cacheKey,
			Function<ByteBuddy, DynamicType.Builder<?>> makeProxyFunction) {
		return load( referenceClass, proxyCache, cacheKey, makeProxyFunction );
	}

	/**
	 * Load a proxy as generated by the {@link BasicProxyFactory}.
	 *
	 * @param referenceClass The main class to proxy - might be an interface.
	 * @param cacheKey The cache key.
	 * @param makeProxyFunction A function building the proxy.
	 * @return The loaded proxy class.
	 */
	Class<?> loadBasicProxy(Class<?> referenceClass, TypeCache.SimpleKey cacheKey,
			Function<ByteBuddy, DynamicType.Builder<?>> makeProxyFunction) {
		return load( referenceClass, basicProxyCache, cacheKey, makeProxyFunction );
	}

	/**
	 * Load a class generated by ByteBuddy.
	 *
	 * @param referenceClass The main class to proxy - might be an interface.
	 * @param makeClassFunction A function building the class.
	 * @return The loaded generated class.
	 */
	public Class<?> load(Class<?> referenceClass, Function<ByteBuddy, DynamicType.Builder<?>> makeClassFunction) {
		Unloaded<?> result =
		make( makeClassFunction.apply( byteBuddy ) );
		if (DEBUG) {
			try {
				result.saveIn( new File( System.getProperty( "java.io.tmpdir" ) + "/bytebuddy/" ) );
			}
			catch (IOException e) {
				LOG.warn( "Unable to save generated class %1$s", result.getTypeDescription().getName(), e );
			}
		}
		return result.load( referenceClass.getClassLoader(), resolveClassLoadingStrategy( referenceClass ) )
		.getLoaded();
	}

	/**
	 * Rewrite a class, used by the enhancer.
	 * <p>
	 * WARNING: Returns null if rewriteClassFunction returns a null builder. Do not use if you expect the original
	 * content.
	 *
	 * @param typePool the ByteBuddy TypePool
	 * @param className The original class name.
	 * @param rewriteClassFunction The function used to rewrite the class.
	 * @return The rewritten content of the class or null if rewriteClassFunction returns a null builder.
	 */
	public byte[] rewrite(TypePool typePool, String className,
			Function<ByteBuddy, DynamicType.Builder<?>> rewriteClassFunction) {
		DynamicType.Builder<?> builder = rewriteClassFunction.apply( byteBuddy );
		if ( builder == null ) {
			return null;
		}

		return make( typePool, builder ).getBytes();
	}

	/**
	 * Returns the proxy definition helpers to reuse when defining proxies.
	 * <p>
	 * These elements are shared as they are immutable.
	 *
	 * @return The proxy definition helpers.
	 */
	public ProxyDefinitionHelpers getProxyDefinitionHelpers() {
		return proxyDefinitionHelpers;
	}

	/**
	 * Wipes out all known caches used by ByteBuddy. This implies it might trigger the need
	 * to re-create some helpers if used at runtime, especially as this state might be shared by
	 * multiple SessionFactory instances, but at least ensures we cleanup anything which is no
	 * longer needed after a SessionFactory close.
	 * The assumption is that closing SessionFactories is a rare event; in this perspective the cost
	 * of re-creating the small helpers should be negligible.
	 */
	void clearState() {
		proxyCache.clear();
		basicProxyCache.clear();
	}

	private Class<?> load(Class<?> referenceClass, TypeCache<TypeCache.SimpleKey> cache,
			TypeCache.SimpleKey cacheKey, Function<ByteBuddy, DynamicType.Builder<?>> makeProxyFunction) {
		return cache.findOrInsert(
				referenceClass.getClassLoader(),
				cacheKey,
				() -> make( makeProxyFunction.apply( byteBuddy ) )
						.load(
								referenceClass.getClassLoader(),
								resolveClassLoadingStrategy( referenceClass )
						)
						.getLoaded(),
				cache
		);
	}

	public Unloaded<?> make(Function<ByteBuddy, DynamicType.Builder<?>> makeProxyFunction) {
		return make( makeProxyFunction.apply( byteBuddy ) );
	}

	public Unloaded<?> make(TypePool typePool, Function<ByteBuddy, DynamicType.Builder<?>> makeProxyFunction) {
		return make( typePool, makeProxyFunction.apply( byteBuddy ) );
	}

	private Unloaded<?> make(DynamicType.Builder<?> builder) {
		return make( null, builder );
	}

	private Unloaded<?> make(TypePool typePool, DynamicType.Builder<?> builder) {
		Unloaded<?> unloadedClass;
		if ( typePool != null ) {
			unloadedClass = builder.make( typePool );
		}
		else {
			unloadedClass = builder.make();
		}

		if ( DEBUG ) {
			try {
				unloadedClass.saveIn( new File( System.getProperty( "java.io.tmpdir" ) + "/bytebuddy/" ) );
			}
			catch (IOException e) {
				LOG.warn( "Unable to save generated class %1$s", unloadedClass.getTypeDescription().getName(), e );
			}
		}
		return unloadedClass;
	}

	public EnhancerImplConstants getEnhancerConstants() {
		return this.enhancerConstants;
	}

	/**
	 * Shared proxy definition helpers. They are immutable so we can safely share them.
	 */
	public static class ProxyDefinitionHelpers {

		private final ElementMatcher<? super MethodDescription> groovyGetMetaClassFilter;
		private final ElementMatcher<? super MethodDescription> virtualNotFinalizerFilter;
		private final ElementMatcher<? super MethodDescription> proxyNonInterceptedMethodFilter;
		private final List<ElementMatcher<? super MethodDescription>> toFullyIgnore = new ArrayList<>();
		private final MethodDelegation delegateToInterceptorDispatcherMethodDelegation;
		private final FieldAccessor.PropertyConfigurable interceptorFieldAccessor;

		private ProxyDefinitionHelpers() {
			this.groovyGetMetaClassFilter = isSynthetic().and( named( "getMetaClass" )
					.and( returns( td -> "groovy.lang.MetaClass".equals( td.getName() ) ) ) );
			this.virtualNotFinalizerFilter = isVirtual().and( not( isFinalizer() ) );
			this.proxyNonInterceptedMethodFilter = nameStartsWith( "$$_hibernate_" ).and( isVirtual() )
					// HHH-15090: Don't apply extended enhancement reader/writer methods to the proxy;
					// those need to be executed on the actual entity.
					.and( not( nameStartsWith( EnhancerConstants.PERSISTENT_FIELD_READER_PREFIX ) ) )
					.and( not( nameStartsWith( EnhancerConstants.PERSISTENT_FIELD_WRITER_PREFIX ) ) );

			// Populate the toFullyIgnore list
			for ( Method m : PrimeAmongSecondarySupertypes.class.getMethods() ) {
				//We need to ignore both the match of each default method on PrimeAmongSecondarySupertypes
				toFullyIgnore.add( isDeclaredBy( PrimeAmongSecondarySupertypes.class ).and( named( m.getName() ) ).and( takesNoArguments() ) );
				//And the override in the interface it belongs to - which we happen to have in the return type
				toFullyIgnore.add( isDeclaredBy( m.getReturnType() ).and( named( m.getName() ) ).and( takesNoArguments() ) );
			}

			this.delegateToInterceptorDispatcherMethodDelegation = MethodDelegation.to( ProxyConfiguration.InterceptorDispatcher.class );

			this.interceptorFieldAccessor = FieldAccessor.ofField( ProxyConfiguration.INTERCEPTOR_FIELD_NAME )
					.withAssigner( Assigner.DEFAULT, Assigner.Typing.DYNAMIC );
		}

		public ElementMatcher<? super MethodDescription> getGroovyGetMetaClassFilter() {
			return groovyGetMetaClassFilter;
		}

		public ElementMatcher<? super MethodDescription> getVirtualNotFinalizerFilter() {
			return virtualNotFinalizerFilter;
		}

		public ElementMatcher<? super MethodDescription> getProxyNonInterceptedMethodFilter() {
			return proxyNonInterceptedMethodFilter;
		}

		public MethodDelegation getDelegateToInterceptorDispatcherMethodDelegation() {
			return delegateToInterceptorDispatcherMethodDelegation;
		}

		public FieldAccessor.PropertyConfigurable getInterceptorFieldAccessor() {
			return interceptorFieldAccessor;
		}

		public DynamicType.Builder<?> appendIgnoreAlsoAtEnd(DynamicType.Builder<?> builder) {
			for ( ElementMatcher<? super MethodDescription> m : toFullyIgnore ) {
				builder = builder.ignoreAlso( m );
			}
			return builder;
		}
	}

	private interface ClassRewriter {
		DynamicType.Builder<?> installReflectionMethodVisitors(DynamicType.Builder<?> builder);

		void registerAuthorizedClass(Unloaded<?> unloadedClass);
	}

	private static class StandardClassRewriter implements ClassRewriter {
		@Override
		public DynamicType.Builder<?> installReflectionMethodVisitors(DynamicType.Builder<?> builder) {
			// do nothing
			return builder;
		}

		@Override
		public void registerAuthorizedClass(Unloaded<?> unloadedClass) {
			// do nothing
		}
	}

	private static ClassLoadingStrategy<ClassLoader> resolveClassLoadingStrategy(Class<?> originalClass) {
		try {
			return ClassLoadingStrategy.UsingLookup.of( MethodHandles.privateLookupIn( originalClass, LOOKUP ) );
		}
		catch (Throwable e) {
			throw new HibernateException( LOG.bytecodeEnhancementFailedUnableToGetPrivateLookupFor( originalClass.getName() ), e );
		}
	}

}
