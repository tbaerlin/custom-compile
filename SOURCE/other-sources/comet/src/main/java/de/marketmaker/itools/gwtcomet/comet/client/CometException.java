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
package de.marketmaker.itools.gwtcomet.comet.client;

public class CometException extends Exception {
	
	private static final long serialVersionUID = 1274546262360559017L;
	
	public CometException() {
		super();
	}
	
	public CometException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CometException(String message) {
		super(message);
	}
	
	public CometException(Throwable cause) {
		super(cause);
	}
}
