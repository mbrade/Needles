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

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class TestNeedle {

    @Before
    public void before() {
	NeedleContext.cleanup();
	NeedleContext.registerUncaughtExceptionHandler(false);
	NeedleConfigFactory.setDefaultNeedleConfig(NeedleConfig.DEBUG);
    }

    @Test
    public void testAppendLog() throws InterruptedException, BrokenBarrierException {
	NeedleContext.cleanup();
	final Needle[] logs = new Needle[100];
	final CyclicBarrier barrier = new CyclicBarrier(logs.length + 1);
	final Semaphore sema = new Semaphore(logs.length + 1);
	Needle.start("main");
	for (int i = 0; i < logs.length; i++) {
	    final int x = i;
	    final Thread th = new Thread() {
		@Override
		public void run() {
		    try {
			sema.acquire();
			barrier.await();
			Needle.start("Thread " + x);
			Needle.stopCurrentNeedle();
		    } catch (final Exception e) {
			e.printStackTrace();
		    } finally {
			logs[x] = NeedleContext.getCurrentNeedle();
			sema.release();
		    }
		}
	    };
	    th.start();
	}
	barrier.await();
	sema.acquire(logs.length + 1);

	Assert.assertEquals(1, NeedleContext.getRootNeedles().size());
	Assert.assertEquals(0, NeedleContext.getCurrentNeedle().getChildren().size());
	for (int i = 0; i < logs.length; i++) {
	    NeedleContext.addToCurrentNeedleStack(logs[i]);
	}
	Needle.stopCurrentNeedle();
	Assert.assertEquals(logs.length, NeedleContext.getCurrentNeedle().getChildren().size());
    }

    @Test
    public void testChildLogging() {
	NeedleContext.cleanup();
	final Needle needle = Needle.start("testMethod");
	needle.stop();
	Assert.assertEquals(1, NeedleContext.getRootNeedles().size());
	final Needle needle2 = Needle.start("testMethod2");
	needle2.stop();
	Assert.assertNull(needle2.getParentNeedle());
	Assert.assertEquals(2, NeedleContext.getRootNeedles().size());
	final Needle needle3 = Needle.start("testMethod2");
	needle3.stop();
	Assert.assertNull(needle3.getParentNeedle());
	Assert.assertEquals(3, NeedleContext.getRootNeedles().size());
	final int c = 2;
	for (int i = 0; i < c; i++) {
	    final Needle needle4 = Needle.start("testMethod4");
	    final Needle needle5 = Needle.start("testMethod5");
	    needle5.stop();
	    needle4.stop();
	    Assert.assertNull(needle4.getParentNeedle());
	    Assert.assertNotNull(needle5.getParentNeedle());
	    Assert.assertEquals(needle4, needle5.getParentNeedle());
	    Assert.assertEquals(1, needle4.getChildren().size());
	    Assert.assertEquals(needle5, needle4.getChildren().get(0));
	}
	Assert.assertEquals(3 + c, NeedleContext.getRootNeedles().size());
    }

    @Test
    public void testDebugLines() {
	final Needle needle = Needle.start("name");
	needle.debug("1");
	needle.debug("2");
	needle.debug("3");
	Assert.assertEquals(3, needle.getDebugLines().size());
	Assert.assertEquals("1", needle.getDebugLines().get(0));
	Assert.assertEquals("2", needle.getDebugLines().get(1));
	Assert.assertEquals("3", needle.getDebugLines().get(2));
    }

    @Test
    public void testDoubleStart() {
	final Needle needle = Needle.start("tst");
	try {
	    needle.start();
	    Assert.fail("Exception expected.");
	} catch (final IllegalStateException ise) {
	}
    }

    @Test
    public void testDoubleStop() {
	Needle.start("tst");
	Needle.stopCurrentNeedle();
	try {
	    Needle.stopCurrentNeedle();
	    Assert.fail("Exception expected.");
	} catch (final IllegalStateException ise) {
	}
    }

    @Test
    public void testExceptionHandler() throws InterruptedException, BrokenBarrierException {
	Assert.assertTrue(!(Thread.currentThread().getUncaughtExceptionHandler() instanceof NeedleUncaughtExceptionHandler));
	NeedleContext.cleanup();
	NeedleContext.registerUncaughtExceptionHandler(true);
	final CyclicBarrier barrier = new CyclicBarrier(2);
	final Semaphore sema = new Semaphore(2);
	Needle.start("1");
	final Needle[] logs = new Needle[1];
	final Thread t = new Thread("LogTestThread") {
	    @Override
	    public void run() {
		try {
		    try {
			sema.acquire();
			barrier.await();
		    } catch (final Exception e) {
		    }
		    logs[0] = Needle.start("2");
		    Assert.assertEquals(NeedleUncaughtExceptionHandler.class, Thread.currentThread().getUncaughtExceptionHandler().getClass());
		    throw new RuntimeException("This exception has been raised for testing and is expected behavior.");
		} finally {
		    sema.release();
		}
	    }
	};
	t.start();
	barrier.await();
	sema.acquire(2);
	int x = 0;
	while (!logs[0].isAborted()) {
	    synchronized (this) {
		try {
		    this.wait(100);
		} catch (final InterruptedException e) {
		}
	    }
	    if (x++ > 100) {
		break;
	    }
	}
	Assert.assertEquals(1, NeedleContext.getRootNeedles().size());
	Assert.assertEquals(0, NeedleContext.getCurrentNeedle().getChildren().size());
	NeedleContext.addToCurrentNeedleStack(logs[0]);
	Assert.assertTrue("The log has no aborted state.", logs[0].isAborted());
	Assert.assertEquals(1, NeedleContext.getCurrentNeedle().getChildren().size());
	Needle.stopCurrentNeedle();
	Assert.assertEquals(1, NeedleContext.getRootNeedles().size());
	Assert.assertEquals(NeedleUncaughtExceptionHandler.class, Thread.currentThread().getUncaughtExceptionHandler().getClass());
	NeedleContext.cleanup();
	Assert.assertTrue(!(Thread.currentThread().getUncaughtExceptionHandler() instanceof NeedleUncaughtExceptionHandler));
    }

    @Test
    public void testIllegalStop() {
	final Needle needle = new Needle("test");
	try {
	    needle.stop();
	    Assert.fail("Stop not allowed before start.");
	} catch (final IllegalStateException ise) {
	}
    }

    @Test
    public void testLogBasics() {
	final Needle needle = new Needle("Some name", "Context1", "Context2");
	Assert.assertFalse(needle.isAborted());
	Assert.assertFalse(needle.isStarted());
	Assert.assertFalse(needle.isStopped());
	Assert.assertNull(needle.getStartStackTraceElement());
	Assert.assertNull(needle.getStopStackTraceElement());
	Assert.assertNull(needle.getId());
	needle.start();
	Assert.assertTrue(needle.isStarted());
	Assert.assertFalse(needle.isStopped());
	Assert.assertNotNull(needle.getId());
	Assert.assertNotNull(needle.getStartStackTraceElement());
	Assert.assertNull(needle.getStopStackTraceElement());
	needle.stop();
	Assert.assertTrue(needle.isStopped());
	Assert.assertNotNull(needle.getStopStackTraceElement());
	Assert.assertEquals(2, needle.getContext().length);
	Assert.assertFalse(needle.isAborted());
	try {
	    needle.abort(null);
	} catch (final Exception ise) {
	    Assert.fail("Expect no Exception on a log calling abort while state is stopped.");
	}
	try {
	    needle.start();
	    Assert.fail("Expected exception on a log calling start while state is stopped.");
	} catch (final IllegalStateException ise) {

	}
    }

    @Test
    public void testManualAbortWithException() {
	NeedleContext.cleanup();
	final Needle needle = Needle.start("Name");
	needle.debug("Dummy Message");//don't remove this line!
	needle.abort(new Exception());
	//	System.out.println(log.getStopStackTraceElement().getLineNumber());
	Assert.assertTrue(needle.isAborted());
	Assert.assertEquals(needle.getStartStackTraceElement().getLineNumber(), needle.getStopStackTraceElement().getLineNumber() - 2);
	Assert.assertEquals(needle.getStartStackTraceElement().getMethodName(), needle.getStopStackTraceElement().getMethodName());
	Assert.assertEquals(needle.getStartStackTraceElement().getClassName(), needle.getStopStackTraceElement().getClassName());
    }

    @Test
    public void testManualAbortWithoutException() {
	NeedleContext.cleanup();
	final Needle needle = Needle.start("Name");
	needle.abort(null);
	Assert.assertTrue(needle.isAborted());
	Assert.assertEquals(needle.getStartStackTraceElement().getLineNumber(), needle.getStopStackTraceElement().getLineNumber() - 1);
	Assert.assertEquals(needle.getStartStackTraceElement().getMethodName(), needle.getStopStackTraceElement().getMethodName());
	Assert.assertEquals(needle.getStartStackTraceElement().getClassName(), needle.getStopStackTraceElement().getClassName());
    }
}
