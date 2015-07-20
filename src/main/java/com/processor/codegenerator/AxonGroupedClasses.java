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

package com.processor.codegenerator;

import com.processor.AxonProcessingException;
import com.processor.annotations.StateAccessor;
import com.processor.codegenerator.aggregate.AggregateBuilder;
import com.processor.codegenerator.eventcommand.constructor.ConstructorCommandBuilder;
import com.processor.codegenerator.eventcommand.constructor.ConstructorEventBuilder;
import com.processor.codegenerator.eventcommand.nonconstructor.CommandBuilder;
import com.processor.codegenerator.eventcommand.nonconstructor.EventBuilder;
import com.processor.codegenerator.facade.FacadeBuilder;
import com.processor.codegenerator.gui.controller.ClassControllerBuilder;
import com.processor.codegenerator.gui.controller.ControllerInterfaceBuilder;
import com.processor.codegenerator.gui.model.ClassModelBuilder;
import com.processor.codegenerator.gui.model.ModelInterfaceBuilder;
import com.processor.codegenerator.gui.view.ClassViewBuilder;
import com.processor.codegenerator.gui.view.ViewInterfaceBuilder;
import com.processor.codegenerator.handler.ExternalCommandHandlerBuilder;
import com.processor.codegenerator.handler.ExternalCommandHandlerInterfaceBuilder;
import com.processor.codegenerator.test.TestAggregateBuilder;
import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class AxonGroupedClasses {

	private Map<String, AxonAnnotatedClass> classMap = new HashMap<String, AxonAnnotatedClass>();

	Filer filer;

	public List<AxonAnnotatedClass> annotatedClassList = new LinkedList<AxonAnnotatedClass>();

	public void writeGeneratedCodeToFile(String packageName, TypeSpec typeSpec)
			throws Exception {
		JavaFile.builder(packageName, typeSpec).build().writeTo(filer);

	}

	public void updateAnnotatedMethod(Elements elementUtils,
			ExecutableElement commandValidator, String methodName) {
		String className = commandValidator.getEnclosingElement()
				.getSimpleName().toString();
		AxonAnnotatedClass annotatedClass = classMap.get(className);
		annotatedClass.updateMethodValidator(methodName, commandValidator);
		classMap.put(className, annotatedClass);
	}

	public void updateAnnotatedClassMap(ExecutableElement annotatedElement,
			String type, Elements elementUtils) throws AxonProcessingException {
		String className = annotatedElement.getEnclosingElement()
				.getSimpleName().toString();
		AxonAnnotatedClass value = classMap.get(className);

		if (type.equals("Modifier")) {

			if (value == null) {
				value = new AxonAnnotatedClass(
						(TypeElement) annotatedElement.getEnclosingElement(),
						elementUtils);
				value.addModifierMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement, elementUtils));
			} else {
				value.addModifierMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement, elementUtils));
			}
			classMap.put(className, value);

		} else if (type.equals("Accessor")) {

			if (value == null) {
				value = new AxonAnnotatedClass(
						(TypeElement) annotatedElement.getEnclosingElement(),
						elementUtils);
				value.addAccessorMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement, elementUtils));
			} else {
				value.addAccessorMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement, elementUtils));
			}
			classMap.put(className, value);
		} else if (type.equals("Constructor")) {

			if (value == null) {
				value = new AxonAnnotatedClass(
						(TypeElement) annotatedElement.getEnclosingElement(),
						elementUtils);
				value.addConstructorMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement, elementUtils));
			} else {
				AxonAnnotatedMethod AxonAnnotatedMethod = new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement, elementUtils);
				value.addConstructorMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement, elementUtils));
			}
			classMap.put(className, value);
		}
	}

	public AxonGroupedClasses(Filer filer) {
		super();
		this.filer = filer;
	}

	public void generateCommandEvents(AxonAnnotatedClass annotatedClass)
			throws IOException, Exception {
		String className = annotatedClass.getClassName();
		for (AxonAnnotatedMethod annotatedMethod : annotatedClass
				.getClassModifierMethods()) {

			String pkgName = annotatedMethod.getPackageName() + ".command";
			CommandBuilder commandBuilder = new CommandBuilder(annotatedMethod);
			writeGeneratedCodeToFile(pkgName, commandBuilder.getCommandClass());

			pkgName = annotatedMethod.getPackageName() + ".event";
			EventBuilder eventBuilder = new EventBuilder(annotatedMethod);
			writeGeneratedCodeToFile(pkgName, eventBuilder.getEventClass());

		}
		for (AxonAnnotatedMethod annotatedMethod : annotatedClass
				.getClassConstructorMethods()) {

			String pkgName = annotatedMethod.getPackageName() + ".command";
			ConstructorCommandBuilder constructorCommandBuilder = new ConstructorCommandBuilder(
					className, annotatedMethod);
			writeGeneratedCodeToFile(pkgName,
					constructorCommandBuilder.getCommandClass());

			pkgName = annotatedMethod.getPackageName() + ".event";
			ConstructorEventBuilder constructorEventBuilder = new ConstructorEventBuilder(
					className, annotatedMethod);
			writeGeneratedCodeToFile(pkgName,
					constructorEventBuilder.getEventClass());

		}

	}

	public void generateAggregate(AxonAnnotatedClass annotatedClass)
			throws IOException, Exception {
		String pkgName = annotatedClass.getPackageName() + ".aggregate";
		AggregateBuilder aggregateBuilder = new AggregateBuilder(annotatedClass);
		writeGeneratedCodeToFile(pkgName, aggregateBuilder.getAggregateClass());

	}

	public void generateTest(AxonAnnotatedClass annotatedClass)
			throws IOException, Exception {
		String pkgName = annotatedClass.getPackageName() + ".test";
		TestAggregateBuilder testAggregateBuilder = new TestAggregateBuilder(
				annotatedClass);
		writeGeneratedCodeToFile(pkgName,
				testAggregateBuilder.getAggregateClass());

	}

	public void generateFacade(AxonAnnotatedClass annotatedClass)
			throws IOException, Exception {
		/* generate the facade */
		String pkgName = annotatedClass.getPackageName();
		FacadeBuilder facadeBuilder = new FacadeBuilder(annotatedClass);
		writeGeneratedCodeToFile(pkgName, facadeBuilder.getFacadeClass());
	}

	public void generateExternalCommandHandler(AxonAnnotatedClass annotatedClass)
			throws IOException, Exception {
		/* external command handler */
		/* interface */
		String pkgName = annotatedClass.getPackageName();
		ExternalCommandHandlerInterfaceBuilder interfaceBuilder = new ExternalCommandHandlerInterfaceBuilder(
				annotatedClass);
		writeGeneratedCodeToFile(pkgName, interfaceBuilder.getInterface());
		/* default implementation */
		writeGeneratedCodeToFile(pkgName,
				interfaceBuilder.getDefaultCommmandHandler());

		ExternalCommandHandlerBuilder externalCommandHandlerBuilder = new ExternalCommandHandlerBuilder(
				annotatedClass);
		writeGeneratedCodeToFile(pkgName,
				externalCommandHandlerBuilder.getHandler());

	}

	public void generateGui(AxonAnnotatedClass annotatedClass)
			throws IOException, Exception {
		
		/* generate the view */
		String pkgName = annotatedClass.getPackageName() + ".gui.view";
		ViewInterfaceBuilder viewInterfaceBuilder = new ViewInterfaceBuilder(
				annotatedClass);
		writeGeneratedCodeToFile(pkgName, viewInterfaceBuilder.getInterface());
		
		ClassViewBuilder classViewBuilder = new ClassViewBuilder(
				annotatedClass);
		writeGeneratedCodeToFile(pkgName, classViewBuilder.getClassView());
		
		
		pkgName = annotatedClass.getPackageName() + ".gui.controller";
		ControllerInterfaceBuilder controllerInterfaceBuilder = new ControllerInterfaceBuilder(
				annotatedClass);
		writeGeneratedCodeToFile(pkgName, controllerInterfaceBuilder.getInterface());
		
		ClassControllerBuilder classControllerBuilder = new ClassControllerBuilder(
				annotatedClass);
		TypeSpec test = classControllerBuilder.getClassController();
		writeGeneratedCodeToFile(pkgName, test);
		
		
		pkgName = annotatedClass.getPackageName() + ".gui.model";
		ModelInterfaceBuilder modelInterfaceBuilder = new ModelInterfaceBuilder(
				annotatedClass);
		writeGeneratedCodeToFile(pkgName, modelInterfaceBuilder.getInterface());
	
		ClassModelBuilder classModelBuilder = new ClassModelBuilder(
				annotatedClass);
		writeGeneratedCodeToFile(pkgName, classModelBuilder.getClassController());

	}

	public void generateCode() throws IOException, Exception {
		for (Map.Entry<String, AxonAnnotatedClass> entry : classMap.entrySet()) {
			AxonAnnotatedClass annotatedClass = entry.getValue();

			this.generateCommandEvents(annotatedClass);
			this.generateAggregate(annotatedClass);
			this.generateExternalCommandHandler(annotatedClass);
			this.generateFacade(annotatedClass);
			this.generateTest(annotatedClass);
			this.generateGui(annotatedClass);

		}

	}

	@Override
	public String toString() {
		return "AxonGroupedClasses [classMap=" + classMap
				+ ", annotatedClassList=" + "]";
	}

}
