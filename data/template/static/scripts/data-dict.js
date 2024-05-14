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

    //分割条
    // var divSplitter = document.getElementById("splitter");
    // divSplitter.onmousedown = moveSplitter;

    $("#container").splitter({
        orientation: "horizontal",
        limit: 100,
    });

    // 版本号切换
    let ddcVersionSelect = document.getElementById("ddcVersion");
    ddcVersionSelect.addEventListener("change", function (evt) {
        var value = this.value;

        // 跳转到对应的链接
        window.open("/datadict/datadict-" + value + "/index.html");

        ddcVersionSelect.value = ddcVersion;
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

    //if (treeNode.children !== undefined && treeNode.children.length > 0) {
    //    return;
    //}
    if (treeNode.isDdcClass !== undefined && !treeNode.isDdcClass) {
        return;
    }

    loadDataDict(treeNode.id);
}

function loadDataDict(classId) {
    $("#intro").css("display", "none");
    $("#dataDictArea").css("display", "initial");

    $.ajax({
        cache: false,
        dataType: "json",
        url: "./dict/" + classId + ".json",
        error: function () {
            // alert("请求失败，请稍后再试或联系管理员！");
            $("#classTableName").html("");
            $("#classDisplayName").html("<span style='color:red'>请求失败，请稍后再试或联系管理员！</span>");
            $("#classList").html("&nbsp;");
            $("#dataDictTableBody").html("");
        },
        success: function (data) {
            if (data === undefined) {
                return false;
            }

            const classDefaultName =
                data.tableName && data.fullClassname
                    ? " (" + (data.tableName + "/" + data.fullClassname) + ")"
                    : data.tableName === undefined && data.fullClassname === undefined
                    ? ""
                    : " (" + (data.tableName || data.fullClassname) + ")";

            $("#classDisplayName").html(data.displayName || "");
            $("#classTableName").html(classDefaultName);
            $("#classList").html(data.classListUrl || "");

            if (data.propertyVO === undefined) {
                data.propertyVO = [];
            }

            let ddcBody = "";

            let index = 1;
            data.propertyVO.forEach((propertyVO) => {
                let trClass = propertyVO.keyProp ? "pk-row" : "";
                let dataTypeShowName = propertyVO.dataTypeDisplayName && propertyVO.dataTypeName ? propertyVO.dataTypeDisplayName + " (" + propertyVO.dataTypeName + ")" : "";

                let help = propertyVO.help;
                let dynamicTable = propertyVO.dynamicTable;

                help = help && dynamicTable ? help + "<br />" + dynamicTable : help ? help : dynamicTable ? dynamicTable : "";

                if (propertyVO.dataTypeStyle == 305 && propertyVO.dataTypeDisplayName && propertyVO.dataTypeName) {
                    dataTypeShowName = `
                        <a href="javascript:void(0);" onclick="loadDataDict('${propertyVO.dataType}');">${propertyVO.dataTypeDisplayName}</a> &nbsp;
                        <a href="./index.html?dataType=${propertyVO.dataType}&keyword=${propertyVO.dataTypeDisplayName}" target="_blank" title="新窗口查看 (${propertyVO.dataTypeDisplayName}) 实体" class="blankLink"> (${propertyVO.dataTypeName})</a>`;
                }

                ddcBody += `<tr class="${trClass}">
                        <td style="text-align: right;">${index++}</td>
                        <td>${propertyVO.displayName || ""}</td>
                        <td>${propertyVO.name || ""}</td>
                        <td>${propertyVO.columnCode || propertyVO.name || ""}</td>
                        <td>${propertyVO.dataTypeSql || ""}</td>
                        <td style="text-align: center">${propertyVO.nullable ? "" : "√"}</td>
                        <td>${dataTypeShowName || ""}</td>
                        <td>${propertyVO.defaultValue || ""}</td>
                        <td>${propertyVO.dataScope || ""}</td>
                        <td>${help || ""}</td>
                    </tr>`;
            });

            $("#dataDictTableBody").html(ddcBody);

            document.getElementById("dataDictArea").scrollTop = 0;
            document.getElementById("dataDictContainer").scrollTop = 0;

            sendBaidu(window.location.href);
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

    if (data && data.length < 500) {
        var zTree = $.fn.zTree.getZTreeObj("dataDictTree");

        zTree.expandAll(true);
    }
}

function filterData(searchVal, dictIndexData) {
    let childrenArr = [];

    dictIndexData.filter(function (item) {
        if (!item.pId) {
            item.open = true;
            return true;
        }

        if (!!item.name && item.name.toLowerCase().includes(searchVal)) {
            if (item.path) {
                childrenArr = [...new Set([...childrenArr, ...item.path.split(","), item.id])];
            }

            return true;
        }

        return false;
    });

    return dictIndexData.filter(function (item) {
        return childrenArr.includes(item.id);
    });
}

function searchUse() {
    let searchKey = document.getElementById("searchKey");

    var searchDomVal = searchKey.value;

    if (searchDomVal.length < 3) {
        if (searchDomVal.length == 0) {
            setTreeData(oldTreeData);
            return;
        }

        return;
    }

    if (!searchDomVal) {
        setTreeData(oldTreeData);
        return;
    }

    let searchData = filterData(searchDomVal.toLowerCase(), dataDictIndexData);

    setTreeData(searchData);
}

$(document).ready(function () {
    initUI();

    setTreeData(dataDictIndexData);

    let searchKey = document.getElementById("searchKey");

    searchKey.focus();
    searchKey.addEventListener("keyup", debounce(searchUse, 500));

    let strSearchValue = getUrlSearchParam("keyword");
    if (strSearchValue) {
        searchKey.value = strSearchValue;
        searchUse();
    }

    var strDataType = getUrlSearchParam("dataType");
    if (strDataType) {
        loadDataDict(strDataType);
    }
});

function initUI() {
    $.ajax({
        cache: false,
        dataType: "json",
        url: "./info.json",
        error: function () {
            // alert("请求失败，请稍后再试或联系管理员！");
            $("#classTableName").html("");
            $("#classDisplayName").html("<span style='color:red'>请求失败，请稍后再试或联系管理员！</span>");
            $("#classList").html("&nbsp;");
            $("#dataDictTableBody").html("");
        },
        success: function (data) {
            if (data === undefined) {
                return false;
            }

            // 设置当前选中版本
            document.title = data.ddcTitle;
            document.getElementById("ddcVersion").value = data.ddcVersion;
            document.getElementById("ddcCreateTime").innerText = data.ddcTs;
        },
    });
}

function clearSearchKey() {
    var searchKey = document.getElementById("searchKey");
    if (searchKey) {
        searchKey.focus();
        searchKey.value = "";

        searchUse();
    }
}

/**********************************************************************
 * 如果url中带keyword参数值对，就默认在左侧搜索
 **********************************************************************/
function getUrlSearchParam(strKey) {
    var search = window.location.search;
    var reg = new RegExp("" + strKey + "=([^&?]*)", "ig");

    var search2 = decodeURI(search);

    return search2.match(reg) ? search2.match(reg)[0].substring(strKey.length + 1) : null;
}

/**********************************************************************
 * 移动分隔条
 **********************************************************************/
function moveSplitter(e) {
    var divLeft = document.getElementById("left");
    if ("none" == divLeft.style.display) {
        return false;
    }

    // 改变分隔条左右宽度所需常量
    const divOrgLeftWidth = 500; // 左边部分原始宽度
    const rightDivLeftGap = 8; // 右边部分与左边部分的距离
    const divSplitterWidth = 8; // 分隔条宽度
    const divSplitterMinLeft = 100; // 分隔条左边部分最小宽度
    const divSplitterMaxLeft = 1000; // 分隔条左边部分最大宽度

    var divContainer = document.getElementById("container"),
        divRight = document.getElementById("right"),
        divSplitter = document.getElementById("splitter");

    var disX = e.clientX; // 记录下初始位置的值
    divSplitter.left = divSplitter.offsetLeft + 3;

    document.onmousemove = function (e) {
        var moveX = e.clientX - disX; // 鼠标拖动的偏移距离
        var iT = divSplitter.left + moveX, // 分隔条相对父级定位的 left 值
            maxT = divContainer.clientWidth - divSplitter.offsetWidth;

        if (iT < 0) {
            iT = 0;
        } else if (iT > maxT) {
            iT = maxT;
        }

        if (iT > divSplitterMinLeft && iT < divSplitterMaxLeft) {
            divLeft.style.width = divSplitter.style.left = iT + 3 + "px";
            divRight.style.left = iT + rightDivLeftGap + "px";
            divRight.style.width = document.body.clientWidth - iT - rightDivLeftGap - 6 + "px";
        }

        return false;
    };

    // 鼠标放开的时候取消操作
    document.onmouseup = function () {
        document.onmouseup = null;
        document.onmousemove = null;
    };
}

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
