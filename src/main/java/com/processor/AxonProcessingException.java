package com.processor;

import javax.lang.model.element.Element;

/**
 * @author Hannes Dorfmann
 */
public class AxonProcessingException extends Exception {

  Element element;

  public AxonProcessingException(Element element, String msg, Object... args) {
    super(String.format(msg, args));
    this.element = element;
  }

  public Element getElement() {
    return element;
  }
}
