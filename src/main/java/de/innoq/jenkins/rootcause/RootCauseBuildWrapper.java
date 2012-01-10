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

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * This BuildWrapper is used to attach a {@link RootCauseAction} to the Build.
 *
 */
public class RootCauseBuildWrapper extends BuildWrapper {


	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {
		// NoOp
		return new BuildWrapper.Environment() {
		};
	}

	@DataBoundConstructor
	public RootCauseBuildWrapper() {
		// Currently no speific configuration to initialize
	}
	
	@Override
	public void preCheckout(AbstractBuild build, Launcher launcher,
			BuildListener listener) throws IOException, InterruptedException {

		build.addAction(new RootCauseAction());
		System.out.println ("Pre Checkout was called on the RootCauseBuildWrapper!");
		super.preCheckout(build, launcher, listener);
	}
	
	/**
	   * Plugin marker for BuildWrapper.
	   */
	  @Extension
	  public static class DescriptorImpl extends BuildWrapperDescriptor
	  {
	    @Override
	    public String getDisplayName()
	    {
	    	// TODO Internationalization
	    	return "Root Cause Plugin";
	    
	      // return Messages.D..;
	    }

	    @Override
	    public boolean isApplicable(final AbstractProject<?, ?> item)
	    {
	      return true;
	    }
	  }
	  
	  
}
