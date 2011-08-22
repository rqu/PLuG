package ch.usi.dag.disl.processor.generator;

import java.util.HashMap;
import java.util.Map;

import ch.usi.dag.disl.dislclass.snippet.Snippet;
import ch.usi.dag.disl.dislclass.snippet.marker.MarkedRegion;

public class PIResolver {

	private Map<ResolverKey, ProcInstance> piStore = 
		new HashMap<ResolverKey, ProcInstance>();
	
	private class ResolverKey {
		
		private Snippet snippet;
		private MarkedRegion markedRegion;
		private int instrPos;
		
		public ResolverKey(Snippet snippet, MarkedRegion markedRegion,
				int instrPos) {
			super();
			this.snippet = snippet;
			this.markedRegion = markedRegion;
			this.instrPos = instrPos;
		}

		@Override
		public int hashCode() {
			
			final int prime = 31;
			int result = 1;
			
			result = prime * result + getOuterType().hashCode();
			
			result = prime * result + instrPos;
			
			result = prime * result
					+ ((markedRegion == null) ? 0 : markedRegion.hashCode());
			
			result = prime * result
					+ ((snippet == null) ? 0 : snippet.hashCode());
			
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			
			if (this == obj)
				return true;
			
			if (obj == null)
				return false;
			
			if (getClass() != obj.getClass())
				return false;
			
			ResolverKey other = (ResolverKey) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			
			if (instrPos != other.instrPos)
				return false;
			
			if (markedRegion == null) {
				if (other.markedRegion != null)
					return false;
			} else if (!markedRegion.equals(other.markedRegion))
				return false;
			
			if (snippet == null) {
				if (other.snippet != null)
					return false;
			} else if (!snippet.equals(other.snippet))
				return false;
			
			return true;
		}

		private PIResolver getOuterType() {
			return PIResolver.this;
		}
	}
	
	public ProcInstance get(Snippet snippet, MarkedRegion markedRegion,
			int instrPos) {

		ResolverKey key = new ResolverKey(snippet, markedRegion, instrPos);
		
		return piStore.get(key);
	}

	public void set(Snippet snippet, MarkedRegion markedRegion,
			int instrPos, ProcInstance processorInstance) {

		ResolverKey key = new ResolverKey(snippet, markedRegion, instrPos);
		
		piStore.put(key, processorInstance);
	}
}