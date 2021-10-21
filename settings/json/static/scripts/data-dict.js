/**********************************************************************
 *  tree hotkey
 **********************************************************************/
function hotkey(e) {
    if (e.keyCode === 220) {
        //反斜杠
        let tree = document.getElementById("tree");
        // let searchKey = tree.contentWindow.document.getElementById("searchKey");
        let searchKey = document.getElementById("searchKey");
        searchKey.focus();
    }
}

function ddcOnload() {
    let ddc = document.getElementById("ddc");

    ddc.contentWindow.document.addEventListener("keyup", function (e) {
        hotkey(e);
    });
}

window.onload = function () {
    let tree = document.getElementById("tree");

    //tree.contentWindow.
    document.addEventListener("keyup", function (e) {
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
        onClick: onClick,
    },
};

let oldTreeData = JSON.parse(JSON.stringify(dataDictIndexData));

function onClick(e, treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj("dataDictTree");
    zTree.expandNode(treeNode);

    if (treeNode.children !== undefined && treeNode.children.length > 0) {
        return;
    }

    let urlHref = window.location.href;
    urlHref = urlHref.slice(0, urlHref.lastIndexOf("/"));

    loadDataDict(treeNode.id) && sendBaidu(urlHref);
}

function loadDataDict(classId) {
    $("#intro").css("display", "none");
    $("#dataDictArea").css("display", "initial");

    $.ajax({
        url: "./dict/" + classId + ".json",
        dataType: "json",
        success: function (data) {
            if (data === undefined) {
                return false;
            }

            $("#classDisplayName").html(data.displayName);
            $("#classDefaultTableName").html(" (" + data.defaultTableName + "/" + data.fullClassname + ")");

            let ddcBody = "";

            let index = 1;
            data.propertyVO.forEach((propertyVO) => {
                let trClass = propertyVO.keyProp ? "pk-row" : "";

                ddcBody += `<tr class="${trClass}">
                                <td>${index++}</td>
                                <td>${propertyVO.name}</td>
                                <td>${propertyVO.displayName}</td>
                                <td>${propertyVO.name}</td>
                                <td>${propertyVO.sqlDateType}</td>
                                <td style="text-align: center">${propertyVO.nullable && propertyVO.nullable === "N" ? "√" : ""}</td>
                                <td>${propertyVO.refClassPathHref}</td>
                                <td>${propertyVO.attrMinValue ? propertyVO.attrMinValue : ""}</td>
                                <td>${propertyVO.dataScope ? propertyVO.dataScope : ""}</td>
                            </tr>`;
            });

            $("#dataDictTableBody").html(ddcBody);

            document.getElementById("dataDictArea").scrollTop = 0;
        },
    });
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
    $.fn.zTree.init($("#dataDictTree"), setting, data);
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

(function () {
    var baiduScript = document.createElement("script");

    baiduScript.src = "https://hm.baidu.com/hm.js?5db3a9b186d5e3bb0c6e427b56570e2e";

    var scripts = document.getElementsByTagName("script")[0];

    scripts.parentNode.appendChild(baiduScript, scripts);
})();

function sendBaidu(path) {
    if (window._hmt) {
        window._hmt.push(["_trackPageview", path]);
    }
}
