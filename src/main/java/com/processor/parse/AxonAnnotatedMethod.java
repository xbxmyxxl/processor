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
package com.processor.parse;

//import com.hannesdorfmann.annotationprocessing101.factory.annotation.Factory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.apache.commons.lang3.StringUtils;

import com.processor.AxonProcessingException;
import com.processor.annotations.StateAccessor;

//parser for constructor and methods
public class AxonAnnotatedMethod {

	// should contain a method
	private ExecutableElement exeElement;
	private String qualifiedGroupClassName;
	private String simpleFactoryGroupName;
	private String methodName;
	private String packageName;
	private String capitalMethodName;
	private TypeMirror methodReturn;
	private Map<String, TypeMirror> methodParam = new HashMap<String, TypeMirror>();
	

	public AxonAnnotatedMethod(ExecutableElement exeElement,Elements elementUtils)
			throws AxonProcessingException {
		this.exeElement = exeElement;

		methodReturn = exeElement.getReturnType();
		methodName = exeElement.getSimpleName().toString();
		capitalMethodName = Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
		PackageElement pkg = elementUtils.getPackageOf(exeElement.getEnclosingElement());
		packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();
		
		List<? extends VariableElement> methodParamList = (List<? extends VariableElement>) exeElement
				.getParameters();
		for (VariableElement variableElement : methodParamList) {
			TypeMirror variableType = variableElement.asType();
			String variableName = variableElement.getSimpleName().toString();
			methodParam.put(variableName,variableType);
		}

	}

	public TypeElement getAxonAnnotatedClass(ExecutableElement exeElement) {
		if (exeElement.getEnclosingElement().getKind() == ElementKind.CLASS) {
			TypeElement typeElement = (TypeElement) exeElement;
			return typeElement;
		} else
			return null;
	}

	/**
	 * Get the full qualified name of the type specified in
	 * {@link StateAccessor#type()}.
	 *
	 * @return qualified name
	 */
	public String getQualifiedFactoryGroupName() {
		return qualifiedGroupClassName;
	}

	/**
	 * Get the simple name of the type specified in {@link StateAccessor#type()}
	 * .
	 *
	 * @return qualified name
	 */
	public String getSimpleFactoryGroupName() {
		return simpleFactoryGroupName;
	}

	/**
	 * The original element that was annotated with @Factory
	 */
	public ExecutableElement getTypeElement() {
		return exeElement;
	}

	public Map<String, TypeMirror> getMethodParam() {
		return methodParam;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public TypeMirror getMethodReturn() {
		return methodReturn;
	}

	public void setMethodReturn(TypeMirror methodReturn) {
		this.methodReturn = methodReturn;
	}

	public String getCapitalMethodName() {
		return capitalMethodName;
	}

	public void setCapitalMethodName(String capitalMethodName) {
		this.capitalMethodName = capitalMethodName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
