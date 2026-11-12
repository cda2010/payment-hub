(function () {
    "use strict";

    angular.module("ptiApp").factory('Utils', ['$timeout','$http', 'RequestUtils', '$sce', function ($timeout,$http, RequestUtils, $sce) {


        return {
            "print": print,
            "printUrl": printUrl,
            "postPrintUrl": postPrintUrl,
            "downloadFile": downloadFile,
            "downloadFileWithGet": downloadFileWithGet
        };

        /**
         * Imprime un document sans avoir à l'afficher dans le navigateur courant
         */
        function print(doc) {
            var objectFrame = document.createElement('iframe');
            objectFrame.style.visibility = "hidden";
            objectFrame.src = doc;
            document.body.appendChild(objectFrame);
            objectFrame.contentWindow.focus();
            objectFrame.contentWindow.open();
        }

        function printUrl(url) {
            $http.get(url, {
                responseType: 'arraybuffer'
            }).success(function (data) {
                var file = new Blob([data], {
                    type: 'application/pdf'
                });
                var fileURL = URL.createObjectURL(file);
                print(fileURL);
            });
        }

        function printUrl(url, data) {
            $http.get(url, data, {
                responseType: 'arraybuffer'
            }).success(function (data) {
                var file = new Blob([data], {
                    type: 'application/pdf'
                });
                var fileURL = URL.createObjectURL(file);
                print(fileURL);
            });
        }

        function postPrintUrl(url, data) {
            $http.post(url, data, {
                responseType: 'arraybuffer'
            }).success(function(data) {
                var file = new Blob([data], {
                    type: 'application/pdf'
                });
                var fileURL = URL.createObjectURL(file);
                window.open(fileURL);
            });
        }


        


     /*   
       RequestUtils.Print.save(datas, function (result) {
               
                $http.post('http://127.0.0.1:55555/x-x-x-x/print_pdf', result.url).then(function (arg) {
                        console.log("ok");
                    }).catch(function (err) {});
            }, function () {
                vm.isSaving = false;
            });
     function postPrintUrl(url, data) {
            $http.post(url, data, {
                responseType: 'arraybuffer'
            }).success(function (data) {
                var file = new Blob([data], {
                    type: 'application/pdf'
                });
                var fileURL = URL.createObjectURL(file);
                window.open(fileURL);
                //print(fileURL);
            });
        }*/

        function deleteFile() {
            var href = "http://127.0.0.1:55555/x-x-x-x/delete?" + "url=" + link.download;
            $http.jsonp(href)
                .success(function (data) {

                }).error(function () {

                });
        }

        function downloadFile(url, data, name) {
            console.log('data',data);
            $http.post(url, data, {
                    responseType: 'arraybuffer'
                })
                .success(function (response) {
                    var file = new Blob([(response)], {
                        type: 'application/'
                    });
                    var lien = URL.createObjectURL(file);
                    var link = document.createElement("a");
                    console.log('==URL POUR IMPRESSION==');
                    console.log(lien);
                    link.href = lien;
                    link.download = name;
                    link.click();
                });
        }

        function downloadFileWithGet(url, data, name) {
            $http.get(url, data, {
                responseType: 'arraybuffer'
            }).success(function (response) {
                /* var file = new Blob([(response)], {
                     type: 'application/' + data.format.toLowerCase()
                 });
                 var lien = URL.createObjectURL(file);
                 var link = document.createElement("a");
                 link.href = lien;
                 link.download = name;
                 link.click();*/
            });
        }
    }]);
})();
