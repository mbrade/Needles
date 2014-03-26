/*
 * Copyright (c) 2013,
 * Marco Brade
 * 							[https://sourceforge.net/users/mbrade],
 * Stephan Huth
 * 							[https://sourceforge.net/users/shuth]
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

package net.sf.needles.aggregation.keycreator;

class NameAggregationKey implements AggregationKey {

    private String name;
    private static final long serialVersionUID = 1L;

    NameAggregationKey(final String name) {
        this.name = name;
    }

    @Override
    public int compareTo(final Object obj) {
        if (this == obj) {
    	return 0;
        }
        if (obj == null) {
    	return -1;
        }
        if (getClass() != obj.getClass()) {
    	return -1;
        }
        final NameAggregationKey other = (NameAggregationKey) obj;
        if (name == null) {
    	if (other.name != null) {
    	    return 1;
    	}
        } else if (other.name != null) {
    	return name.compareTo(other.name);
        } else {
    	return -1;
        }
        return 0;
    }

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
        final NameAggregationKey other = (NameAggregationKey) obj;
        if (name == null) {
    	if (other.name != null) {
    	    return false;
    	}
        } else if (!name.equals(other.name)) {
    	return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

}