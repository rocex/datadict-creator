//

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
