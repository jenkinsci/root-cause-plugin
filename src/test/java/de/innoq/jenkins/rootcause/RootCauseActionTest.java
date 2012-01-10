package de.innoq.jenkins.rootcause;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;

import org.jvnet.hudson.test.HudsonTestCase;

public class RootCauseActionTest extends HudsonTestCase {
	
	  public void testBuildWrapperAddsAction() throws Exception {
	        // Given
		  	FreeStyleProject project = createFreeStyleProject();
	        
	        project.getBuildWrappersList().add(new RootCauseBuildWrapper());

	        // When
	        FreeStyleBuild build = project.scheduleBuild2(0).get();
	        
	        // Then
	        RootCauseAction action = build.getAction(RootCauseAction.class);
	        assertNotNull(action);
	    }
}
