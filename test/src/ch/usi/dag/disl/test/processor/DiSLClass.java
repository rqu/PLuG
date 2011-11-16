package ch.usi.dag.disl.test.processor;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.annotation.SyntheticLocal;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.processor.Processor;
import ch.usi.dag.disl.processor.ProcessorMode;
import ch.usi.dag.disl.staticcontext.MethodSC;

public class DiSLClass {

	@SyntheticLocal
	public static String flag = "Start";

	@Before(marker = BodyMarker.class, order = 0, scope = "TargetClass.*")
	public static void insideMethod(MethodSC ci) {
		
		System.out.println("(In) Method " + ci.thisMethodName() + ": ");
		System.out.println(flag);
		
		Processor.apply(ProcessorTest.class, ProcessorMode.METHOD_ARGS);
		
		System.out.println(flag);
		System.out.println(ProcessorTest.flag);
	}
	
	@Before(marker = BytecodeMarker.class, args="invokevirtual", order = 0, scope = "TargetClass.*")
	public static void beforeInvocation(MethodSC ci) {
		
		System.out.println("(Before) Method : ");
		
		Processor.apply(ProcessorTest.class, ProcessorMode.CALLSITE_ARGS);
		
		System.out.println(ProcessorTest.flag);
	}
}
