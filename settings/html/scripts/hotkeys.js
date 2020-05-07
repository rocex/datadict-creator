document.onkeydown = function() {
	let keyword = document.getElementById('keyword');

	if (event.keyCode == 191) {
		keyword.focus();
		return false;
	}

	return true;
};
