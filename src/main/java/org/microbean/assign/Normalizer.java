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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An <strong>experimental</strong>, simple, mutable, concurrent cache of objects.
 *
 * @param <T> the type of object to be normalized
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #normalize(Object, Object)
 */
public final class Normalizer<T> {

  private final Map<Object, T> cache;

  /**
   * Creates a new {@link Normalizer}.
   *
   * @see #Normalizer(ConcurrentMap)
   */
  public Normalizer() {
    this(null);
  }

  /**
   * Creates a new {@link Normalizer}.
   *
   * @param cache a {@link ConcurrentMap} that will be used as the internal cache; may be {@code null} in which case a
   * default, unbounded, initially empty implementation will be used instead
   */
  public Normalizer(final ConcurrentMap<Object, T> cache) {
    super();
    this.cache = cache == null ? new ConcurrentHashMap<>() : cache;
  }

  /**
   * <dfn>Normalizes</dfn> the supplied {@code element} such that if this {@link Normalizer} already has an {@linkplain
   * Object#equals(Object) equivalent} cached element, the cached element is returned, and, if it does not, the supplied
   * {@code element} is cached indefinitely and returned.
   *
   * @param element the element to normalize; may be {@code null} in which case {@code null} is returned
   *
   * @return the supplied {@code element} or a previously cached {@linkplain Object#equals(Object) equivalent} element
   *
   * @see #normalize(Object, Object)
   */
  public final T normalize(final T element) {
    return this.normalize(element, null);
  }

  /**
   * <dfn>Normalizes</dfn> the supplied {@code element} such that if this {@link Normalizer} already has an {@linkplain
   * Object#equals(Object) equivalent} cached element, the cached element is returned, and, if it does not, the supplied
   * {@code element} is cached indefinitely and returned.
   *
   * @param element the element to normalize; may be {@code null} in which case {@code null} is returned
   *
   * @param extra additional data to include in equality comparisons; may be {@code null}; ignored if {@code element} is
   * {@code null}
   *
   * @return the supplied {@code element} or a previously cached {@linkplain Object#equals(Object) equivalent} element
   */
  @SuppressWarnings("unchecked")
  public final T normalize(final T element, final Object extra) {
    if (element == null) {
      return null;
    } else if (extra == null) {
      return this.cache.computeIfAbsent(element, k -> (T)k); // don't bother to create a Key
    }
    return this.cache.computeIfAbsent(new Key<>(element, extra), k -> ((Key<T>)k).element());
  }


  /*
   * Inner and nested classes.
   */


  private static final record Key<T>(T element, Object extra) {

    @Override // Record
    public final int hashCode() {
      if (this.element == null) {
        if (this.extra == null) {
          return 0;
        }
        return this.extra instanceof Object[] a ? Arrays.deepHashCode(a) : this.extra.hashCode();
      } else if (this.extra == null) {
        return this.element instanceof Object[] a ? Arrays.deepHashCode(a) : this.element.hashCode();
      }
      int hashCode = 17;
      int c = this.element instanceof Object[] a ? Arrays.deepHashCode(a) : this.element.hashCode();
      hashCode = 31 * hashCode + c;
      c = this.extra instanceof Object[] a ? Arrays.deepHashCode(a) : this.extra.hashCode();
      return 31 * hashCode + c;
    }

    @Override // Record
    public final boolean equals(final Object other) {
      if (other == this) {
        return true;
      } else if (other != null && this.getClass() == other.getClass()) {
        final Key<?> her = (Key<?>)other;
        return Objects.deepEquals(this.element, her.element) && Objects.deepEquals(this.extra, her.extra);
      } else {
        return false;
      }
    }

  }

}
