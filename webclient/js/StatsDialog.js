define(["md/ajax","md/TrendChart"],function(ajax,TrendChart){
	var dia = $("statsDialog");
	var getExeName = function(){
		return "com.spss.nextgen.rest.build.GetBuildStatus.CAExecute";
	};
	
	var paint = function(){
		//dia.style.width = "1000px";
		//dia.style.height = "800px";
		var exename = document.body.getAttribute("d");;
		var p = new Promise(function(resolve,reject){
			ajax.openURL("calltree/stats/"+exename,function(http){
				var jsonText = http.responseText;
				var obj = JSON.parse(jsonText);
				resolve(obj);
			},function(){
				reject(-1);
			},{"Accecpt":"application/json","Content-Type":"application/json"},"GET",null,true);
		});
		return p;
	};
	var a = {
		charts:[],
		opened:false,
		refresh:function(){
			var p = paint();
			this.opened = dia.open;
			var that = this;
			p.then(function(obj){
				var chart = new TrendChart(obj);
				that.clearChart();
				that.render(chart);
			});
		},
		open:function(){
			dia.showModal();
			var p = paint();
			this.opened = dia.open;
			var that = this;
			p.then(function(obj){
				var chart = new TrendChart(obj);
				that.clearChart();
				that.render(chart);
			});
		},
		close:function(){
			dia.close();
			this.opened = dia.open;
		},
		render:function(chart){
			chart.init(dia);
			this.charts.push(chart);
		},
		clearChart:function(){
			var cs = dia.getElementsByTagName("canvas");
			
			for(var i=0;i<cs.length;i++){
				var d = cs[i];
				d.parentNode.removeChild(d);
			}
		},
	};
	return a;
});