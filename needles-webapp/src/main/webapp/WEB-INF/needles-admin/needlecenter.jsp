<%--

    Copyright (c) 2013,
    Marco Brade
    							[https://sourceforge.net/users/mbrade],
    Stephan Huth
    							[https://sourceforge.net/users/shuth]
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright notice,
          this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright notice,
          this list of conditions and the following disclaimer in the documentation
          and/or other materials provided with the distribution.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED.
    IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
    DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--%>

<%@page import="net.sf.needles.aggregation.keycreator.AggregationKey"%>
<%@page import="net.sf.needles.renderer.RendererHelper"%>
<%@page import="net.sf.needles.NeedleStub"%>
<%@page import="net.sf.needles.aggregation.HotspotAggregation"%>
<%@page import="net.sf.needles.aggregation.HotspotAggregationFactory"%>
<%@page import="net.sf.needles.aggregation.ExecutionAggregationFactory"%>
<%@page import="net.sf.needles.aggregation.ExecutionAggregation"%>
<%@page import="net.sf.needles.web.renderer.SimpleHTMLRenderer"%>
<%@page import="net.sf.needles.NeedleInfo"%>
<%@page import="net.sf.needles.aggregation.Top10Aggregation"%>
<%@page import="net.sf.needles.aggregation.Top10AggregationFactory"%>
<%@page import="net.sf.needles.aggregation.AggregationFactory"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="java.util.List"%>
<%@page import="net.sf.needles.aggregation.Aggregation"%>
<%@page import="net.sf.needles.aggregation.worker.AggregationWorker"%>
<%@page import="java.util.Map"%>
<%@page import="net.sf.needles.GlobalContext"%>
<%@page import="net.sf.needles.NeedleContext"%>
<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>
<%!

    public Map<String, AggregationFactory<? extends Aggregation<?>>> factories = GlobalContext.getAggregationFactories();
    
    public ExecutionAggregationFactory executionAggregationFactory = null;
    {
        for (AggregationFactory<? extends Aggregation<?>> fac : factories.values()) {
            if (fac instanceof ExecutionAggregationFactory) {
                executionAggregationFactory = (ExecutionAggregationFactory) fac;
            }
        }
    }
    
    public String writeNeedle(int count, AggregationKey aggregationkey, NeedleInfo ni, StringBuilder builder) {
        RendererHelper rendererHelper = new RendererHelper();
        StringBuilder sb = builder==null?new StringBuilder():builder;
        if (count %2 == 0){
        	sb.append("<tr bgcolor=\"#CCBBAA\" data-tt-id=\"");
        }else{
        	sb.append("<tr bgcolor=\"#FFFFFF\" data-tt-id=\"");
        }
        sb.append(String.format("%1$s_%2$d_%3$d_%4$s", aggregationkey.toString(), count, ni.getDepth(), ni.getId().toString())).append("\"");
        if(ni.getParentNeedle() != null) {
        	NeedleInfo parent = ni.getParentNeedle();
            sb.append(" data-tt-parent-id=\"").append(String.format("%1$s_%2$d_%3$d_%4$s", aggregationkey.toString(), count, parent.getDepth(), parent.getId().toString())).append("\"");
        }
        sb.append(">");
        sb.append("<td>").append(ni.getName()).append(" (").append(ni.getChildCount()).append(")</td>");
        sb.append("<td>").append(rendererHelper.getFormatedDouble(ni.getDurationMillis())).append("</td>");
        sb.append("<td>").append(rendererHelper.getFormatedDateRange(ni)).append("</td>");
        sb.append("<td>").append(rendererHelper.getFormatedStack(ni)).append("</td>");
        sb.append("<td>").append(rendererHelper.getFormatedContext(ni)).append("</td></tr>");
        if(ni.getChildCount()>0) {
            for(NeedleInfo child : ni.getChildren()) {
                writeNeedle(count, aggregationkey, child, sb);
            }
        }
        return sb.toString();
    }
    
    
    %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<link href="css/needles.css" rel="stylesheet" type="text/css" />
<link href="css/jquery.treetable.css" rel="stylesheet" type="text/css" />
<link href="css/jquery.treetable.theme.default.css" rel="stylesheet"
	type="text/css" />
<script type="text/javascript" src="webjars/jquery/1.9.1/jquery.min.js"></script>
<script type="text/javascript" src="js/jquery.treetable.js"></script>
<title>Needle Monitoring Center</title>
</head>
<body>
	<h1>Java Needles Configuration and Monitoring Center</h1>
	<!-- <div class="block"> -->
		
			<%
			    SimpleHTMLRenderer renderer = new SimpleHTMLRenderer();

			    for (AggregationFactory<? extends Aggregation<?>> fac : factories.values()) {
			        if (fac instanceof Top10AggregationFactory) {
			            
			            out.write("<h2>" + fac.getName() + " Needles</h2>");
			            out.write(" &rarr;<br/> ");
			            out.write("<table id=\"top10table\">");
			            out.write("<thead> <tr> <th>Name</th> <th>Duration</th><th>Date Range</th> <th>Stack Info</th><th>Execution Context</th></tr></thead>");
			            Top10AggregationFactory fact = (Top10AggregationFactory) fac;

			            for (Top10Aggregation aggr : fact.getRootAggregations()) {
			              /*   out.write(aggr.getNeedleName());
			                if (executionAggregationFactory != null) {
			                    ExecutionAggregation executionAggregation = executionAggregationFactory.getAggregation(aggr.getAggregationKey());
			                    out.write(" | MIN: " + executionAggregation.getMinimum() + " MAX: " + executionAggregation.getMaximum() + " AVG: "
			                            + executionAggregation.getAverage() + " Total: " + executionAggregation.getTotal() + " Count: "
			                            + executionAggregation.getMeasurements() + "<br/>");
			                } */
			                int i=0;
			                for (NeedleInfo ni : aggr.getTop10Needles()) {
			                    out.write(writeNeedle(i++, aggr.getAggregationKey(), ni, null));
			                }

			            }
			            out.write("</table>");
			        }
			    }
			%>
		
		<%
			    
			    for (AggregationFactory<? extends Aggregation<?>> fac : factories.values()) {
			        if (fac instanceof HotspotAggregationFactory) {
			            out.write("<h2>" + fac.getName() + " Needles</h2>");
			            out.write(" &rarr;<br/> ");
			            HotspotAggregationFactory hotspotAggregationFactory = (HotspotAggregationFactory) fac;
			            for (HotspotAggregation hotspot : hotspotAggregationFactory.getRootAggregations()) {
			                int i = 1;
			                for (NeedleInfo needle : hotspot.getHotspots()) {
			                    NeedleStub stub = needle.toNeedleStub();
			                    stub.setName(i++ + ". " + stub.getName());
			                    out.write(renderer.render(stub).toString());
			                }
			            }
			        }
			    }
			%>

	<!--  </div> -->
	<script type="text/javascript">
		$(document).ready(function() {
			$("#top10table").treetable({
			expandable: true,
			treeColumn: 0
			});
		});
	</script>
	<script type="text/javascript">
        $(document).ready(function() {
        	$.getJSON('needles-service/showJson', function(data) {
        		var items = [];
        		$.each(data, function(key, val) {
        			if(key == 'needles') {
        				items.push('<p>');
        				items.push('<table border="solid 1px;">');
        				var needleValues = $.parseJSON(val);
        			  
        			 $.each(needleValues, function(i, needle) {
        				items.push('<tr id="' + needle.id + ' data-tt-id="'+needle.id+'"><td>' 
        						+ needle.name + '</td>' +'<td>'+needle.date+'</td><td>'+needle.duration+'</td><td>'+needle.context+'</td>'); 
        			 });
                     items.push('</table>');
                     items.push('</p>');
        			} else {
        			 items.push('<h2 id="' + key + '">' + val + '</h2>');
        			 
        			 
        			}
        		
        		});
        		$('<ul/>', {
        		'class': 'my-new-list',
        		html: items.join('')
        		}).appendTo('body');
        		});
        });
    </script>
</body>
</html>