package ch.usi.dag.disl.staticinfo.analysis.cache;

import ch.usi.dag.disl.staticinfo.analysis.StaticAnalysisData;

public class SnippetCache extends StaticAnalysisData {

	public SnippetCache(StaticAnalysisData sad) {
		super(sad);
	}

	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		
		result = prime * result
				+ ((classNode == null) ? 0 : classNode.hashCode());

		result = prime * result
				+ ((methodNode == null) ? 0 : methodNode.hashCode());

		result = prime * result + ((snippet == null) ? 0 : snippet.hashCode());

		// ignored for this cache type
		//result = prime * result + ((marking == null) ? 0 : marking.hashCode());
		
		// ignored for this cache type
		//result = prime * result	+ ((markedRegion == null) ? 0 : markedRegion.hashCode());
		
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
		
		SnippetCache other = (SnippetCache) obj;

		if (classNode == null) {
			if (other.classNode != null)
				return false;
		} else if (!classNode.equals(other.classNode))
			return false;

		if (methodNode == null) {
			if (other.methodNode != null)
				return false;
		} else if (!methodNode.equals(other.methodNode))
			return false;
		
		if (snippet == null) {
			if (other.snippet != null)
				return false;
		} else if (!snippet.equals(other.snippet))
			return false;

		// ignored for this cache type
		//if (marking == null) {
		//	if (other.marking != null)
		//		return false;
		//} else if (!marking.equals(other.marking))
		//	return false;
		
		// ignored for this cache type
		//if (markedRegion == null) {
		//	if (other.markedRegion != null)
		//		return false;
		//} else if (!markedRegion.equals(other.markedRegion))
		//	return false;
		
		return true;
	}
}