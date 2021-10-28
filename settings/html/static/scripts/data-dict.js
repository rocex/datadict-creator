/**********************************************************************
 *  tree hotkey
 **********************************************************************/
function hotkey(e) {
	if (e.keyCode === 220) {
		//反斜杠
		let tree = document.getElementById('tree');
		let searchKey = tree.contentWindow.document.getElementById('searchKey');
		searchKey.focus();
	}
}

function ddcOnload() {
	let ddc = document.getElementById('ddc');

	ddc.contentWindow.document.addEventListener('keyup', function(e) {
		hotkey(e);
	});
}

window.onload = function() {
	let tree = document.getElementById('tree');

	tree.contentWindow.document.addEventListener('keyup', function(e) {
		hotkey(e);
	});
};

/**********************************************************************
 *  quick search
 **********************************************************************/
 var setting = {
    view: {
        dblClickExpand: false,
        showLine: true,
    },
    data: {
        simpleData: {
            enable: true,
        },
    },
    callback: {
        beforeClick: beforeClick,
        onClick: onClick,
    },
};

let oldTreeData = JSON.parse(JSON.stringify(dataDictIndexData));

function beforeClick(treeId, treeNode, clickFlag) {
    treeNode.url = "./dict/" + treeNode.id + ".html";
    treeNode.target = "ddc";

    return true;
}

function onClick(e, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj("DataDict");
    zTree.expandNode(treeNode);

    let urlHref = window.location.href;
    urlHref = urlHref.slice(0, urlHref.lastIndexOf("/"));

    treeNode.url && sendBaiDu(urlHref);
}

function debounce(fn, time) {
    var timer;
    return function () {
        let _this = this;
        let args = Array.from(arguments);
        if (!timer) clearTimeout(timer);
        timer = setTimeout(function () {
            fn.call(_this, args);
        }, time);
    };
}

function throttle(fn, timer) {
    var oldTime = 0;
    return function () {
        let args = Array.from(arguments);
        let nowTime = Date.now();
        if (nowTime - oldTime > timer) {
            fn.call(this, ...args);
            oldTime = nowTime;
        }
    };
}

function setTreeData(data) {
    var treeObj = $.fn.zTree.init($("#DataDict"), setting, data);
}

function filterData(searchVal, dictIndexData) {
    let childrenArr = [];

    let searchData = dictIndexData.filter(function (item) {
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

    return searchData.filter(function (item) {
        if (!item.pId && !childrenArr.includes(item.id)) {
            return false;
        }
        return true;
    });
}

function searchUse() {
    let searchKey = document.getElementById("searchKey");

    var searchDomVal = searchKey.value;

    if (!searchDomVal) {
        setTreeData(oldTreeData);
        return;
    }

    let searchData = filterData(searchDomVal, dataDictIndexData);

    setTreeData(searchData);
}

$(document).ready(function () {
    let searchKey = document.getElementById("searchKey");

    setTreeData(dataDictIndexData);

    searchKey.focus();

    searchKey.addEventListener("keyup", debounce(searchUse, 500));
});


/**********************************************************************
 *  baidu trance
 **********************************************************************/

var _hmt = _hmt || [];

(function() {
	var hm = document.createElement('script');

	hm.src = 'https://hm.baidu.com/hm.js?5db3a9b186d5e3bb0c6e427b56570e2e';

	var s = document.getElementsByTagName('script')[0];

	s.parentNode.insertBefore(hm, s);
})();

function sendBaiDu(path) {
	if (window._hmt) {
		window._hmt.push([ '_trackPageview', path ]);
	}
}
