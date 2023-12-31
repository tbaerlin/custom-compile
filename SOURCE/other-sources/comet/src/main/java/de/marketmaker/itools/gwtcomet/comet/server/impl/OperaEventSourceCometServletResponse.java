/*
 * Copyright 2009 Richard Zschech.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package de.marketmaker.itools.gwtcomet.comet.server.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.itools.gwtcomet.comet.client.impl.OperaEventSourceCometTransport;
import de.marketmaker.itools.gwtcomet.comet.server.CometServlet;

import com.google.gwt.user.server.rpc.SerializationPolicy;

/**
 * The CometServletResponse for the {@link OperaEventSourceCometTransport}
 * 
 * @author Richard Zschech
 */
public class OperaEventSourceCometServletResponse extends RawDataCometServletResponse {
	
	public OperaEventSourceCometServletResponse(HttpServletRequest request, HttpServletResponse response, SerializationPolicy serializationPolicy, CometServlet servlet, AsyncServlet async, int heartbeat) {
		super(request, response, serializationPolicy, servlet, async, heartbeat);
	}
	
	@Override
	protected void setupHeaders(HttpServletResponse response) {
		super.setupHeaders(response);
		response.setContentType("application/x-dom-event-stream");
		// disable HTTP chunking
		response.setHeader("Connection", "Close");
	}

	@Override
	protected void appendMessageHeader() throws IOException {
		writer.append("Event: e\ndata: ");
	}
	
	@Override
	protected void appendMessageTrailer() throws IOException {
		writer.append("\n\n");
	}
}
