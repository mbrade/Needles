/*
 * Copyright (c) 2013,
 * Marco Brade
 * 							[https://github.com/mbrade]
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.sf.needles;

import junit.framework.Assert;

import org.junit.Test;

public class TestNeedleId {

    @Test
    public void testCompareTo() {
	final NeedleId id = new NeedleId(new byte[] { 0 });
	final NeedleId id2 = new NeedleId(new byte[] { 0 });
	Assert.assertEquals(0, id.compareTo(id2));
	Assert.assertEquals(1, id.compareTo(new NeedleId(new byte[] { 1 })));
	Assert.assertEquals(-1, new NeedleId(new byte[] { 1 }).compareTo(id));
    }

    @Test
    public void testEquals() {
	final NeedleId id = new NeedleId(new byte[] { 0 });
	final NeedleId id2 = new NeedleId(new byte[] { 0 });
	Assert.assertNotSame(id, id2);
	Assert.assertEquals(id, id2);
	Assert.assertFalse(id.equals(new NeedleId(new byte[] { 1 })));
	Assert.assertFalse(id.equals(new Object()));
	Assert.assertFalse(id.equals(null));
    }

    @Test
    public void testToString() {
	Assert.assertEquals("0001", new NeedleId(new byte[] {
	                                                     0,
	                                                     1 }).toString());
    }

}
