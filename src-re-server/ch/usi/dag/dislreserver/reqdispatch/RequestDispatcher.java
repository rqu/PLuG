package ch.usi.dag.dislreserver.reqdispatch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.msg.analyze.AnalysisHandler;
import ch.usi.dag.dislreserver.msg.close.CloseHandler;

public class RequestDispatcher {

	private static final Map<Integer, RequestHandler> requestMap;
	
	static {
		requestMap = new HashMap<Integer, RequestHandler>();
		
		// Messages - should be in sync with c agent
		
		// close
		requestMap.put(0, new CloseHandler());
		// analyze
		requestMap.put(1, new AnalysisHandler());
	}
	
	public static boolean dispatch(int requestNo, DataInputStream is,
			DataOutputStream os, boolean debug) throws DiSLREServerException {

		// request handler
		RequestHandler rh = requestMap.get(requestNo);
		
		if(debug) {
			System.out.println("Dispatching " + rh.getClass().getName());
		}
		
		// process request
		rh.handle(is, os, debug);
		
		// if close handler is there then exit
		return rh instanceof CloseHandler;
	}

}
