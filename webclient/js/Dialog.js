"use strict"

define(["md/ajax","md/Tree","md/Event","md/StatsDialog"],function(ajax,Tree,event,statsDialog){
	var getSummary = function(){
		ajax.openURL("calltree/summary",function(h){
						var t2 = h.responseText;
						var obj2 = JSON.parse(t2);
						var eles = document.getElementById("caseDialog").querySelectorAll("div[d^='com']");
						var len = eles.length;
						for(var i=0;i<len;i++){
							var costs = obj2[eles[i].getAttribute("d")];
							var spans = eles[i].querySelectorAll("span");
							spans[0].innerHTML = costs[0];
							spans[1].innerHTML = costs[1];
							spans[0].setAttribute("avg",costs[0]);
							spans[1].setAttribute("cost",costs[1]);
						}
						},function(){},{},"GET",null,true);
	}
	var a = function(id){
		this.obj = $(id);
		this.setData = function(ds){
			var datas = ds.names;
			var tree = new Tree("container");
			tree.init();
			for(var i=0;i<datas.length;i++){
				var div = document.createElement("div");
				div.innerHTML = "<a href=\"#\">"+datas[i]+"</a>----avg:<span></span>ms--latest:<span></span>ms";
				div.setAttribute("d",datas[i]);
				div.addEventListener("click",function(e){
					var n = this.getAttribute("d");
					
					ajax.openURL("calltree/"+n,function(h){
						var t2 = h.responseText;
						var obj2 = JSON.parse(t2);
						
						tree.data = obj2.data[0];
						tree.repaint();
						
						document.body.setAttribute("d",n);
						event.reset();
						event.tree = tree;
						event.attachFilterEvent();
						event.attachFilterBtnEvent();
						event.attachFilterClostBtnEvent();
						event.attachFilterRestBtnEvent();
						statsDialog.refresh();
						$(id).close();
					},function(){
						console.log("fail to load");
					},{},"GET",null,true);
				});
				this.obj.appendChild(div);
			}

			getSummary();
		};
		this.open = function(){
			event.attachArchiveBtnEvent();
			event.attachReArchiveBtnEvent();
			event.attachFilterListBtnEvent();
			this.obj.classList.toggle("hide");
			this.obj.showModal();
		};
		this.close = function(){
			this.obj.close();
		};
	};
	
	return a;
});