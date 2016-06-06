/**
 *    Copyright 2015 Bernardo Ferreira

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package NovaSYS.IES_CBIR.data.BOVW;

import java.io.Serializable;

public class CodebookNode implements Serializable{

	private static final long serialVersionUID = -8247437320274751353L;
	
	private int nodeId;
	private int[] node;
	private CodebookNode[] childs;
	private int height;
	
	public CodebookNode(int nodeId, int[] node, int height, int nChilds) {
		setNodeId(nodeId);
		setNode(node);
		setNodeHeight(height);
		if (nChilds == -1)
			childs = null;
		else
			childs = new CodebookNode[nChilds];
	}
	
	public int[] getNode() {
		return node;
	}
	
	public void setNode(int[] node) {
		this.node = node;
	}
	
	public CodebookNode getChild(int index) {
		return childs[index];
	}
	
	public CodebookNode[] getChilds() {
		return childs;
	}
	
	public void setChilds(CodebookNode[] childs) {
		this.childs = childs;
	}
	
	public boolean isLeaf() {
		return childs == null;
	}
	
	public boolean isRoot() {
		return node == null;
	}
	
	public void setChilds(int[][] medians, int diff) {
		assert medians.length == childs.length;
		for (int i=0; i<medians.length; i++) {
			final int childId = (this.getNodeId()+1)*medians.length+i;
			if (childId < diff)
				childs[i] = new CodebookNode(childId,	medians[i], height+1, medians.length);
			else //is leaf
				childs[i] = new CodebookNode(childId-diff, medians[i], height+1, -1);
		}
	}

	public int getNodeHeight() {
		return height;
	}

	public void setNodeHeight(int height) {
		this.height = height;
	}
	
	public int codebookHeight() {
		if (this.isLeaf())
			return this.getNodeHeight();	
		else
			return childs[0].codebookHeight();
	}
	
	public int codebookSize() {
		assert !this.isLeaf();
		return (int)Math.pow(childs.length,codebookHeight());
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
}
