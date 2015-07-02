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
import com.processor.codegenerator.constructor.ConstructorCommandBuilder;
import com.processor.codegenerator.constructor.ConstructorEventBuilder;
import com.processor.codegenerator.eventcommand.CommandBuilder;
import com.processor.codegenerator.eventcommand.EventBuilder;
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
	private static final String SUFFIX = "";


	private Map<String, AxonAnnotatedClass> classMap = new HashMap<String, AxonAnnotatedClass>();
	public List<AxonAnnotatedClass> annotatedClassList = new LinkedList<AxonAnnotatedClass>();
	Filer filer;

	public void writeGeneratedCodeToFile(String packageName ,TypeSpec typeSpec) throws Exception {
		JavaFile.builder(packageName, typeSpec).build().writeTo(filer);

	}


	public void updateAnnotatedClassMap(ExecutableElement annotatedElement,
			String type,Elements elementUtils) throws AxonProcessingException {
		String className = annotatedElement.getEnclosingElement()
				.getSimpleName().toString();
		AxonAnnotatedClass value = classMap.get(className);

		if (type.equals("Modifier")) {

			if (value == null) {
				value = new AxonAnnotatedClass(
						(TypeElement) annotatedElement.getEnclosingElement(),elementUtils);
				value.addModifierMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement,elementUtils));
			} else {
				value.addModifierMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement,elementUtils));
			}
			classMap.put(className, value);

		} else if (type.equals("Accessor")) {

			if (value == null) {
				value = new AxonAnnotatedClass(
						(TypeElement) annotatedElement.getEnclosingElement(),elementUtils);
				value.addAccessorMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement,elementUtils));
			} else {
				value.addAccessorMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement,elementUtils));
			}
			classMap.put(className, value);
		} else if (type.equals("Constructor")) {

			if (value == null) {
				value = new AxonAnnotatedClass(
						(TypeElement) annotatedElement.getEnclosingElement(),elementUtils);
				value.addConstructorMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement,elementUtils));
			} else {
				value.addConstructorMethod(new AxonAnnotatedMethod(
						(ExecutableElement) annotatedElement,elementUtils));
			}
			classMap.put(className, value);
		}
	}

	public AxonGroupedClasses(Filer filer) {
		super();
		this.filer = filer;
	}

	public void generateCodeForClass()
			throws IOException, Exception {
		// for package name and staff
		/*
		 * TypeElement superClassName = elementUtils
		 * .getTypeElement(qualifiedClassName); String factoryClassName =
		 * superClassName.getSimpleName() + SUFFIX; String
		 * qualifiedFactoryClassName = qualifiedClassName + SUFFIX;
		 * PackageElement pkg = elementUtils.getPackageOf(superClassName);
		 * String packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName()
		 * .toString();
		 */

		// Generate items map

		for (Map.Entry<String, AxonAnnotatedClass> entry : classMap.entrySet()) {
			AxonAnnotatedClass annotatedClass = entry.getValue();
			String className = entry.getKey();
			for (AxonAnnotatedMethod annotatedMethod : annotatedClass.getClassModifierMethods()) {
				String pkgName =annotatedMethod.getPackageName()+".command";
				CommandBuilder commandBuilder = new CommandBuilder(annotatedMethod);
				writeGeneratedCodeToFile(pkgName ,commandBuilder.getCommandClass());
				pkgName =annotatedMethod.getPackageName()+".event";
				EventBuilder eventBuilder = new EventBuilder(annotatedMethod);
				writeGeneratedCodeToFile(pkgName,eventBuilder.getEventClass());
			}
			for (AxonAnnotatedMethod annotatedMethod : annotatedClass.getClassConstructorMethods()) {
				String pkgName =annotatedMethod.getPackageName()+".command";
				ConstructorCommandBuilder constructorCommandBuilder = new ConstructorCommandBuilder(className,annotatedMethod);
				writeGeneratedCodeToFile(pkgName,constructorCommandBuilder.getCommandClass());
				pkgName =annotatedMethod.getPackageName()+".event";
				ConstructorEventBuilder constructorEventBuilder = new ConstructorEventBuilder(className,annotatedMethod);
				writeGeneratedCodeToFile(pkgName,constructorEventBuilder.getEventClass());
			}
			String pkgName =annotatedClass.getPackageName()+".aggregate";
			AggregateBuilder aggregateBuilder = new AggregateBuilder(annotatedClass);
			writeGeneratedCodeToFile(pkgName,aggregateBuilder.getAggregateClass());
			
			pkgName =annotatedClass.getPackageName()+".test";
			TestAggregateBuilder testAggregateBuilder = new TestAggregateBuilder(annotatedClass);
			writeGeneratedCodeToFile(pkgName,testAggregateBuilder.getAggregateClass());

		}

	}

	@Override
	public String toString() {
		return "AxonGroupedClasses [classMap=" + classMap
				+ ", annotatedClassList=" + annotatedClassList + "]";
	}

	

}
