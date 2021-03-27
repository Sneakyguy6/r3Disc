(function ($) {
	"use strict";
	$("form").submit( function (e) {
    	e.preventDefault();
		var formdata = $(this).serialize();
		$.ajax({
	    	type: 'GET',
	    	async: false,
	    	url: '/tomcatR3/Streams/TwitchSubscribe',
	    	data: formdata,
	    	dataType: 'text',
	    	contentType: 'text/plain',
	    	success: function(result, status, xhr) {
	    		alert(xhr.responseText);
	    	},
			error: function(xhr, status, error) {
				alert("Error: " + xhr.status + ", " + xhr.statusText + ", " + xhr.responseText + ", " + xhr.readyState);
			}
		}).done();
	});
})(jQuery);