package org.jboss.perf.annotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import org.jboss.perf.BaseBenchmarkState;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;


@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BenchmarkQueryProcessor extends AbstractProcessor {

	private static final String[] SUPPORTED_ANNOTATIONS = new String[] {
			BenchmarkQuery.class.getCanonicalName(),
			BenchmarkQueries.class.getCanonicalName(),
			BenchmarkQueryMethod.class.getCanonicalName()
	};

	private Elements elements;
	private Filer filer;
	private Messager messager;
	private Types types;

	private Map<Element, BenchmarkQueryMethod> benchmarkMethods = new LinkedHashMap<>();
	private Map<Element, List<BenchmarkQuery>> benchmarkQueries = new LinkedHashMap<>();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init( processingEnv );

		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
		types = processingEnv.getTypeUtils();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return new LinkedHashSet<>( Arrays.asList( SUPPORTED_ANNOTATIONS ) );
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		roundEnv.getElementsAnnotatedWith( BenchmarkQuery.class ).forEach( this::processQueryElement );
		roundEnv.getElementsAnnotatedWith( BenchmarkQueries.class ).forEach( this::processQueryElement );
		roundEnv.getElementsAnnotatedWith( BenchmarkQueryMethod.class ).forEach( this::processQueryMethodElement );

		generateBenchmarkCode();

		benchmarkMethods.clear();
		benchmarkQueries.clear();
		return true;
	}

	private void processQueryMethodElement(Element element) {
		for ( BenchmarkQueryMethod methodAnnotation : element.getAnnotationsByType( BenchmarkQueryMethod.class ) ) {
			List<? extends VariableElement> parameters = ( (ExecutableElement) element ).getParameters();
			if ( parameters.size() != 2 ) {
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"Invalid number of parameters at benchmark method ",
						element
				);
				return;
			}
			if ( !types.isSubtype(parameters.get( 0 ).asType(),
					elements.getTypeElement( String.class.getCanonicalName() ).asType()
			) ) {
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"First parameter must be String at benchmark method ",
						element
				);
				return;
			}
			if ( !types.isSubtype(parameters.get( 1 ).asType(),
					elements.getTypeElement( BaseBenchmarkState.class.getCanonicalName() )
							.asType()
			) ) {
				messager.printMessage(
						Diagnostic.Kind.ERROR,
						"Second parameter must be BaseBenchmarkState at benchmark method ",
						element
				);
				return;
			}
			benchmarkMethods.put( element, methodAnnotation );
		}
	}

	private void processQueryElement(Element element) {
		for ( BenchmarkQuery queryAnnotation : element.getAnnotationsByType( BenchmarkQuery.class ) ) {
			processQueryAnnotation( queryAnnotation, element );
		}

	}

	private void processQueryAnnotation(BenchmarkQuery queryAnnotation, Element element) {

		if ( queryAnnotation == null || queryAnnotation.name().isEmpty() ) {
			messager.printMessage( Diagnostic.Kind.ERROR, "BenchmarkQuery with empty name at ", element );
			return;
		}
		if ( queryAnnotation.query().isEmpty() ) {
			messager.printMessage( Diagnostic.Kind.ERROR, "BenchmarkQuery with empty query at ", element );
			return;
		}
		benchmarkQueries.computeIfAbsent( element, l -> new ArrayList<>() ).add( queryAnnotation );
	}

	// --- //

	private void generateBenchmarkCode() {
		String jmhStateParameterName = "state";

		AnnotationSpec jmhBenchmark = AnnotationSpec.builder( Benchmark.class ).build();
		AnnotationSpec jmhMode = AnnotationSpec.builder( BenchmarkMode.class ).addMember(
				"value",
				"$T.$L",
				Mode.class,
				Mode.AverageTime
		).build();
		AnnotationSpec jmhOutput = AnnotationSpec.builder( OutputTimeUnit.class ).addMember(
				"value",
				"$T.$L",
				TimeUnit.class,
				TimeUnit.MICROSECONDS
		).build();

		for ( Map.Entry<Element, BenchmarkQueryMethod> benchmark : benchmarkMethods.entrySet() ) {
			String benchmarkName = benchmark.getValue().value();

			for ( Element baseElement : benchmarkQueries.keySet() ) {

				TypeSpec.Builder benchmarkClassBuilder = TypeSpec.classBuilder( baseElement.getSimpleName() + "_" + benchmarkName )
						.addModifiers( Modifier.PUBLIC )
						.superclass( TypeName.get( baseElement.asType() ) );
				for ( BenchmarkQuery benchmarkQuery : benchmarkQueries.get( baseElement ) ) {
					benchmarkClassBuilder.addMethod( MethodSpec.methodBuilder( benchmarkQuery.name() )
															 .addAnnotation( jmhBenchmark )
															 .addAnnotation( jmhMode )
															 .addAnnotation( jmhOutput )
															 .addModifiers( Modifier.PUBLIC )
															 .returns( Object.class )
															 .addParameter(
																	 BaseBenchmarkState.class,
																	 jmhStateParameterName
															 )
															 .addStatement(
																	 "return $L( $S, $L )",
																	 benchmark.getKey().getSimpleName(),
																	 benchmarkQuery.query(),
																	 jmhStateParameterName
															 )
															 .build() );
				}

				messager.printMessage( Diagnostic.Kind.NOTE, "Creating benchmark class ", baseElement );

				try {
					JavaFile.builder( baseElement.getEnclosingElement().toString(), benchmarkClassBuilder.build() )
							.build()
							.writeTo( filer );
				}
				catch (IOException e) {
					messager.printMessage(
							Diagnostic.Kind.ERROR,
							"Unable to generate benchmark file to " + filer.toString()
					);
				}
			}
		}
	}
}
