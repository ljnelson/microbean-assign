/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2026 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.assign;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.microbean.construct.DefaultDomain;
import org.microbean.construct.Domain;

import org.microbean.construct.type.UniversalType;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.assign.Types.erasedName;

final class TestSupertypesWithTypeUseAnnotations {

  private Domain domain;

  private Types types;

  private TestSupertypesWithTypeUseAnnotations() {
    super();
  }

  @BeforeEach
  final void setup() {
    this.domain = new DefaultDomain();
    this.types = new Types(this.domain);
  }

  @Test
  final void test() {
    final TypeElement te = this.domain.typeElement(B.class.getCanonicalName());
    // Surprising, but see https://mail.openjdk.org/pipermail/type-annotations-spec-experts/2013-November/000174.html:
    //
    //   "JSR 308 introduced ElementType.TYPE_USE as pertaining not only to uses of types but also to declarations of
    //   types; logically, it's a 'supertype' [superset?] of ElementType.TYPE."
    //
    assertEquals(1, te.getAnnotationMirrors().size());

    final DeclaredType dt = (DeclaredType)te.asType();

    // The TYPE_USE annotation is also not "propagated" to the TypeMirror "underlying" the TypeElement representing the
    // declaration. Is this surprising? I guess not?
    assertEquals(0, dt.getAnnotationMirrors().size());

    final DeclaredType superclassType = (DeclaredType)te.getSuperclass();
    final TypeElement superclassElement = (TypeElement)superclassType.asElement();
    assertEquals(this.domain.typeElement(A.class.getCanonicalName()), superclassElement);

    // Makes sense; A has no annotations on it
    assertTrue(superclassElement.getAnnotationMirrors().isEmpty());

    // Makes sense; the-usage-of-A-in-B's-extends-clause has an annotation on it
    assertEquals(1, superclassType.getAnnotationMirrors().size());

    // OK, we're using Domain. That means we can do evil awful things with annotations. For example, we could propagate
    // declaration annotations to type usage ones:
    ((UniversalType)dt).getAnnotationMirrors().addAll(te.getAnnotationMirrors());

    // Now dt has declaration annotations:
    assertEquals(1, dt.getAnnotationMirrors().size());

    // This shouldn't change anything:
    assertTrue(domain.sameType(domain.typeElement(B.class.getCanonicalName()).asType(), // fresh copy
                               dt));
    
  }

  @Test
  final void test2() {
    final TypeElement te = this.domain.typeElement(C.class.getCanonicalName());
    List<? extends TypeMirror> directSupertypes = this.domain.directSupertypes(te.asType());
    assertEquals(1, directSupertypes.size());
    final DeclaredType bType = (DeclaredType)directSupertypes.get(0);
    assertTrue(bType.getAnnotationMirrors().isEmpty());
    directSupertypes = this.domain.directSupertypes(bType);
    assertEquals(1, directSupertypes.size());
    final DeclaredType aType = (DeclaredType)directSupertypes.get(0);
    assertEquals(1, aType.getAnnotationMirrors().size()); // type use annotations are preserved
  }
  
  @Target(TYPE_USE)
  @Retention(RUNTIME)
  private static @interface Gorp {}

  private static sealed class A permits B {

  }

  @Gorp
  private static sealed class B extends @Gorp A permits C {

  }

  private static final class C extends B {

  }

}
