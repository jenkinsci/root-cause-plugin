package jenkins.plugins.rootcause;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;

import java.util.Collections;
import java.util.List;

import jenkins.plugins.rootcause.RootCauseAction;
import jenkins.plugins.rootcause.RootCauseBuildWrapper;
import jenkins.plugins.rootcause.RootCauseAction.RootCause;

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

	public void testSimpleLinearRootCauseIsFound() throws Exception {
		// Given projectA that triggers a Build of projectB

	
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

	public void testDiamondDependencies() throws Exception {
		
			hudson.setNumExecutors(1);
			// Given a diamond: A->B->D , A->C->D
		
			FreeStyleProject projectA = createFreeStyleProject("A");
			FreeStyleProject projectB = createFreeStyleProject("B");
			FreeStyleProject projectC = createFreeStyleProject("C");
			FreeStyleProject projectD = createFreeStyleProject("D");

			projectA.getPublishersList().add(
					new BuildTrigger(Collections.singleton(projectB),
							Result.SUCCESS));
			
			projectA.getPublishersList().add(
					new BuildTrigger(Collections.singleton(projectC),
							Result.SUCCESS));
						
			projectC.getPublishersList().add(
					new BuildTrigger(Collections.singleton(projectD),
							Result.SUCCESS));
						
			projectB.getPublishersList().add(
					new BuildTrigger(Collections.singleton(projectD),
							Result.SUCCESS));			
			hudson.rebuildDependencyGraph();

			projectD.getBuildWrappersList().add(new RootCauseBuildWrapper());

			// When
			TestCause rootCause = new TestCause();
			FreeStyleBuild build = projectA.scheduleBuild2(0, rootCause).get();

			
			System.out.println("Waiting for Project D to finish its build.");
			while (projectD.getBuilds().isEmpty())
				;
			build = projectD.getBuilds().get(0);
			while (build.isBuilding())
				;
			System.out.println("Done Building");
			RootCauseAction rootCauseAction = build
					.getAction(RootCauseAction.class);
			
			//THEN 
			
			List<RootCause> result = rootCauseAction.getCauses();
			// There should be only on root cause, we just triggered A
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
