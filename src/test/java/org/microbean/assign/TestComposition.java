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

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.microbean.assign.Types.erasedName;

final class TestComposition {

  private TestComposition() {
    super();
  }
  
  @Test
  final void test() {
    final Selectable<Boolean, Integer> s0 = TestComposition::select;
    final Function<Boolean, List<Integer>> f0 = s0::select;
    final Function<String, List<Integer>> f1 = f0.compose(TestComposition::stringToBoolean);
  }

  private static List<Integer> select(final Boolean ignored) {
    return List.of(Integer.valueOf(42));
  }

  private static Boolean stringToBoolean(final String s) {
    return Boolean.valueOf(s);
  }

}
