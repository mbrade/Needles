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

package net.sf.needles;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * The Class NeedleUncaughtExceptionHandler.
 */
public class NeedleUncaughtExceptionHandler implements UncaughtExceptionHandler {

    private UncaughtExceptionHandler handler;

    private NeedleUncaughtExceptionHandler() {
    }

    private NeedleUncaughtExceptionHandler(final UncaughtExceptionHandler handler) {
	this.handler = handler;
    }

    /**
     * Register an {@link NeedleUncaughtExceptionHandler}
     */
    public static void registerNeedleUncaughtExceptionHandler() {
	registerNeedleUncaughtExceptionHandler(Thread.currentThread());
    }

    /**
     * Register log uncaught exception handler.
     * 
     * @param t
     *            the thread to register the {@link NeedleUncaughtExceptionHandler}
     */
    public static void registerNeedleUncaughtExceptionHandler(final Thread t) {
	UncaughtExceptionHandler handler = t.getUncaughtExceptionHandler();
	if (handler == null) {
	    handler = Thread.getDefaultUncaughtExceptionHandler();
	}
	final NeedleUncaughtExceptionHandler logHandler = new NeedleUncaughtExceptionHandler(handler);
	t.setUncaughtExceptionHandler(logHandler);
    }

    public static void unregisterLogUncaughtExceptionHandler() {
	unregisterLogUncaughtExceptionHandler(Thread.currentThread());
    }

    public static void unregisterLogUncaughtExceptionHandler(final Thread t) {
	if (t.getUncaughtExceptionHandler() instanceof NeedleUncaughtExceptionHandler) {
	    final NeedleUncaughtExceptionHandler logHandler = ((NeedleUncaughtExceptionHandler) t.getUncaughtExceptionHandler());
	    if (Thread.getDefaultUncaughtExceptionHandler() != null) {
		if (Thread.getDefaultUncaughtExceptionHandler() != logHandler.getUncaughtExceptionHandler()) {
		    t.setUncaughtExceptionHandler(logHandler.getUncaughtExceptionHandler());//sets null or the prior exceptionhandler
		} else {
		    t.setUncaughtExceptionHandler(null);
		}
	    } else {
		t.setUncaughtExceptionHandler(logHandler.getUncaughtExceptionHandler());
	    }
	}
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
	return handler;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
     */
    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
	try {
	    final Needle needle = NeedleContext.getCurrentNeedle();
	    if (needle != null && needle.getNeedleState() == NeedleState.STARTED) {
		needle.abort(e);
	    }
	} catch (final Exception ex) {
	    //TODO add log
	} finally {
	    if (handler != null) {
		handler.uncaughtException(t, e);
	    }
	}
    }
}
