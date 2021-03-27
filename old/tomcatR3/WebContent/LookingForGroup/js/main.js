
(function ($) {
	"use strict";
	
	//var urlParams = new URLSearchParams(window.location.search);
	//document.getElementById(id).innerHTML = new HTML
	
    /*==================================================================
    [ Validate ]*/
    var input = $('.validate-input .input100');
    var canSubmit = true;

    $('.validate-form').on('submit',function(){
        var check = true;

        for(var i=0; i<input.length; i++) {
            if(validate(input[i]) == false){
                showValidate(input[i]);
                check=false;
            }
        }
        canSubmit = check
        return check;
    });


    $('.validate-form .input100').each(function(){
        $(this).focus(function(){
           hideValidate(this);
        });
    });

    function validate (input) {
        if($(input).attr('type') == 'email' || $(input).attr('name') == 'email') {
            if($(input).val().trim().match(/^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{1,5}|[0-9]{1,3})(\]?)$/) == null) {
                return false;
            }
        }
        else {
            if($(input).val().trim() == ''){
                return false;
            }
        }
    }

    function showValidate(input) {
        var thisAlert = $(input).parent();

        $(thisAlert).addClass('alert-validate');
    }

    function hideValidate(input) {
        var thisAlert = $(input).parent();

        $(thisAlert).removeClass('alert-validate');
    }
    
    $("form").submit( function (e) {
    	e.preventDefault();
    	if(!canSubmit){
    		return false;
    	}
		var formdata = $(this).serialize();
		$.ajax({
	    	type: 'GET',
	    	async: false,
	    	url: '/tomcatR3/lfg',
	    	data: formdata,
	    	dataType: 'text',
	    	contentType: 'text/plain',
	    	success: function(result, status, xhr) {
	    		alert(xhr.responseText);
	    	},
			error: function(xhr, status, error) {
				//var err = eval("(" + xhr.responseText + ")");
				//alert(err.Message);
				alert("Error: " + xhr.status + ", " + xhr.statusText + ", " + xhr.responseText + ", " + xhr.readyState);
	    	}
	    	/*complete: function (xhr, status) {
	    		if (status === 'error' || !xhr.responseText) {
	    			console.log(error);
	    			alert(status);
	    	    }
	    	    else {
	    	    	console.log('It Works!');
	    	    	alert(xhr.responseText)
	    	    }
	    	}*/
	}).done();
	
	/*$(document).ready(function() {
		alert(window.location.pathname);
	});*/
		//window.location.replace("http://localhost:8080/tomcatR3/confirmed/message.html?m=Post created!");
		/*var xhr = new XMLHttpRequest();
		xhr.open('GET', 'http://localhost:25564/lfg', true);

		// If specified, responseType must be empty string or "text"
		xhr.responseType = 'text';

		xhr.onload = function () {
		    if (xhr.readyState === xhr.DONE) {
		        if (xhr.status === 200) {
		        	alert(xhr.response);
		        	alert(xhr.responseText)
		            console.log(xhr.response);
		            console.log(xhr.responseText);
		        } else {
		        	alert(xhr.status);
		        }
		    }
		};
		
		xhr.send(formdata);*/
    	/*var request=new XMLHttpRequest();

        request.onreadystatechange=function(){
            if (request.readyState == 4) {
                if (request.status == 200){
                    alert(request.responseText)
                    //var progress=parseInt(request.responseText);
                    //bar.setAttribute("value",progress);
                }
                else
                    alert("Error: "+request.status+request.statusText);
            }
        }
        request.open("GET", "http://localhost:4096/lfg",true);
        request.send();*/
		return false;
	});

})(jQuery);