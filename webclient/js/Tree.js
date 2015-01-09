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

	var lastKeyNode;

	var nodes = [];
	var createCanvas = function(pid){
		var c = document.createElement("canvas");
		var p = document.getElementById(pid);
		p.appendChild(c);
		
		c.setAttribute("width",cw);
		c.setAttribute("height",ch);
		
		return c;
	};

	var getCanvas = function(pid){
		return document.getElementById(pid).getElementsByTagName("canvas")[0];
	}
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
			if(n.hideChild){
				ctx.save();
				ctx.strokeStyle = "#0000FF";
			}
			if(n.keyNode){
				ctx.save();
				ctx.strokeStyle = "#FF0000";
			}
			createRect(n.x,n.y,rw,rh);
			if(n.hideChild){
				ctx.restore();
			}
			if(n.keyNode){
				ctx.restore();
			}
		}
	};

	var applyLineStyle = function(n,p){
		if(n.keyNode && p.keyNode){
			ctx.save();
			ctx.strokeStyle="#FF0000";
		}
	};

	var releaseLineStyle = function(n,p){
		ctx.restore();
	};
	var createLine = function(n,p){
		if(p == null){
			return;
		}
		var px = p.x+rw/2;
		var py = p.y+rh;
		
		var x = n.x+rw/2;
		var y = n.y;
		
		applyLineStyle(n,p);
		ctx.beginPath();
		ctx.moveTo(px,py);
		//ctx.lineTo(px,py+minYgap/3);
		//ctx.lineTo(x,py+minYgap/2);
		ctx.lineTo(x,y);
		ctx.stroke();
		releaseLineStyle(n,p);
	}
	var createLins = function(ns){
		for(var i=0;i<ns.length;i++){
			var n = ns[i];
			createLine(n,n.parent);
		}
	};
	var fillText = function(ns){
		var offset = 2;
		for(var i=0;i<ns.length;i++){
			var n = ns[i];
			ctx.fillText(n.key,n.x+offset,n.y);
			ctx.fillText(n.callMeanCost+"/"+n.callTimes,n.x+offset,(n.y+10));
			ctx.fillText(n.callMeanCost*n.callTimes,n.x+offset,(n.y+20));
		}
	};

	var getTotalCost = function(n){
		var total = (n.callMeanCost*1)*(n.callTimes*1);
		return total;
	};

	/**
	* update the entire tree to find out the key path, the key path mean the path that take the most of the time cost;
	**/
	var updateKeyPath = function(ns){
		if(ns.length == 1 && ns[0].currentIdx == 0){//root node
			ns[0].keyNode = true;
			lastKeyNode = ns[0];
			return;
		}
		var parent = lastKeyNode;
		var keyNodeIdx = -1;
		var maxTotalCost = 0;
		if(parent.keyNode){
			for(var i=0;i<ns.length;i++){
				var n = ns[i];
				if(n.parent != lastKeyNode && lastKeyNode){
					continue;
				}
				var maxOne = Math.max(maxTotalCost,getTotalCost(n));
				if(maxOne > maxTotalCost){
					maxTotalCost = maxOne;
					keyNodeIdx = i;
				}
			}
		}
		
		if(keyNodeIdx != -1){
			ns[keyNodeIdx].keyNode = true;
			lastKeyNode = ns[keyNodeIdx];
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
		updateKeyPath(n);
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
		ctx.save();
		ctx.font="15px Times New Roman";
		ctx.fillText(new Date(createTime),10,10);
		ctx.restore();
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
			if(n && n.parent == root){
				n.hide = true;
				hideTree(n);
			}
		}
	};

	var toggleTree = function(root,hide){
		for(var i = 0;i<nodes.length;i++){
			var n = nodes[i];
			if(n && n.parent == root){
				n.hide = hide;
				n.collapse = hide;
				toggleTree(n,hide);
			}
		}
	};
	
	var processNodes = function(e,callback,callback2){
		for(var i=0;i<nodes.length;i++){
			var n = nodes[i];
			if(!n || n.hide){
				continue;
			}
			if(e.offsetX > n.x && e.offsetX < n.x+rw && e.offsetY > n.y && e.offsetY < n.y+rh){
				var result = callback(n);
				if(result){
					return;
				}
				ctx.save();
				ctx.clearRect(0,0,cw,ch);
				paintCallTime();
				for(var x=0;x<=indicator.i;x++){
					paintNodes(x);
				}
				ctx.restore();
				break;
			}else{
				if(callback2){
					callback2();
				}
			}
		}
	};

	var currentNode = null;//to record the current onmousemove selected node

	var mousemoveRunning = false;//if the mousemove event is still running

	var deleteHideNodes = function(){
		for(var i=0;i<nodes.length;i++){
			var n = nodes[i];
			if(n && n.hide && !n.collapse){
				nodes[i] = null;
			}
		}
	};
	var a = function(id){
		this.id = id;
		this.data;
		this.dblclickEvent = function(e){
				console.log("db click");
				//collision check
				if(!e.ctrlKey && !e.shiftKey){
					processNodes(e,function(n){
						n.hide = true;
						hideTree(n);
						deleteHideNodes();
					});
				}
			};

		this.clickEvent = function(e){
				console.log("click");
				if(e.ctrlKey){
					console.log("ctrl click");
					//only this node and its children
					processNodes(e,function(n){
						if(n && n.currentIdx){
							for(var y=0;y<nodes.length;y++){
								var n2 = nodes[y];
								if(n2 && n2.currentIdx === n.currentIdx && n2 !== n && n2.parent == n.parent){
									n2.hide = true;
									hideTree(n2);
								}
							}
							deleteHideNodes();
						}
					});
				}else if(e.shiftKey){
					console.log("shift key");
					processNodes(e,function(n){
						if(n.hideChild){
							toggleTree(n,false);
							n.hideChild = false;
						}else{
							n.hideChild = true;
							toggleTree(n,true);
						}
						
					});
				}
			};

		
		this.mousemoveEvent = function(e){
				if(mousemoveRunning){
					setTimeout(200,"console.log('sleep 200ms')");
					return;
				}
				mousemoveRunning = true;
				
				var dialog = document.getElementById("nodeInfoDialog");
				processNodes(e,function(n){
					console.log(n.key);
					if(!currentNode && currentNode == n && dialog.open){
						mousemoveRunning = false;
						return true;
					}
					currentNode = n;
					var textContainers = dialog.getElementsByTagName("div");

					textContainers[0].innerText = n.key;
					textContainers[1].innerText = n.callMeanCost+"/"+n.callTimes;
					textContainers[2].innerText = n.callMeanCost*n.callTimes;
			
					dialog.style.top=n.y+rh/1.2+"px";
                    dialog.style.left=n.x+rw/1.2+"px";
					dialog.show();

					mousemoveRunning = false;
					return true;
				},function(){
					if(dialog.open){
						dialog.close();
					}
					mousemoveRunning = false;
				});
			};
		this.regEvent = function(c){
			//db click event
			c.removeEventListener("dblclick",this.dblclickEvent);
			c.addEventListener("dblclick",this.dblclickEvent);
			
			//CTRL + left click event
			c.removeEventListener("click",this.clickEvent);
			c.addEventListener("click",this.clickEvent);

			//mousemove event
			c.removeEventListener("mousemove",this.mousemoveEvent);
			c.addEventListener("mousemove",this.mousemoveEvent);
		};
		this.init = function(){
			var c = createCanvas(this.id);
			ctx = c.getContext("2d");
			
			this.regEvent(c);
		}

		this.repaint = function(){
			var clonedData = JSON.parse(JSON.stringify(this.data));
			ctx.save();
			ctx.clearRect(0,0,cw,ch);
			paint(ctx,clonedData);
			var c = getCanvas(this.id);
			this.regEvent(c);
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