/*
 * Copyright (c) 2014. Real Time Genomics Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.rtg.util.intervals;

import java.util.ArrayList;
import java.util.List;

import com.rtg.util.intervals.RangeList.RangeData;

import junit.framework.TestCase;

/**
 */
public class RangeListTest extends TestCase {

  public void testRange() {
    RangeList.RangeData<String> r = new RangeData<>(0, 1, "blah");
    assertEquals("blah", r.getMeta().get(0));
    assertTrue(r.isInRange(0));
    assertFalse(r.isInRange(1));
    assertFalse(r.isInRange(-1));
    assertEquals("1-1", r.toString());
    r.addMeta("boo");
    assertEquals(2, r.getMeta().size());
    assertEquals("blah", r.getMeta().get(0));
    assertEquals("boo", r.getMeta().get(1));
    r = new RangeList.RangeData<>(1, 2);
    assertEquals("2-2", r.toString());
    assertEquals(1, r.getStart());
    assertEquals(2, r.getEnd());
  }

  public void testEmptyRange() {
    final RangeList.RangeData<String> r = new RangeList.RangeData<>(0, 0, "blah");
    assertEquals("blah", r.getMeta().get(0));
    assertFalse(r.isInRange(0));
    assertFalse(r.isInRange(1));
    assertFalse(r.isInRange(-1));
    assertEquals("1-0", r.toString());
    r.addMeta("boo");
    assertEquals(2, r.getMeta().size());
    assertEquals("blah", r.getMeta().get(0));
    assertEquals("boo", r.getMeta().get(1));

    final RangeList<String> search = new RangeList<>(r);

    final List<RangeList.RangeData<String>> newRanges = search.getRangeList();
    assertEquals(0, newRanges.size());

    assertNull(search.find(0));
    assertNull(search.find(1));
    assertNull(search.find(-1));
  }

  public void testRangeSearch() {
    final List<RangeList.RangeData<String>> ranges = new ArrayList<>();
    ranges.add(new RangeList.RangeData<>(1, 4, "1-4"));
    ranges.add(new RangeList.RangeData<>(3, 6, "3-6"));
    ranges.add(new RangeList.RangeData<>(30, 120, "30-120"));
    final RangeList<String> search = new RangeList<>(ranges);
    List<String> found = search.find(0);
    assertNull(found);
    found = search.find(1);
    assertEquals(1, found.size());
    assertEquals("1-4", found.get(0));
    found = search.find(3);
    assertEquals(2, found.size());
    assertEquals("1-4", found.get(0));
    assertEquals("3-6", found.get(1));
    found = search.find(4);
    assertEquals(1, found.size());
    assertEquals("3-6", found.get(0));
    search.find(6);
    assertNull(null);
    found = search.find(60);
    assertEquals(1, found.size());
    assertEquals("30-120", found.get(0));
    found = search.find(145);
    assertNull(found);
    found = search.find(400);
    assertNull(found);

    found = search.find(Integer.MIN_VALUE);
    assertNull(found);

    found = search.find(Integer.MAX_VALUE - 1);
    assertNull(found);

    found = search.find(Integer.MAX_VALUE);
    assertNull(found);
  }

}


