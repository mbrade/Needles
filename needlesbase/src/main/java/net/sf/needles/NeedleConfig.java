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
import java.util.HashMap;
import java.util.Map;

public enum NeedleConfig implements Serializable {

    /**
     * Will do no measurement at all
     */
    QUIET(0),
    /**
     * Only do measurements but get no contexts
     */
    MEASUREMENT(1),
    /**
     * Will get all measurements with context informations
     */
    CONTEXT(2),
    /**
     * Will also transfer debug informations
     */
    DEBUG(3);

    private static Map<Integer, NeedleConfig> idMap = new HashMap<Integer, NeedleConfig>();

    static {
	for (final NeedleConfig nc : values()) {
	    idMap.put(nc.getConfigId(), nc);
	}
    }

    private int configId;

    private NeedleConfig(final int needleConfigValue) {
	this.configId = needleConfigValue;
    }

    public static NeedleConfig getNeedleConfigById(final int id) {
	return idMap.get(id);
    }

    public int getConfigId() {
	return configId;
    }
}
