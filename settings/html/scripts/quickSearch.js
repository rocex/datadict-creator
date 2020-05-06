var setting = {
	view: {
		dblClickExpand: false,
		showLine: true
	},
	data: {
		simpleData: {
			enable: true
		}
	},
	callback: {
		onClick: onClick
	}
};

let oldTreeData = JSON.parse(JSON.stringify(dataDictIndexData));

function onClick(e, treeId, treeNode) {
	var zTree = $.fn.zTree.getZTreeObj('DataDict');
	zTree.expandNode(treeNode);
}

function debounce(fn, time) {
	var timer;
	return function() {
		let _this = this;
		let args = Array.from(arguments);
		if (!timer) clearTimeout(timer);
		timer = setTimeout(function() {
			fn.call(_this, args);
		}, time);
	};
}

function throttle(fn, timer) {
	var oldTime = 0;
	return function() {
		let args = Array.from(arguments);
		let nowTime = Date.now();
		if (nowTime - oldTime > timer) {
			fn.call(this, ...args);
			oldTime = nowTime;
		}
	};
}

function setTreeData(data) {
	var treeObj = $.fn.zTree.init($('#DataDict'), setting, data);
}

function filterData(searchVal, dictIndexData) {
	let childrenArr = [];

	let searchData = dictIndexData.filter(function(item) {
		if (!item.pId) {
			item.open = true;
			return true;
		}
		if (!!item.name && item.name.includes(searchVal)) {
			if (!childrenArr.includes(item.pId)) childrenArr.push(item.pId);
			return true;
		}
		return false;
	});

	return searchData.filter(function(item) {
		if (!item.pId && !childrenArr.includes(item.id)) {
			return false;
		}
		return true;
	});
}

function searchUse() {
	let keyword = document.getElementById('keyword');

	var searchDomVal = keyword.value;

	if (!searchDomVal) {
		setTreeData(oldTreeData);
		return;
	}

	let searchData = filterData(searchDomVal, dataDictIndexData);

	setTreeData(searchData);
}

$(document).ready(function() {
	let keyword = document.getElementById('keyword');

	setTreeData(dataDictIndexData);

	keyword.addEventListener('keyup', debounce(searchUse, 500));
});
