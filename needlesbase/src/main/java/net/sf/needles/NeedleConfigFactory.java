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

public abstract class NeedleConfigFactory {

    private static InheritableThreadLocal<NeedleConfigContext> needleConfigThreadLocal = new InheritableThreadLocal<NeedleConfigContext>() {
	@Override
	protected NeedleConfigContext initialValue() {
	    return new NeedleConfigContext(defaultNeedleConfig);
	}
    };
    private static NeedleConfig defaultNeedleConfig = NeedleConfig.QUIET;

    private NeedleConfigFactory() {
    }

    public static NeedleConfig getNeedleConfig() {
	return doGetNeedleConfig();
    }

    public static String getSessionId() {
	final NeedleConfigContext needleConfigContext = needleConfigThreadLocal.get();
	return needleConfigContext.getSessionId();
    }

    public static boolean isContextEnabled() {
	return getNeedleConfig().getConfigId() >= NeedleConfig.CONTEXT.getConfigId();
    }

    public static boolean isDebugEnabled() {
	return getNeedleConfig().getConfigId() >= NeedleConfig.DEBUG.getConfigId();
    }

    public static boolean isMeasuremtEnabled() {
	return getNeedleConfig().getConfigId() >= NeedleConfig.MEASUREMENT.getConfigId();
    }

    public static void resetNeedleConfig() {
	needleConfigThreadLocal.remove();
    }

    public static void setDefaultNeedleConfig(final NeedleConfig defaultNeedleConfig) {
	NeedleConfigFactory.defaultNeedleConfig = defaultNeedleConfig;
    }

    public static void setNeedleConfig(final NeedleConfig needleConfig) {
	final NeedleConfigContext needleConfigContext = needleConfigThreadLocal.get();
	needleConfigContext.setNeedleConfig(needleConfig);
    }

    public static void setSessionId(final String sessionId) {
	final NeedleConfigContext needleConfigContext = needleConfigThreadLocal.get();
	needleConfigContext.setSessionId(sessionId);
    }

    private static NeedleConfig doGetNeedleConfig() {
	final NeedleConfigContext needleConfigContext = needleConfigThreadLocal.get();
	return needleConfigContext.getNeedleConfig();
    }

    protected static class NeedleConfigContext {
	private NeedleConfig needleConfig;
	private String sessionId = null;

	public NeedleConfigContext(final NeedleConfig needleConfig) {
	    this.needleConfig = needleConfig;
	}

	public NeedleConfig getNeedleConfig() {
	    return needleConfig;
	}

	public String getSessionId() {
	    return sessionId;
	}

	public void setNeedleConfig(final NeedleConfig needleConfig) {
	    this.needleConfig = needleConfig;
	}

	public void setSessionId(final String sessionId) {
	    this.sessionId = sessionId;
	}

    }
}
