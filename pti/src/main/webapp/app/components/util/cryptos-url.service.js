(function () {
    'use strict';

    angular
        .module('ptiApp')
        .factory('CryptosUrl', CryptosUrl);

    CryptosUrl.$inject = ['CRYPTOS_BASE_URL'];

    function CryptosUrl(CRYPTOS_BASE_URL) {
        var baseUrl = CRYPTOS_BASE_URL || 'http://127.0.0.1:55555';

        return {
            signObject: signObject,
            printPdf: printPdf,
            deleteFile: deleteFile
        };

        function signObject(username) {
            return baseUrl + '/x-x-x-x/sign-object/' + username + '?callback=JSON_CALLBACK';
        }

        function printPdf() {
            return baseUrl + '/x-x-x-x/print_pdf';
        }

        function deleteFile(fileUrl) {
            return baseUrl + '/x-x-x-x/delete?url=' + fileUrl;
        }
    }
})();
