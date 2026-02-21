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

import java.util.List;

import java.util.function.Predicate;

import javax.lang.model.AnnotatedConstruct;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;

/**
 * An interface whose implementations bear <dfn>semantically significant annotations</dfn>.
 *
 * <p><dfn>Semantically significant</dfn> annotations are included in hashcode and equality computations.</p>
 *
 * @param <T> the type of the annotated object
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see org.microbean.construct.element.AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror,
 * java.util.function.Predicate)
 *
 * @see org.microbean.construct.element.AnnotationMirrors#containsAll(java.util.Collection, java.util.Collection,
 * java.util.function.Predicate)
 *
 * @see org.microbean.construct.element.AnnotationMirrors#hashCode(AnnotationMirror, java.util.function.Predicate)
 */
public interface Annotated<T> {

  /**
   * Returns a non-{@code null}, immutable, determinate {@link List} of <dfn>semantically significant</dfn> {@link
   * AnnotationMirror}s.
   *
   * <p>These annotations supersede any annotations that might otherwise be available from objects returned by the
   * {@link #annotated()} method.</p>
   *
   * @return a non-{@code null}, immutable, determinate {@link List} of <dfn>semantically significant</dfn> {@link
   * AnnotationMirror}s.
   */
  public List<AnnotationMirror> annotations();

  /**
   * Returns the non-{@code null}, determinate, annotated object.
   *
   * @return the non-{@code null}, determinate, annotated object.
   */
  public T annotated();

  /**
   * A convenience method that returns a new {@link Annotated} implementation.
   *
   * @param <T> the type of {@link AnnotatedConstruct}
   *
   * @param ac a non-{@code null} {@link AnnotatedConstruct}
   *
   * @return a new, non-{@code null} {@link Annotated} implementation
   *
   * @exception NullPointerException if {@code ac} is {@code null}
   *
   * @see #of(AnnotatedConstruct, Predicate)
   */
  public static <T extends AnnotatedConstruct> Annotated<T> of(final T ac) {
    return of(ac, null);
  }

  /**
   * A convenience method that returns a new {@link Annotated} implementation.
   *
   * @param <T> the type of {@link AnnotatedConstruct}
   *
   * @param ac a non-{@code null} {@link AnnotatedConstruct}
   *
   * @param p a {@link Predicate} that returns {@code true} if a given {@link ExecutableElement}, representing an
   * annotation element, is to be included in comparison operations; may be {@code null} in which case it is as if
   * {@code e -> true} were supplied instead
   *
   * @return a new, non-{@code null} {@link Annotated} implementation
   *
   * @exception NullPointerException if {@code ac} is {@code null}
   */
  public static <T extends AnnotatedConstruct> Annotated<T> of(final T ac, final Predicate<? super ExecutableElement> p) {
    return new CacheableAnnotatedConstruct<T>(ac, p);
  }

}
