define(function(){
	var cw = 1800;
	var ch = 800;
	
	var startPoint = {x:45,y:ch-15};
	
	var maxX = cw - startPoint.x - 5;
	var maxY = startPoint.y;
	
	var ds = 10;//Y轴间隔长度
	var maxYSplit = 50;//Y轴最大间隔数目
	
	var paintXY = function(ctx,chartData){
		var x = chartData.axis_x;
		var y = chartData.axis_y;
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
	/**
	绘制X方向分割线
	*/
	var paintSplit = function(ctx,chartData){
		var sdy = 5;//分割线的高度
		var times = chartData.xsplit.length;
		for(var i=0;i<times;i++){
			ctx.beginPath();
			ctx.moveTo(startPoint.x+chartData.xsplit[i].pos,startPoint.y);
			ctx.lineTo(startPoint.x+chartData.xsplit[i].pos,startPoint.y-sdy);
			ctx.stroke();
		}
		
		times = chartData.ysplit.length;
		for(var i=0;i<times;i++){
			ctx.beginPath();
			ctx.moveTo(startPoint.x,startPoint.y-chartData.ysplit[i].pos);
			ctx.lineTo(startPoint.x+sdy,startPoint.y-chartData.ysplit[i].pos);
			ctx.stroke();
			
			ctx.strokeText(Math.ceil((chartData.peak.min + i*ds)),startPoint.x-30,startPoint.y-chartData.ysplit[i].pos);
		}
	};
	var paintPoint = function(ctx,statsData,data){
		var times = data.length;
		var t = 1;
		var lastPoint = null;
		var timestamp = document.body.getAttribute("ct");
		for(var i=(times>50?(times-50):0);i<times;i++){
			var cost = data[i].callMeanCost;
			var createTime = data[i].createdDate;
			var x = startPoint.x+t*statsData.xlen;
			var y = maxY - ((cost-statsData.peak.min)/ds)*statsData.ylen;
			var radius = 3;
			ctx.beginPath();
			ctx.save();
			if(timestamp == createTime){
				ctx.fillStyle = "#ff0000";
			}
			ctx.arc(x, y, radius, 0, 2*Math.PI, true);
			ctx.fill();
			ctx.restore();
			if(lastPoint != null){
				ctx.beginPath();
				ctx.moveTo(lastPoint[0],lastPoint[1]);
				ctx.lineTo(x,y);
				ctx.stroke();
			}
			lastPoint = [x,y];
			t++;
		}
	};
	var getPeek = function(statsData){
		var times = statsData.length;
		var maxTime = 0;
		var minTime = 10000000;
		for(var i=(times>50?(times-50):0);i<times;i++){
			var cost = statsData[i].callMeanCost;
			maxTime = Math.max(maxTime,cost);
			minTime = Math.min(minTime,cost);
		}
		
		return {max:maxTime,min:minTime};
	};
	var genChartData = function(statsData){
		ds = 10;//10ms每10ms一个间隔
		var peak = getPeek(statsData);
		var xsize = statsData.length+5;
		var ysize = (peak.max - peak.min)/ds+1;
		
		var dy = 0;
		if(ysize > maxYSplit){
			ysize = 50;
			ds = Math.ceil((peak.max - peak.min)/maxYSplit);//获取每间隔长度
			dy = maxY/maxYSplit;
		}else{
			dy = Math.ceil((peak.max - peak.min)/ysize)*((maxY-peak.min)/(peak.max-peak.min));
			ds = dy;
		}
		
				
		xsize = xsize > 55?55:xsize;//最大50个间隔
		
		var dx = maxX/xsize;//x方向单位间隔长度
		
		var d = {
			axis_x:maxX - startPoint.x,
			axis_y:maxY,
			xsplit:[],
			ysplit:[],
			xlen:dx,
			ylen:dy,
			peak:peak
		};
		
		for(var i=0;i<xsize;i++){
			d.xsplit.push({pos:dx*i});
		}
		for(var i=0;i<ysize;i++){
			d.ysplit.push({pos:dy*i});
		}
		
		return d;
	}
	var a = function(data){
		this.data = data;
		this.parent = null;
		this.init = function(parent){
			this.parent = parent;
			var c = document.createElement("canvas");
			c.setAttribute("width",cw);
			c.setAttribute("height",ch);
			this.parent.appendChild(c);
			var ctx = c.getContext("2d");
			
			var chartData = genChartData(this.data);
			paintXY(ctx,chartData);
			paintSplit(ctx,chartData);
			paintPoint(ctx,chartData,this.data);
		};
	};
	
	return a;
});