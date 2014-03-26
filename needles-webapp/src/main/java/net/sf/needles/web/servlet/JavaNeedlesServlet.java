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

package net.sf.needles.web.servlet;

import java.io.IOException;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.needles.GlobalContext;
import net.sf.needles.Needle;
import net.sf.needles.NeedleContext;
import net.sf.needles.NeedleId;
import net.sf.needles.NeedleInfo;
import net.sf.needles.aggregation.Aggregation;
import net.sf.needles.aggregation.AggregationFactory;
import net.sf.needles.aggregation.ExecutionAggregationFactory;
import net.sf.needles.aggregation.HotspotAggregationFactory;
import net.sf.needles.aggregation.Top10Aggregation;
import net.sf.needles.aggregation.Top10AggregationFactory;
import net.sf.needles.aggregation.keycreator.NeedleIdKeyCreator;
import net.sf.needles.aggregation.worker.AggregationWorker;
import net.sf.needles.aggregation.worker.SimpleAggregationWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

@WebServlet(displayName = "Java Needles Servlet", name = "NeedlesServlet", urlPatterns = { "/needles-service/*" })
public class JavaNeedlesServlet extends HttpServlet {

    public JavaNeedlesServlet() {
	// TODO Auto-generated constructor stub
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
	// TODO Auto-generated method stub
	super.init(config);
	final AggregationWorker worker = new SimpleAggregationWorker();
	worker.addAggregationFactory(new Top10AggregationFactory(NeedleIdKeyCreator.INSTANCE, "Top 10"));
	worker.addAggregationFactory(new ExecutionAggregationFactory(NeedleIdKeyCreator.INSTANCE, "Executions"));
	worker.addAggregationFactory(new HotspotAggregationFactory());
	GlobalContext.setAggregationWorker(worker);
	GlobalContext.start();

	final Thread t = new Thread() {
	    @Override
	    public void run() {
		try {
		    final Random rand = new Random(1000);
		    while (true) {
			synchronized (this) {
			    try {
				Needle.start("test");
				sleep(RandomUtils.nextInt(1000));
				Needle.start("child-test");
				sleep(RandomUtils.nextInt(1000));
				Needle.start("another-child-test");
				sleep(RandomUtils.nextInt(1000));
				Needle.stopCurrentNeedle();
				Needle.stopCurrentNeedle();
				Needle.stopCurrentNeedle();
				Needle.start("test2");
				sleep(RandomUtils.nextInt(1000));
				Needle.start("child-test2");
				sleep(RandomUtils.nextInt(1000));
				Needle.start("another-child-test-" + rand.nextFloat());
				sleep(RandomUtils.nextInt(1000));
				Needle.stopCurrentNeedle();
				Needle.stopCurrentNeedle();
				Needle.stopCurrentNeedle();

				this.wait(2000);
			    } catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			}
		    }
		} catch (final Exception e) {
		    e.printStackTrace();
		}
	    }
	};
	t.start();
    }

    private NeedleInfo resolveNeedleViaPath(final NeedleInfo root, final Deque<String> paths) {

	final List<NeedleInfo> needleInfos = root.getChildren();
	for (final NeedleInfo ni : needleInfos) {
	    final NeedleId needleId = new NeedleId(paths.pollFirst().getBytes());
	    if (ni.getId().equals(needleId)) {
		if (paths.isEmpty()) {
		    return ni;
		} else {
		    return resolveNeedleViaPath(ni, paths);
		}
	    }
	}
	return null;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

	final MurksBean murksBean = new MurksBean();
	murksBean.doSomething("murks");
	final String path = StringUtils.defaultIfEmpty(req.getPathInfo(), StringUtils.EMPTY);
	if (path.startsWith("aggegregator")) {
	    // read out data from specified aggregator
	} else if (path.startsWith("configuration")) {
	    // goto configuration based logic to configure needle mode, aggregator configuration etc.
	} else if (path.contains("showJson")) {
	    
	    @SuppressWarnings("unchecked")
	    List<Top10Aggregation> rootAggregations = (List<Top10Aggregation>)GlobalContext.getAggregations("Top 10");
	    List<NeedleInfo> top10Needles = null;
 	    for (Top10Aggregation agg : rootAggregations) {
		top10Needles = agg.getTop10Needles();
	    }
	    System.out.println("Top10 Needles -> "+top10Needles);
 	    Result result = new Result("Top 10", top10Needles);
	    String json = result.toJson();
	    System.out.println("JSON -> "+json);
	    resp.getWriter().write(json);
	    resp.getWriter().flush();
	    NeedleContext.cleanup();
	    return;
	}
	
	    
	req.getRequestDispatcher("/WEB-INF/needles-admin/needlecenter.jsp").forward(req, resp);

	NeedleContext.cleanup();
    }
}
