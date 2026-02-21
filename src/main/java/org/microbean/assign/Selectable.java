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

import java.util.List;

/**
 * A notional, opaque, stable collection of elements from which immutable sublists may be <dfn>selected</dfn> according
 * to some <dfn>criteria</dfn>.
 *
 * @param <C> the criteria type
 *
 * @param <E> the element type
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 */
@FunctionalInterface
public interface Selectable<C, E> {

  /**
   * <em>Selects</em> and returns an immutable {@link List} representing a sublist of this {@link Selectable}'s
   * elements, as mediated by the supplied criteria.
   *
   * <p>Implementations of this method must be idempotent and must be <dfn>stable</dfn>, <em>i. e.</em> return a
   * determinate value.</p>
   *
   * <p>Implementations of this method must not return {@code null}.</p>
   *
   * @param criteria the criteria to use; may be {@code null} to indicate no particular criteria should be used during
   * selection
   *
   * @return a non-{@code null}, immutable, determinate sublist of this {@link Selectable}'s elements effectively selected by the
   * supplied {@code criteria}
   */
  // Filters this thing according to the supplied criteria, producing a List.
  // List not Stream to permit caching
  // List not Collection so equals() is well-defined
  // List is unmodifiable and is always valid for the supplied criteria (unenforceable)
  // C and not Predicate because equality semantics for Predicate are not well-defined (caching again)
  public List<E> select(final C criteria);

}
