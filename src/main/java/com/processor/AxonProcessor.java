/*
 * Copyright (C) 2015 Hannes Dorfmann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.processor;

import com.google.auto.service.AutoService;
import com.processor.annotations.CommandValidator;
import com.processor.annotations.StateAccessor;
import com.processor.annotations.StateModifier;
import com.processor.codegenerator.AxonGroupedClasses;
import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;

import java.awt.List;
//import com.hannesdorfmann.annotationprocessing101.factory.annotation.Factory;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


/**
 * Annotation Processor for @Factory annotation
 *
 * @author Hannes Dorfmann
 */
@AutoService(Processor.class)
public class AxonProcessor extends AbstractProcessor {

	private Types typeUtils;
	private Elements elementUtils;
	private Filer filer;
	private Messager messager;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		typeUtils = processingEnv.getTypeUtils();
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotataions = new LinkedHashSet<String>();
		annotataions.add(StateAccessor.class.getCanonicalName());
		annotataions.add(StateModifier.class.getCanonicalName());
		annotataions.add(CommandValidator.class.getCanonicalName());
		return annotataions;
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv) {
		AxonGroupedClasses axonGroupedClass = new AxonGroupedClasses(filer);
		try {
			// Scan classes
			for (Element annotatedElement : roundEnv
					.getElementsAnnotatedWith(StateModifier.class)) {

				// Check if a method has been annotated with @StateModifer
				if (annotatedElement.getKind() == ElementKind.METHOD) {
					axonGroupedClass.updateAnnotatedClassMap(
							(ExecutableElement) annotatedElement, "Modifier",
							elementUtils);
 
				} else if (annotatedElement.getKind() == ElementKind.CONSTRUCTOR) {
					axonGroupedClass.updateAnnotatedClassMap(
							(ExecutableElement) annotatedElement,
							"Constructor", elementUtils);

				} else {
					throw new AxonProcessingException(annotatedElement,
							"Only methods can be annotated with @%s",
							StateModifier.class.getSimpleName());
				}

			}

			for (Element annotatedElement : roundEnv
					.getElementsAnnotatedWith(StateAccessor.class)) {

				// Check if a method has been annotated with @StateAccessor
				if (annotatedElement.getKind() == ElementKind.METHOD) {
					axonGroupedClass.updateAnnotatedClassMap(
							(ExecutableElement) annotatedElement, "Accessor",
							elementUtils);

				} else if (annotatedElement.getKind() == ElementKind.CONSTRUCTOR) {
					throw new AxonProcessingException(
							annotatedElement,
							"Only methods that change state can be annotated with @%s, constructors never change state",
							StateAccessor.class.getSimpleName());

				} else {
					throw new AxonProcessingException(annotatedElement,
							"Only methods can be annotated with @%s",
							StateAccessor.class.getSimpleName());
				}
			}
			
			for (Element annotatedElement : roundEnv
					.getElementsAnnotatedWith(CommandValidator.class)) {
				if (annotatedElement.getKind() == ElementKind.METHOD) {
					CommandValidator annotation = annotatedElement.getAnnotation(CommandValidator.class);
					String methodName = annotation.targetMethod();
					axonGroupedClass.updateAnnotatedMethod( elementUtils,(ExecutableElement )annotatedElement,methodName);
					
				} else {
					throw new AxonProcessingException(annotatedElement,
							"Only methods can be annotated with @%s",
							StateAccessor.class.getSimpleName());
				}
		
			}

			this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
					"start to generate code FOR " + axonGroupedClass);

			axonGroupedClass.generateCode();

		} catch (AxonProcessingException e) {
			error(e.getElement(), e.getMessage());
		} catch (Exception e) {
			error(null, e.getMessage());
		}

		return true;
	}

	/**
	 * Prints an error message
	 *
	 * @param e
	 *            The element which has caused the error. Can be null
	 * @param msg
	 *            The error message
	 */
	public void error(Element e, String msg) {
		messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
	}
}
