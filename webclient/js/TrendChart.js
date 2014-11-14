define(function(){
	var cw = 1000;
	var ch = 800;
	var maxX = cw - 50;
	var maxY = ch - 50;
	var startPoint = {x:15,y:ch-15};
	
	var getXY = function(data){
		console.log(data);
		
		return {x:maxX,y:maxY};
	};
	var paintXY = function(ctx,x,y){
		var sx = startPoint.x;
		var sy = startPoint.y;
		
		ctx.beginPath();
		ctx.moveTo(sx,sy);
		ctx.lineTo(sx,sy-y);
		ctx.moveTo(sx,sy);
		ctx.lineTo(sx+x,sy);
		ctx.stroke();
		
		return [x,y];
	};
	var paintSplit = function(ctx,data,xyl){
		var sdy = 10;
		var times = data.length;
		var maxTime = 0;
		for(var i=0;i<times;i++){
			var cost = data[i].callMeanCost;
			maxTime = Math.max(maxTime,cost);
		}
		var xl = xyl[0];
		var yl = xyl[1];
		var dx = xl/(times+1);
		var dy = yl/maxTime;
		
		for(var i=1;i<=times;i++){
			ctx.beginPath();
			ctx.moveTo(startPoint.x+dx*i,startPoint.y);
			ctx.lineTo(startPoint.x+dx*i,startPoint.y-sdy);
			ctx.stroke();
		}
	};
	var paintPoint = function(ctx,data){
		
	};
	var a = function(data){
		this.data = data;
		this.canvas = null;
		this.parent = null;
		this.init = function(parent){
			this.parent = parent;
			var c = document.createElement("canvas");
			c.setAttribute("width",cw);
			c.setAttribute("height",ch);
			this.parent.appendChild(c);
			this.canvas = c;
			var ctx = c.getContext("2d");
			
			var xy = getXY(this.data);
			var xyl = paintXY(ctx,xy.x,xy.y);
			paintSplit(ctx,this.data,xyl);
			paintPoint(ctx,this.data);
		};
		this.destroy = function(){
			this.parent.removeChild(this.canvas);
		};
	};
	
	return a;
});