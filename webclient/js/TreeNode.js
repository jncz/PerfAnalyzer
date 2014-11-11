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
		
	};
	
	return a;
});