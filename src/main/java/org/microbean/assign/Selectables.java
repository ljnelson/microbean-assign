/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2025–2026 microBean™.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.ConcurrentHashMap;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.lang.model.AnnotatedConstruct;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import static java.util.Objects.requireNonNull;

/**
 * Utility methods for working with {@link Selectable}s.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see Selectable
 */
public final class Selectables {

  private Selectables() {
    super();
  }

  /**
   * Returns a {@link Selectable} that caches its results.
   *
   * <p>The cache is unbounded.</p>
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param selectable a {@link Selectable}; must not be {@code null}
   *
   * @return a non-{@code null} {@link Selectable}
   *
   * @exception NullPointerException if {@code selectable} is {@code null}
   *
   * @see #caching(Selectable, BiFunction)
   */
  public static <C, E> Selectable<C, E> caching(final Selectable<? super C, E> selectable) {
    final Map<C, List<E>> selectionCache = new ConcurrentHashMap<>();
    return Selectables.<C, E>caching(selectable, selectionCache::computeIfAbsent);
  }

  /**
   * Returns a {@link Selectable} that caches its results.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param selectable a {@link Selectable}; must not be {@code null}
   *
   * @param f a {@link BiFunction} that returns a cached result, computing it on demand via its supplied mapping {@link
   * Function} if necessary; must not be {@code null}; normally safe for concurrent use by multiple threads; often a
   * reference to the {@link ConcurrentHashMap#computeIfAbsent(Object, Function)} method
   *
   * @return a non-{@code null} {@link Selectable}
   *
   * @exception NullPointerException if {@code selectable} or {@code f} is {@code null}
   *
   * @see ConcurrentHashMap#computeIfAbsent(Object, Function)
   */
  public static <C, E> Selectable<C, E> caching(final Selectable<? super C, E> selectable,
                                                final BiFunction<? super C, Function<? super C, ? extends List<E>>, ? extends List<E>> f) {
    return c -> f.apply(c, selectable::select);
  }
  
  /**
   * An <strong>experimental</strong> method that converts a {@link Selectable} accepting {@link Annotated
   * Annotated&lt;AnnotatedConstruct&gt;} instances into a {@link Selectable} accepting {@link AnnotatedConstruct}
   * instances.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param s a non-{@code null} {@link Selectable} accepting {@link Annotated Annotated&lt;AnnotatedConstruct&gt;}
   * instances
   *
   * @return a non-{@code null}, determinate {@link Selectable} accepting {@link AnnotatedConstruct} instances
   *
   * @exception NullPointerException if {@code s} is {@code null}
   *
   * @see #convert(Selectable, Predicate)
   *
   * @see Annotated
   *
   * @see AnnotatedConstruct
   */
  @Deprecated(forRemoval = true) // Annotated.of(AnnotatedConstruct) exists
  public static <C extends AnnotatedConstruct, E> Selectable<C, E> convert(final Selectable<? super Annotated<C>, E> s) {
    return convert(s, null);
  }
  
  /**
   * An <strong>experimental</strong> method that converts a {@link Selectable} accepting {@link Annotated
   * Annotated&lt;AnnotatedConstruct&gt;} instances into a {@link Selectable} accepting {@link AnnotatedConstruct}
   * instances.
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param s a non-{@code null} {@link Selectable} accepting {@link Annotated Annotated&lt;AnnotatedConstruct&gt;}
   * instances
   *
   * @param annotationElementInclusionPredicate a {@link Predicate} that returns {@code true} if a given {@link
   * ExecutableElement}, representing an annotation element, is to be included in comparison operations; may be {@code
   * null} in which case it is as if {@code e -> true} were supplied instead
   *
   * @return a non-{@code null}, determinate {@link Selectable} accepting {@link AnnotatedConstruct} instances
   *
   * @exception NullPointerException if {@code s} is {@code null}
   *
   * @see Annotated
   *
   * @see Annotated#of(AnnotatedConstruct, Predicate)
   *
   * @see AnnotatedConstruct
   */
  @Deprecated(forRemoval = true) // Annotated.of(AnnotatedConstruct) exists
  public static <C extends AnnotatedConstruct, E> Selectable<C, E> convert(final Selectable<? super Annotated<C>, E> s,
                                                                           final Predicate<? super ExecutableElement> annotationElementInclusionPredicate) {
    return ac -> s.select(Annotated.of(ac, annotationElementInclusionPredicate));
  }

  /**
   * Returns a {@link Selectable} whose {@link Selectable#select(Object)} method always returns an {@linkplain List#of()
   * empty, immutable <code>List</code>}.
   *
   * <p>This method is useful primarily for completeness and for testing pathological situations.</p>
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @return a non-{@code null} {@link Selectable}
   */
  public static final <C, E> Selectable<C, E> empty() {
    return Selectables::empty;
  }

  private static final <C, E> List<E> empty(final C ignored) {
    return List.of();
  }

  /**
   * Returns a {@link Selectable} using the supplied {@link Collection} as its elements, and the supplied {@link
   * BiPredicate} as its <em>selector</em>.
   *
   * <p>There is no guarantee that this method will return new {@link Selectable} instances.</p>
   *
   * <p>The {@link Selectable} instances returned by this method may or may not cache their selections.</p>
   *
   * <p>The selector tests its first argument to see if it is <dfn>selected</dfn> by its second argument. The selector
   * is invoked repeatedly. If, for any given invocation, the first argument is selected, the selected element is added
   * to the selection that is eventually returned as a sublist of the supplied {@link Collection}. The selector must
   * additionally be idempotent and must produce a determinate value when given the same arguments.</p>
   *
   * <p>No validation of these semantics of the selector is performed.</p>
   *
   * @param <C> the criteria type
   *
   * @param <E> the element type
   *
   * @param collection a {@link Collection} of elements from which sublists may be selected; must not be {@code null}
   *
   * @param p the selector; must not be {@code null}
   *
   * @return a {@link Selectable}; never {@code null}
   *
   * @exception NullPointerException if either {@code collection} or {@code p} is {@code null}
   */
  @SuppressWarnings("unchecked")
  public static <C, E> Selectable<C, E> filtering(final Collection<? extends E> collection,
                                                  final BiPredicate<? super E, ? super C> p) {
    requireNonNull(p, "p");
    return collection.isEmpty() ? empty() : c -> (List<E>)collection.stream().filter(e -> p.test(e, c)).toList();
  }

  /*
   * An <strong>experimental</strong> convenience method that returns a non-{@code null}, determinate {@link Selectable}
   * representing the composition of the supplied {@link Selectable} with the supplied {@code argumentTransformer} {@link
   * Function}.
   *
   * @param <B> the (criteria) type of the sole parameter of the returned {@link Selectable}
   *
   * @param <C> the (criteria) type of the sole parameter of the supplied {@link Selectable}
   *
   * @param <E> the element type of both {@link Selectable}s
   *
   * @param selectable a non-{@code null} {@link Selectable}
   *
   * @param argumentTransformer a non-{@code null} {@link Function} that maps its sole parameter (of type {@code B}) to
   * a value of type {@code C} suitable for supplying as criteria to the supplied {@link Selectable}; must be idempotent
   * and return a determinate value
   *
   * @return a non-{@code null}, determinate, composed {@link Selectable}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see Function#compose(Function)
   */
  /*
  public static <B, C, E> Selectable<B, E> compose(final Selectable<? super C, E> selectable,
                                                   final Function<? super B, ? extends C> argumentTransformer) {
    return ((Function<C, List<E>>)selectable::select).compose(argumentTransformer)::apply;
  }
  */

}
