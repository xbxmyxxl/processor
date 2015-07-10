package com.processor.parse;

import java.util.ArrayList;
import java.util.List;

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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import com.processor.AxonProcessingException;

public class AxonAnnotatedClass {
	private TypeElement classElement;
	
	private String qualifiedGroupClassName;
	private String simpleFactoryGroupName;
	private String packageName;
	private String className;
	private TypeMirror classType;
	
	private List<AxonAnnotatedMethod> classModifierMethods = new ArrayList<AxonAnnotatedMethod>();
	private List<AxonAnnotatedMethod> classAccessorMethods = new ArrayList<AxonAnnotatedMethod>();
	private List<AxonAnnotatedMethod> classConstructorMethods = new ArrayList<AxonAnnotatedMethod>();

	private String lowerClassName;

	

	public AxonAnnotatedClass(TypeElement classElement, Elements elementUtils)
			throws AxonProcessingException {
		this.classElement = classElement;
		this.className = classElement.getSimpleName().toString();
		this.lowerClassName =Character.toLowerCase(className.charAt(0)) + className.substring(1); 
		this.classType = classElement.asType();
		PackageElement pkg = elementUtils.getPackageOf(classElement);
		packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();
	}/*
	public void addConstructor()
	{
		classConstructors.add(constructor);
	}*/
	public void addConstructorMethod(AxonAnnotatedMethod constructor)
	{
		classConstructorMethods.add(constructor);
	}
	
	public void addModifierMethod(AxonAnnotatedMethod method)
	{
		classModifierMethods.add(method);
	}
	
	public void addAccessorMethod(AxonAnnotatedMethod method)
	{
		classAccessorMethods.add(method);
	}
	public String getQualifiedGroupClassName() {
		return qualifiedGroupClassName;
	}
	public void setQualifiedGroupClassName(String qualifiedGroupClassName) {
		this.qualifiedGroupClassName = qualifiedGroupClassName;
	}
	public String getSimpleFactoryGroupName() {
		return simpleFactoryGroupName;
	}
	public void setSimpleFactoryGroupName(String simpleFactoryGroupName) {
		this.simpleFactoryGroupName = simpleFactoryGroupName;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public List<AxonAnnotatedMethod> getClassMethods() {
		return classModifierMethods;
	}
	public void setClassMethods(List<AxonAnnotatedMethod> classMethods) {
		this.classModifierMethods = classMethods;
	}
	public List<AxonAnnotatedMethod> getClassConstructors() {
		return classConstructorMethods;
	}
	public void setClassConstructors(List<AxonAnnotatedMethod> classConstructors) {
		this.classConstructorMethods = classConstructors;
	}
	public List<AxonAnnotatedMethod> getClassModifierMethods() {
		return classModifierMethods;
	}
	public void setClassModifierMethods(
			List<AxonAnnotatedMethod> classModifierMethods) {
		this.classModifierMethods = classModifierMethods;
	}
	public List<AxonAnnotatedMethod> getClassAccessorMethods() {
		return classAccessorMethods;
	}
	public void setClassAccessorMethods(
			List<AxonAnnotatedMethod> classAccessorMethods) {
		this.classAccessorMethods = classAccessorMethods;
	}
	public TypeMirror getClassType() {
		return classType;
	}
	public void setClassType(TypeMirror classType) {
		this.classType = classType;
	}
	public List<AxonAnnotatedMethod> getClassConstructorMethods() {
		return classConstructorMethods;
	}
	public void setClassConstructorMethods(
			List<AxonAnnotatedMethod> classConstructorMethods) {
		this.classConstructorMethods = classConstructorMethods;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public void updateMethodValidator(String methodName,
			ExecutableElement commandValidator) {
		for (AxonAnnotatedMethod element : classModifierMethods) {
		   if(element.getMethodName().equals(methodName))
			   element.setCommandValidator(commandValidator);
		}
		
	}
	public String getLowerClassName() {
		return lowerClassName;
	}
	public void setLowerClassName(String lowerClassName) {
		this.lowerClassName = lowerClassName;
	}

}
