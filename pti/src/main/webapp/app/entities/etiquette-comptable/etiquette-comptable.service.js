(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('EtiquetteComptable', EtiquetteComptable);

    EtiquetteComptable.$inject = ['$resource','BASE_URL_API'];

    function EtiquetteComptable ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'etiquette-comptables/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
