"use strict"

define(function(){
	var a = function(cn,mn,curIdx,p){
		this.className = cn;
		this.methodName = mn;
		this.currentIdx = curIdx;
		this.key = this.className+"."+this.methodName;
		this.parent = p;
		this.callMeanCost;
		this.callTimes;
		this.keyNode;//indicate whether this node is a key node, that means the node take the most of the time of its sibling nodes
		
	};
	
	return a;
});