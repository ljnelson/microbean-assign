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

import java.util.function.BiPredicate;

/**
 * A {@link BiPredicate} with particular semantics associated with its {@link #test(Object, Object) test(Object,
 * Object)} method.
 *
 * @param <C> the criteria object
 *
 * @param <T> the object being tested
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #test(Object, Object)
 */
@FunctionalInterface
public interface Matcher<C, T> extends BiPredicate<C, T> {

  /**
   * Returns {@code true} if and only if the second argument <dfn>matches</dfn> the first argument.
   *
   * <p>The order of arguments may therefore be significant for {@link Matcher} implementations that do not represent
   * equality tests.</p>
   *
   * @param c an object serving as a kind of criteria; must not be {@code null}
   *
   * @param t an object to test against the criteria; must not be {@code null}
   *
   * @return {@code true} if and only if the second argument <dfn>matches</dfn> the first argument; {@code false}
   * otherwise
   *
   * @exception NullPointerException if either {@code c} or {@code t} is {@code null}
   *
   * @exception IllegalArgumentException if either non-{@code null} argument is unsuitable for any reason
   */
  @Override // BiPredicate<C, T>
  public boolean test(final C c, final T t);

}
