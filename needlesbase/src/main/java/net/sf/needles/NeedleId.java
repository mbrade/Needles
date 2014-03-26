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

import java.io.Serializable;

import net.sf.needles.aggregation.keycreator.AggregationKey;

public class NeedleId implements Serializable, AggregationKey {

    private static final long serialVersionUID = 1L;
    private byte[] value;
    public final static NeedleId EMPTY_LOG_ID = new NeedleId(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

    public NeedleId() {
	this.value = EMPTY_LOG_ID.value;
    }

    public NeedleId(final byte[] value) {
	this.value = value;
    }

    @Override
    public int compareTo(final Object o) {
	if (this == o) {
	    return 0;
	}
	if (o == null) {
	    return -1;
	}
	if (getClass() != o.getClass()) {
	    return -1;
	}
	final NeedleId other = (NeedleId) o;
	if (value == other.value) {
	    return 0;
	}
	if (value == null) {
	    if (other.value != null) {
		return 1;
	    }
	} else {
	    if (other.value == null) {
		return -1;
	    }
	    if (value.length < other.value.length) {
		return 1;
	    } else if (value.length == other.value.length) {
		for (int i = 0; i < value.length; i++) {
		    if (value[i] != other.value[i]) {
			return value[i] < other.value[i] ? 1 : -1;
		    }
		}
	    } else {
		return -1;
	    }
	}
	return toString().compareTo(o.toString());
    }

    //    @Override
    //    public int compareTo(final Object o) {
    //
    //	return toString().compareTo(o.toString());
    //    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final NeedleId other = (NeedleId) obj;
	if (value == other.value) {
	    return true;
	}
	if (value == null || other.value == null) {
	    return false;
	}
	final int length = value.length;
	if (other.value.length != length) {
	    return false;
	}
	for (int i = 0; i < length; i++) {
	    if (value[i] != other.value[i]) {
		return false;
	    }
	}

	return true;
    }

    @Override
    public int hashCode() {
	int result = 0;
	for (final byte element : value) {
	    result = 31 * result + element;
	}
	return result;
    }

    @Override
    public String toString() {
	final StringBuilder sbMd5Hash = new StringBuilder();
	for (final byte element : value) {
	    sbMd5Hash.append(Character.forDigit((element >> 4) & 0xf, 16));
	    sbMd5Hash.append(Character.forDigit(element & 0xf, 16));
	}
	return sbMd5Hash.toString();
    }

}
