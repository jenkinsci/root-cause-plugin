package jenkins.plugins.rootcause;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import jenkins.plugins.rootcause.RootCauseAction;
import jenkins.plugins.rootcause.RootCauseBuildWrapper;
import jenkins.plugins.rootcause.RootCauseAction.RootCause;

import org.jvnet.hudson.test.HudsonTestCase;


public class RootCauseActionTest extends HudsonTestCase {

	private FreeStyleProject projectA;
	private FreeStyleProject projectB;
	
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
		// Given
		setupProjectAAndB();		
		
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
		Collection<RootCause> result = rootCauseAction.getCauses();
		assertEquals(1, result.size());
		assertEquals(rootCause, result.iterator().next().getCauseCount().keySet().iterator().next());
		System.out.println(result);
	}
	
	
	
	public void testMultipleTimeSameCause() throws IOException, InterruptedException, ExecutionException {
		// When
		setupProjectAAndB();
		// when
		// Trigger a build twice
		TestCause rootCause = new TestCause();
		Future<FreeStyleBuild> future  =projectA.scheduleBuild2(1, rootCause);

		projectA.scheduleBuild2(0, new TestCause());
		FreeStyleBuild build = future.get();
		
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
		Collection<RootCause> result = rootCauseAction.getCauses();
		assertEquals (1, projectB.getBuilds().size());
		assertEquals(1, result.size());
		assertEquals(rootCause, result.iterator().next().getCauseCount().keySet().iterator().next());
		assertEquals(new Integer (2), result.iterator().next().getCauseCount().entrySet().iterator().next().getValue());
		System.out.println(result);	
	}
	
	
	
	public void testShowCausesFromMultipleBuildsOfSameProject() throws IOException, InterruptedException, ExecutionException {
		// When
		hudson.setNumExecutors(1);
		setupProjectAAndB();
		projectB.setQuietPeriod(2);
		// Trigger two build for A
		TestCause rootCause = new TestCause();
		Future<FreeStyleBuild> future = projectA.scheduleBuild2(0, rootCause);
		future.get(); // Make Sure A has finsihed, B will now be in its quiet Period.

		// Trigger another Build of A
		future  =projectA.scheduleBuild2(0, new TestCause());
		FreeStyleBuild build = future.get();
		
		// Now B has two causes, Build 1 and 2 of A.
		
		System.out.println("Waiting for Project B to finish its build.");
		while (projectB.getBuilds().isEmpty());
		
		build = projectB.getBuilds().get(0);
		while (build.isBuilding());
		System.out.println("Done Building B");
		RootCauseAction rootCauseAction = build
				.getAction(RootCauseAction.class);
		Collection<RootCause> result = rootCauseAction.getCauses();
		assertEquals (1, projectB.getBuilds().size());
		assertEquals(2, result.size()); // There should be two Root Causes, one for each Build of A
	}

	
  /**
   * Setup a Project A and B where A triggeres B and B has the Root Cause Plugin activated.
   * @throws IOException
   */
	private void setupProjectAAndB() throws IOException {
		// Given projectA that triggers a Build of project
		projectA = createFreeStyleProject();
		projectB = createFreeStyleProject();

		projectA.getPublishersList().add(
				new BuildTrigger(Collections.singleton(projectB),
						Result.SUCCESS));
		hudson.rebuildDependencyGraph();

		projectB.getBuildWrappersList().add(new RootCauseBuildWrapper());
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
			
			Collection<RootCause> result = rootCauseAction.getCauses();
			// There should be only on root cause, we just triggered A
			assertEquals(1, result.size());
			assertEquals(rootCause, result.iterator().next().getCauseCount().keySet().iterator().next());
			System.out.println(result);
		
		
	}
	public static class TestCause extends Cause {

		@Override
		public String getShortDescription() {
			return "Cause for Unittest";
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof TestCause);
		}

		@Override
		public int hashCode() {
			return 4711;
		}
		
		

	}
}
