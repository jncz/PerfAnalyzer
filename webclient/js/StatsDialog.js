define(["md/ajax","md/TrendChart"],function(ajax,TrendChart){
	var dia = $("statsDialog");
	var getExeName = function(){
		return "com.spss.nextgen.rest.build.CancelBuild.CAExecute";
	};
	
	var paint = function(){
//		dia.style.width = "1000px";
//		dia.style.height = "800px";
		var exename = getExeName();
		var p = new Promise(function(resolve,reject){
			ajax.openURL("/catalyst/ca/jsoncalltree/stats/"+exename,function(http){
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
			for(var i=0;i<this.charts.length;i++){
				this.charts[i].destroy();
			}
		},
	};
	return a;
});