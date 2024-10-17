let strSearchText;
let strFullText = [];

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
};

/**********************************************************************
 *  quick search
 **********************************************************************/
var setting = {
    view: {
        dblClickExpand: false,
        showLine: true,
        nameIsHTML: true, //允许name支持html
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
dataDictIndexData.forEach((item) => {
    item.oldName = item.name;
});

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
        cache: true,
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

                let strName = propertyVO.name || "";
                let strDislpayName = propertyVO.displayName || "";
                let strColumnCode = propertyVO.columnCode || propertyVO.name || "";

                strName = strName.replace(strSearchText, "<span class='high-light'>" + strSearchText + "</span>");
                strDislpayName = strDislpayName.replace(strSearchText, "<span class='high-light'>" + strSearchText + "</span>");
                strColumnCode = strColumnCode.replace(strSearchText, "<span class='high-light'>" + strSearchText + "</span>");

                ddcBody += `<tr class="${trClass}">
                        <td style="text-align: right;">${index++}</td>
                        <td>${strDislpayName}</td>
                        <td>${strName}</td>
                        <td>${strColumnCode}</td>
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
    let strFindIds = [];

    dictIndexData.filter(function (item) {
        if (!item.pId) {
            item.open = true;
            return true;
        }

        if (!!item.oldName && item.oldName.toLowerCase().includes(searchVal)) {
            if (item.path) {
                strFindIds = [...new Set([...strFindIds, ...item.path.split(","), item.id])];
            }

            return true;
        }

        return false;
    });

    let blFullText = document.getElementById("chckFullText").checked;
    if (blFullText) {
        let strFindFullIds = [];

        strFullText.filter((strText) => {
            if (strText.name.includes(searchVal)) {
                strFindFullIds.push(strText.id);
                return true;
            }
            return false;
        });

        dataDictIndexData.filter(function (item) {
            if (strFindFullIds.includes(item.id)) {
                strFindIds = [...new Set([...strFindIds, ...item.path.split(","), item.id])];
            }
        });
    }

    return dictIndexData.filter(function (item) {
        let blFind = strFindIds.includes(item.id);
        item.name = blFind && item.oldName.includes(searchVal) ? item.oldName.replace(searchVal, "<span class='high-light'>" + searchVal + "</span>") : item.oldName;
        return blFind;
    });
}

function searchUse() {
    let searchKey = document.getElementById("searchKey");

    strSearchText = searchKey.value;

    var iLength = 0;

    for (var i = 0; i < strSearchText.length; i++) {
        var strTemp = strSearchText.charCodeAt(i);

        iLength = iLength + (strTemp >= 0 && strTemp <= 255 ? 1 : 2);
    }

    if (iLength < 3) {
        if (strSearchText.length == 0) {
            setTreeData(oldTreeData);
            return;
        }

        return;
    }

    if (!strSearchText) {
        setTreeData(oldTreeData);
        return;
    }

    let searchData = filterData(strSearchText.toLowerCase(), dataDictIndexData);

    setTreeData(searchData);
}

$(document).ready(function () {
    let searchKey = document.getElementById("searchKey");

    searchKey.focus();
    searchKey.addEventListener("keyup", debounce(searchUse, 500));

    initUI();

    let strClassId = getUrlSearchParam("dataType");
    strSearchText = getUrlSearchParam("keyword");

    if (strClassId) {
        loadDataDict(strClassId);
    } else if (strSearchText) {
        searchKey.value = strSearchText;
        searchUse();
    } else {
        setTreeData(dataDictIndexData);
    }

    setTimeout(() => {
        fetchFullText(); // 调用异步函数
    }, 3000);
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
            document.getElementById("ddcVersion").innerText = data.ddcTitle;
            document.getElementById("ddcCreateTime").innerText = data.ddcTs;
            document.getElementById("fullTextIndex").value = data.fullTextIndex;
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

    baiduScript.src = "https://hm.baidu.com/hm.js?79cba97204629d9fcf1ed81661fa43d3";

    var scripts = document.getElementsByTagName("script")[0];

    scripts.parentNode.appendChild(baiduScript, scripts);
})();

function sendBaidu(path) {
    if (window._hmt) {
        window._hmt.push(["_trackPageview", path]);
    }
}

async function fetchFullText() {
    let fullTextFileName = document.getElementById("fullTextIndex").value.split(",");

    console.log(fullTextFileName);

    let promises = fullTextFileName.map((id) => {
        return fetchData("./scripts/full-text-" + id + ".json");
    });

    function fetchData(url) {
        return fetch(url)
            .then((response) => response.json())
            .then((json) => {
                console.log(json.data.length);
                return json.data;
            })
            .catch((ex) => console.log(ex));
    }

    await Promise.all(promises)
        .then((results) => {
            results.forEach((result) => {
                strFullText = strFullText.concat(result);
                result = null;
            });
        })
        .then(() => {
            document.getElementById("chckFullText").disabled = false;
        })
        .catch((ex) => console.log(ex));

    console.log("length:" + strFullText.length);
}
