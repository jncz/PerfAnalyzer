"use strict"

define(function(){
	var keycode_f = 70;
	var keycode_a = 65;
	var dialogOpen = function(id){
		var ele = document.getElementById(id);
		ele.classList.toggle("hide");
		a.detachFilterEvent();
		
		ele.showModal();
	};
	var a = {
		tree : null,
		reset : function(){
			document.removeEventListener("keydown",this.filterEventListener);
		},
		filterEventListener : function(e){
				console.log(e.keyCode);
				if(e.keyCode == keycode_f){
					dialogOpen("filterDialog");
				}
				if(e.keyCode == keycode_a){
					document.getElementById("caseDialog").showModal();
				}
			},
		attachFilterEvent : function(){
			console.log("attache key down");
			document.addEventListener("keydown",this.filterEventListener);
		},
		detachFilterEvent : function(){
			document.removeEventListener("keydown",this.filterEventListener);
		},
		
		attacheFilterBtnEvent: function(){
			var btn = document.getElementById("filterBtn");
			var that = this;
			btn.addEventListener("click",function(){
				var cost = document.getElementById("cost").value;
				var times = document.getElementById("times").value;
				var condition = document.getElementById("and").checked;
				that.tree.filter(cost,times,condition);
				that.tree.repaint();
			});
		},
		attachFilterClostBtnEvent:function(){
			var btn = document.getElementById("clostBtn");
			var that = this;
			btn.addEventListener("click",function(){
				console.log("close");
				that.attachFilterEvent();
				document.getElementById("filterDialog").classList.toggle("hide");
				if(document.getElementById("filterDialog").open){
					document.getElementById("filterDialog").close();
				}
			});
		},
		attachFilterRestBtnEvent:function(){
			var btn = document.getElementById("resetBtn");
			var that = this;
			btn.addEventListener("click",function(){
				that.tree.filter(null,null,null);
				that.tree.repaint();
			});
		},
	};

	return a;
});