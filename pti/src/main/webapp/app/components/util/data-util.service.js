(function() {
    'use strict';

    angular
        .module('ptiApp')
        .factory('DataUtils', DataUtils);

    DataUtils.$inject = ['$window','$http'];

    function DataUtils ($window,$http) {

        var service = {
            abbreviate: abbreviate,
            byteSize: byteSize,
            openFile: openFile,
            toBase64: toBase64,
            downloadPdf:downloadPdf
        };

        return service;

       

        function abbreviate (text) {
            if (!angular.isString(text)) {
                return '';
            }
            if (text.length < 30) {
                return text;
            }
            return text ? (text.substring(0, 15) + '...' + text.slice(-10)) : '';
        }

        function byteSize (base64String) {
            if (!angular.isString(base64String)) {
                return '';
            }

            function endsWith(suffix, str) {
                return str.indexOf(suffix, str.length - suffix.length) !== -1;
            }

            function paddingSize(base64String) {
                if (endsWith('==', base64String)) {
                    return 2;
                }
                if (endsWith('=', base64String)) {
                    return 1;
                }
                return 0;
            }

            function size(base64String) {
                return base64String.length / 4 * 3 - paddingSize(base64String);
            }

            function formatAsBytes(size) {
                return size.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ') + ' bytes';
            }

            return formatAsBytes(size(base64String));
        }

        function serviceDownloadPdf() {
           
    }

        function downloadPdf (data) {
            var fileName = "file_name.pdf";
            var a = document.createElement("a");
            document.body.appendChild(a);
                var file = data;
                var fileURL = window.URL.createObjectURL(file);
                a.href = fileURL;
                a.download = fileName;
                a.click();
        };

        function debugBase64(base64URL){
            var win = window.open();
            win.document.write('<iframe src="' + base64URL  + '" frameborder="0" style="border:0; top:0px; left:0px; bottom:0px; right:0px; width:100%; height:'+screen.height+';" allowfullscreen></iframe>');
        }

        function openFile (type, data) {
            console.log("open file");
            //downloadPdf(data);
          debugBase64('data:' + type + ';base64,' + escape(data));
         //  $window.open('data:' + type + ';base64,' + escape(data), '_top', 'height=300,width=400');
        }
     
        function toBase64 (file, cb) {
            var fileReader = new FileReader();
            fileReader.readAsDataURL(file);
            fileReader.onload = function (e) {
                var base64Data = e.target.result.substr(e.target.result.indexOf('base64,') + 'base64,'.length);
                cb(base64Data);
            };
        }
    }
})();
