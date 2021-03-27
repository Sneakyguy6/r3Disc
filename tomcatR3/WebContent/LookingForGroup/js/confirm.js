(function ($) {
	"use strict";
	$(document).ready(function() {
    	const params = new URLSearchParams(window.location.search);
    	//alert('is being triggered');
    	$.ajax({
	    	type: 'GET',
	    	async: false,
	    	url: '/tomcatR3/lfgVerif',// + new URLSearchParams(window.location.search).toString(),
	    	dataType: 'text',
	    	contentType: 'text/plain',
	    	data: {
	    		"verifKey": params.get('verifKey'),
	    		"accept": params.get('accept')
	    	},
	    	success: function(result, status, xhr) {
	    		//alert(xhr.responseText);
	    		document.getElementById("responseText").textContent=xhr.responseText;
	    	},
			error: function(xhr, status, error) {
				alert("Error: " + xhr.status + ", " + xhr.statusText + ", " + xhr.responseText + ", " + xhr.readyState);
	    	}
		}).done();
    });
})(jQuery);