package ch.usi.dag.disl.test.jraf2;

import ch.usi.dag.disl.dislclass.annotation.Before;
import ch.usi.dag.disl.dislclass.snippet.marker.BasicBlockMarker;
import ch.usi.dag.disl.dislclass.snippet.marker.BodyMarker;
import ch.usi.dag.disl.dislclass.snippet.marker.ExceptionHandlerMarker;
import ch.usi.dag.disl.staticinfo.analysis.BasicBlockAnalysis;
import ch.usi.dag.disl.dislclass.annotation.SyntheticLocal;
import ch.usi.dag.disl.dislclass.annotation.ThreadLocal;
import ch.usi.dag.disl.dislclass.annotation.SyntheticLocal.Initialize;

public class DiSLClass {
	
	@ThreadLocal 
	static int counter = 0;
	
	@SyntheticLocal(initialize=Initialize.ALWAYS)
	public static int THRESHOLD = 100;
	
	@Before(marker = BodyMarker.class, scope = "TargetClass.*(...)", order = 1)
	public static void before() {
		if(counter > THRESHOLD) {
			Profiler.profile(Thread.currentThread(), counter);
			counter = 0;
		}
	}
	
	@Before(marker = ExceptionHandlerMarker.class, scope = "TargetClass.*(...)", order = 1)
	public static void beforeHandler() {
	
		if(counter > THRESHOLD) {
			System.out.println(" HANDLER.....****** RESET COUNTER");
			Profiler.profile(Thread.currentThread(), counter);
			counter = 0;
		}
	}
	
	
	@Before(marker = BasicBlockMarker.class, scope = "TargetClass.*(...)", order = 0)
	public static void updateCounter(BasicBlockAnalysis bba) {
		if(bba.isFirstOfLoop() && counter > THRESHOLD) {
			System.out.println("PROFILE FIRST IN LOOP... ***** RESET COUNTER");
			Profiler.profile(Thread.currentThread(), counter);
			counter = 0;
		}
		
		counter += bba.getBBSize();
		System.out.println("basic block index: " + bba.getBBindex()
				+ " size: " + bba.getBBSize() + " counter " + counter);
	}
	
}