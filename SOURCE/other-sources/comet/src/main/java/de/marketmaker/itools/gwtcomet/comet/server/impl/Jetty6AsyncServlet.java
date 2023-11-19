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

/*
########################################################## Commented out by Ulrich: gwt-dev-2.6.0 does no longer include org.mortbay.jetty ######################################

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.SessionManager;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.Context.SContext;
*/

/**
 * An extension of {@link BlockingAsyncServlet} for Jetty.
 * 
 * This extension improves on the default session keep alive strategy, refreshing the connection just before the session
 * expires, by updating the session managers last access time when ever sending data down the Comet connection
 * 
 * @author Richard Zschech
 */
public class Jetty6AsyncServlet extends BlockingAsyncServlet {
	
/*
########################################################## Commented out by Ulrich: gwt-dev-2.6.0 does no longer include org.mortbay.jetty ######################################
	private SessionManager sessionManager;
	
	@Override
	public void init(ServletContext context) throws ServletException {
		super.init(context);
		sessionManager = ((Context) ((SContext) context).getContextHandler()).getSessionHandler().getSessionManager();
	}
	
	@Override
	protected boolean access(HttpSession httpSession) {
		sessionManager.access(httpSession, false);
		return true;
	}
*/
}
