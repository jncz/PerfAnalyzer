"use strict"

define(["md/ajax","md/Tree","md/Event","md/StatsDialog"],function(ajax,Tree,event,statsDialog){
	var a = function(id){
		this.obj = $(id);
		this.setData = function(ds){
			var datas = ds.names;
			var tree = new Tree("container");
			tree.init();
			for(var i=0;i<datas.length;i++){
				var div = document.createElement("div");
				div.innerHTML = "<a href=\"#\">"+datas[i]+"</a>";
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
		};
		this.open = function(){
			event.attachArchiveBtnEvent();
			event.attachReArchiveBtnEvent();
			this.obj.classList.toggle("hide");
			this.obj.showModal();
		};
		this.close = function(){
			this.obj.close();
		};
	};
	
	return a;
});