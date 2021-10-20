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
