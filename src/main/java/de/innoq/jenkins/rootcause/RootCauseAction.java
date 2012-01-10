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
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RootCauseAction implements RunAction {

	private static final Logger logger = Logger.getLogger(RootCauseAction.class.getName());
	private List<Cause> causes = null;
	private Run run;
	
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
		this.run = r;
		logger.log(Level.FINEST, "Root Cause Logger was attached to {0}",r.getFullDisplayName());
	}

	public void onBuildComplete() {
		// This is never called , JENKINS-12359
	}

	
	public synchronized List<Cause> getCauses() {
		if (causes == null) {
			causes = new ArrayList<Cause>();
			CauseAction causeAction = run.getAction(CauseAction.class);
			
			Stack<Cause> causesToVisit=  new Stack<Cause> ();
			causesToVisit.addAll(causeAction.getCauses());
			Set<Cause> marked = new HashSet<Cause>();
			marked.addAll(causesToVisit);
			while (!causesToVisit.isEmpty()) {
				
				Cause cause = causesToVisit.pop();
				if (cause instanceof UpstreamCause) {
					
					UpstreamCause uc = (UpstreamCause) cause;
					AbstractProject p = (AbstractProject) Hudson.getInstance().getItem (uc.getUpstreamProject());
					Run upstreamRun = p.getBuildByNumber(uc.getUpstreamBuild());
					
					CauseAction upstreamCauseAction = upstreamRun.getAction(CauseAction.class);
					for (Cause upstreamCause : upstreamCauseAction.getCauses()) {
						if (!marked.contains(upstreamCause)) {
							marked.add(upstreamCause);
							causesToVisit.add(upstreamCause);
						}
					}
				} else {
					causes.add(cause);
				}
			}
		}
		return causes;
	}

// Overall Todos:	

//  TODO Implement actually finding out (and displaying) the root cause
// 
//	TODO Check for Concurrency Problems / synchronize RootCauseACtion 	
// TODO Check Persistent behaviour / xml Files
//  TODO Setup Proper Naming / Internationalization for existing Labels	
//  TODO Create Github Readme file
//  TODO Setup Maven Metadata (License, Developer Connection and so on)

}
