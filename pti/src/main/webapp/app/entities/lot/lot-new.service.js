(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LotNew', LotNew);

    LotNew.$inject = ['$resource','$http','BASE_URL_API'];

    function LotNew ($resource, $http, BASE_URL_API) {
        var resourceUrl =  'api/lots/:id';

        var resource = $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'export': { method: 'POST', url: BASE_URL_API+'etat/lots/export', isArray: false},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' },
            'setEtat': { method:'PUT', url:BASE_URL_API+'lots/set-etat', isArray: false}
        });

        // Nouvelle méthode pour télécharger un ZIP
        resource.exportZip = function(lot, callback) {
            $http({
                method: 'POST',
                url: BASE_URL_API+'etat/lots/exportZip', // endpoint backend pour ZIP
                data: lot,
                responseType: 'arraybuffer' // très important pour les fichiers binaires
            }).then(function(response) {
                var blob = new Blob([response.data], { type: 'application/zip' });
                var downloadUrl = URL.createObjectURL(blob);
                var a = document.createElement("a");
                a.href = downloadUrl;
                a.download = "lot_" + lot.id + ".zip";
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);

                if (callback) callback();
            }, function(error) {
                console.error('Erreur export Lot ZIP', error);
                if (callback) callback(error);
            });
        };

        return resource;
    }
})();
