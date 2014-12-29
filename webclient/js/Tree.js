"use strict"

define(["md/TreeNode"],function(TreeNode){
	var cw = 2500;
	var ch = 2500;//canvas height
	
	var rw = 40;//rectangle width
	var rh = 40;//rectangle height
	
	var minXgap = 20;
	var minYgap = 20;
	
	var ctx;
	var ftimes;
	var fcost;
	var fcondition;
	
	var createTime;
	
	var nodes = [];
	var createCanvas = function(pid){
		var c = document.createElement("canvas");
		var p = document.getElementById(pid);
		p.appendChild(c);
		
		c.setAttribute("width",cw);
		c.setAttribute("height",ch);
		
		return c;
	};
	var createRect = function(x,y,w,h){
		ctx.strokeRect(x,y,w,h);
	}
	var createNode = function(className,methodName,currentIdx,parent){
		//TODO
		var node = new TreeNode(className,methodName,currentIdx,parent);
		return node;
	};
	var indicator = {
		i : 0,
		incr : function(){
			this.i++;
			//console.log(this.i);
		},
		reset:function(){
			this.i = 0;
		},
	};
	
	var p = function(id,ds){
		if(id.i == 0 && ds.length == 1){
			var d = ds[0];
			var node = createNode(d["class"],d["method"],id.i,null);
			node.callMeanCost = d.callMeanCost;
			node.callTimes = d.callTimes;
			nodes.push(node);
			if(d.nextcall){
				id.incr();
				for(var x=0;x<d.nextcall.length;x++){
					d.nextcall[x].parent = node;
				}
				p(id,d.nextcall);
			}
		}else{
			var dss = [];
			for(var i=0;i<ds.length;i++){
				var d = ds[i];
				if(!d){
					continue;
				}
				var node = createNode(d["class"],d["method"],id.i,d.parent);
				node.callMeanCost = d.callMeanCost;
				node.callTimes = d.callTimes;
				nodes.push(node);
				if(d.nextcall){
					for(var x=0;x<d.nextcall.length;x++){
						if(!d.nextcall[x]){
							continue
						}
						d.nextcall[x].parent = node;
					}
					dss = dss.concat(d.nextcall);
				}
			}
			id.incr();
			if(dss.length === 0){
				return;
			}
			p(id,dss);
		}
	}

	var calculateSize = function(ns){
		var x = cw/(ns.length+1);
		var y = ns[0].currentIdx*(rh+minYgap);
		
		for(var i=0;i<ns.length;i++){
			var n = ns[i];
			n.x = x*(i+1);
			n.y = y+10;
		}
	};
	var createRectangles = function(ns){
		for(var i=0;i<ns.length;i++){
			var n = ns[i];
			createRect(n.x,n.y,rw,rh);
		}
	};
	var createLine = function(n,p){
		if(p == null){
			return;
		}
		var px = p.x+rw/2;
		var py = p.y+rh;
		
		var x = n.x+rw/2;
		var y = n.y;
		
		ctx.beginPath();
		ctx.moveTo(px,py);
		ctx.lineTo(x,y);
		ctx.stroke();
	}
	var createLins = function(ns){
		for(var i=0;i<ns.length;i++){
			var n = ns[i];
			createLine(n,n.parent);
		}
	};
	var fillText = function(ns){
		for(var i=0;i<ns.length;i++){
			var n = ns[i];
			ctx.fillText(n.key,n.x,n.y);
			ctx.fillText(n.callMeanCost+"/"+n.callTimes,n.x,(n.y+10));
			ctx.fillText(n.callMeanCost*n.callTimes,n.x,(n.y+20));
		}
	};
	var paintNodes = function(idx){
		var n = [];
		for(var i=0;i<nodes.length;i++){
			var node = nodes[i];
			if(node && node.currentIdx == idx && !node.hide){
				n.push(node);
			}
		}
		if(n.length > 0){
			calculateSize(n);
			//createRectangles(n);
			createLins(n);
			createRectangles(n);
			fillText(n);
		}
	};
	var resetParent = function(){
		for(var i=1;i<nodes.length;i++){
			var n = nodes[i];
			if(n && fcost && ftimes){
				console.log(fcondition);
				if(fcondition){
					if(n.callMeanCost*1 < fcost || n.callTimes*1 < ftimes){
						for(var x=1;x<nodes.length;x++){
							var n2 = nodes[x];
							if(!n2){
								continue;
							}
							if(n2.parent == n){
								n2.parent = n.parent;
							}
						}
						nodes[i] = null;
					}
				}else{
					if(n.callMeanCost*1 < fcost && n.callTimes*1 < ftimes){
						for(var x=1;x<nodes.length;x++){
							var n2 = nodes[x];
							if(!n2){
								continue;
							}
							if(n2.parent == n){
								n2.parent = n.parent;
							}
						}
						nodes[i] = null;
					}
				}
				
			}
		}
	};
	var paintCallTime = function(){
		ctx.fillText(new Date(createTime),10,10);
	};
	var paint = function(ctx,data){
		createTime = data[0].createdTime;
		document.body.setAttribute("ct",createTime);
		paintCallTime();
		nodes = [];
		indicator.reset();
		p(indicator,data,null);
		
		resetParent();
		for(var i=0;i<=indicator.i;i++){
			paintNodes(i);
		}
		//console.log(nodes);
	}
	
	var hideTree = function(root){
		for(var i = 0;i<nodes.length;i++){
			var n = nodes[i];
			if(n.parent == root){
				n.hide = true;
				hideTree(n);
			}
		}
	};
	var a = function(id){
		this.id = id;
		this.data;
		this.init = function(){
			var c = createCanvas(this.id);
			ctx = c.getContext("2d");
			c.addEventListener("dblclick",function(e){
				console.log("db click");
				//collision check
				for(var i=0;i<nodes.length;i++){
					var n = nodes[i];
					if(n.hide){
						continue;
					}
					if(e.offsetX > n.x && e.offsetX < n.x+rw && e.offsetY > n.y && e.offsetY < n.y+rh){
						console.log(e.offsetX+" - "+e.offsetY+" - "+n.x+" - "+n.y+" - "+(e.offsetX > n.x && e.offsetX < n.x+rw && e.offsetY > n.y && e.offsetY < n.y+rh));
						n.hide = true;
						hideTree(n);
						ctx.save();
						ctx.clearRect(0,0,cw,ch);
						paintCallTime();
						for(var x=0;x<=indicator.i;x++){
							paintNodes(x);
						}
						ctx.restore();
						break;
					}
				}
				
			});
			c.addEventListener("click",function(e){
				if(e.ctrlKey){
					//only this node and its children
					for(var i=0;i<nodes.length;i++){
						var n = nodes[i];
						if(n.hide){
							continue;
						}
						if(e.offsetX > n.x && e.offsetX < n.x+rw && e.offsetY > n.y && e.offsetY < n.y+rh){
							if(n.currentIdx){
								for(var y=0;y<nodes.length;y++){
									var n2 = nodes[y];
									if(n2.currentIdx === n.currentIdx && n2 !== n && n2.parent == n.parent){
										n2.hide = true;
										hideTree(n2);
									}
								}
							}
							ctx.save();
							ctx.clearRect(0,0,cw,ch);
							paintCallTime();
							for(var x=0;x<=indicator.i;x++){
								paintNodes(x);
							}
							ctx.restore();
							break;
						}
					}
				}
			});
		}
		
		this.repaint = function(){
			var clonedData = JSON.parse(JSON.stringify(this.data));
			ctx.save();
			ctx.clearRect(0,0,cw,ch);
			paint(ctx,clonedData);
			ctx.restore();
		};
		this.filter = function(cost,times,condition){
			fcost = cost;
			ftimes = times;
			fcondition = condition;
		};
	};
	
	return a;
});