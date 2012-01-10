package de.innoq.jenkins.rootcause;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.model.Result;
import hudson.tasks.BuildTrigger;
import hudson.util.OneShotEvent;

import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.TestBuilder;

import de.innoq.jenkins.rootcause.RootCauseAction.RootCause;

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

	public void testSimpleLinearRootCauseIsFound() throws Exception {
		// Given projectA that triggers a Build of projectB

		final OneShotEvent buildStarted = new OneShotEvent();
		RunListener.LISTENERS.add(new RunListener<Run>() {

			@Override
			public void onCompleted(Run r, TaskListener listener) {
				buildStarted.signal();
			}

		});
		FreeStyleProject projectA = createFreeStyleProject();
		FreeStyleProject projectB = createFreeStyleProject();

		projectA.getPublishersList().add(
				new BuildTrigger(Collections.singleton(projectB),
						Result.SUCCESS));
		hudson.rebuildDependencyGraph();

		projectB.getBuildWrappersList().add(new RootCauseBuildWrapper());

		// When
		TestCause rootCause = new TestCause();
		FreeStyleBuild build = projectA.scheduleBuild2(0, rootCause).get();
		//
		System.out.println("Blocking on buildStarted");
		buildStarted.block();
		System.out.println("Waiting for Project B to finish its build.");
		while (projectB.getBuilds().isEmpty())
			;
		build = projectB.getBuilds().get(0);
		while (build.isBuilding())
			;
		System.out.println("Done Building");
		RootCauseAction rootCauseAction = build
				.getAction(RootCauseAction.class);
		List<RootCause> result = rootCauseAction.getCauses();
		assertEquals(1, result.size());
		assertEquals(rootCause, result.get(0).getCauseCount().keySet().iterator().next());
		System.out.println(result);
	}

	public static class TestCause extends Cause {

		@Override
		public String getShortDescription() {
			return "Cause for Unittest";
		}

	}
}
