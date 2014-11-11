/**
提供基础的函数库
*/
"use strict"
String.prototype.startWith=function(str){     
  var reg=new RegExp("^"+str);     
  return reg.test(this);        
} 
 
String.prototype.endWith=function(str){     
  var reg=new RegExp(str+"$");     
  return reg.test(this);        
}

Array.prototype.upush=function(ele){
	if(this.indexOf(ele)==-1){
		this.push(ele);
	}
}
Array.prototype.indexOfPoint=function(p){
	for(var i=0;i<this.length;i++){
		if(p[0] == this[i][0] && p[1] == this[i][1]){
			return i;
		}
	}
	return -1;
}
Array.prototype.existPoint=function(p){
	return this.indexOfPoint(p)!=-1;
}
function $(id){
	return document.getElementById(id);
}

function isKeyEvent(eventType){
	return eventType.startWith("key");
}

function extend(c,p){
	var c = c || {};
	
	for(var i in p){
		if(typeof p[i] === 'object'){
			c[i] = (p[i].constructor === Array)?[]:{};
			extend(c[i],p[i]);
		}else{
			if(c.hasOwnProperty(i)){
				continue;
			}
			c[i] = p[i];
		}
	}
	return c;
}

function extend2(c,p){
	var F = function(){};
	F.prototype = p.prototype;
	c.prototype = new F();
	c.prototype.constructor = c;
}