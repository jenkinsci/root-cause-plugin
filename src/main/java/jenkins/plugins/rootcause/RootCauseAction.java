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
package jenkins.plugins.rootcause;

import hudson.model.RunAction;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Cause.UpstreamCause;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.model.Run;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Action is attached to a single Run/Build. It contributes a summary.jelly
 * which displays the root causes on the build overview page.
 * 
 */
public class RootCauseAction implements RunAction {

	private static final Logger logger = Logger.getLogger(RootCauseAction.class
			.getName());
	private Collection<RootCause> causes = null;
	private Run run;

	public String getIconFileName() {
		// no icon
		return null;
	}

	public String getDisplayName() {
		return "This should never be displayed";
	}

	public String getUrlName() {
		return null;
	}

	public void onLoad() {
		// NOP
	}

	public void onAttached(Run r) {
		this.run = r;
		logger.log(Level.FINEST, "Root Cause Logger was attached to {0}",
				r.getFullDisplayName());
	}

	public void onBuildComplete() {
		// This is never called , JENKINS-12359
	}

	/**
	 * Each Build can have multiple Causes. Each cause can be seen multiple
	 * times.
	 * 
	 * Multiples are only relevant for leaves.
	 * 
	 * @return
	 */
	public synchronized Collection<RootCause> getCauses() {
		if (causes == null) {
			HashMap<RootCause, RootCause> rootCauses = new HashMap<RootCause, RootCause>();
			collectRootCauses(rootCauses, run);
			causes = rootCauses.values();
		}
		return causes;
	}

	private void collectRootCauses(Map<RootCause, RootCause> rootCauses, Run run) {
		CauseAction causeAction = run.getAction(CauseAction.class);
		Map<Cause, Integer> currentCauses = causeAction.getCauseCounts();
		RootCause rootCause = null;
		boolean isLeave = false;
		for (Entry<Cause, Integer> currentCauseCount : currentCauses.entrySet()) {
			if (currentCauseCount.getKey() instanceof UpstreamCause) {
				UpstreamCause uc = (UpstreamCause) currentCauseCount.getKey();
				// if (!projectsHandled.contains(uc.getUpstreamProject())) {
				AbstractProject p = (AbstractProject) Hudson.getInstance()
						.getItem(uc.getUpstreamProject());
				Run upstreamRun = p.getBuildByNumber(uc.getUpstreamBuild());
				collectRootCauses(rootCauses, upstreamRun);
				// }

			} else {
				if (rootCause == null) {
					rootCause = new RootCause();
					rootCause.setProject(run.getParent().getName());
					rootCause.setProjectUrl(run.getParent().getUrl());
					rootCause.setBuild(run.getNumber());
					if (rootCauses.containsKey(rootCause)) {
						// There is already such a root cause, we need to merge
						rootCause = rootCauses.get(rootCause);
					} else {
						rootCauses.put(rootCause, rootCause);
					}
				}
				rootCause.getCauseCount().put(currentCauseCount.getKey(),
						currentCauseCount.getValue());
			}
		}
	}

	public static class RootCause {

		public String getProject() {
			return project;
		}

		public void setProject(String project) {
			this.project = project;
		}

		public int getBuild() {
			return build;
		}

		public void setBuild(int i) {
			this.build = i;
		}

		public Map<Cause, Integer> getCauseCount() {
			return causeCount;
		}

		public void setCauseCount(Map<Cause, Integer> causeCount) {
			this.causeCount = causeCount;
		}

		private String project;
		private String projectUrl;
		private int build;
		private Map<Cause, Integer> causeCount = new LinkedHashMap<Cause, Integer>();

		public String getProjectUrl() {
			return projectUrl;
		}

		public void setProjectUrl(String projectUrl) {
			this.projectUrl = projectUrl;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + build;
			result = prime * result
					+ ((project == null) ? 0 : project.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			RootCause other = (RootCause) obj;
			if (build != other.build) {
				return false;
			}
			if (project == null) {
				if (other.project != null) {
					return false;
				}
			} else if (!project.equals(other.project)) {
				return false;
			}
			return true;
		}

	}

	// Overall Todos:.
	// TODO Test this plugin in different settings, e.g. Maven instead of
	// Freestyle Projects. Add Testcases
	// TODO Release Plugin, announce
}
