/**
 * Copyright (C) 2011 innoQ Deutschland GmbH
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
package de.innoq.jenkins.rootcause;

import hudson.model.RunAction;
import hudson.model.Run;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RootCauseAction implements RunAction {

	private static final Logger logger = Logger.getLogger(RootCauseAction.class.getName());
	
	public String getIconFileName() {
		// no icon
		return null;
	}

	public String getDisplayName() {
		return "Root Cause";
	}

	public String getUrlName() {
		return null;
	}

	public void onLoad() {
		// NOP		
	}

	public void onAttached(Run r) {
		logger.log(Level.FINEST, "Root Cause Logger was attached to {0}",r.getFullDisplayName());
	}

	public void onBuildComplete() {
		// NOP
	}


// Overall Todos:	

//	TODO Setup Unit/Integration Tests with Jenkins Test Harness
//  TODO Setup Proper Naming / Internationalization for existing Labels
//  TODO Implement actually finding out (and displaying) the root cause
//  TODO Create Github Readme file
//  TODO Setup Maven Metadata (License, Developer Connection and so on)

}
