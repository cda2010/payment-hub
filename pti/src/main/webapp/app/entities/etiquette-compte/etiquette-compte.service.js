(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('EtiquetteCompte', EtiquetteCompte);

    EtiquetteCompte.$inject = ['$resource','BASE_URL_API'];

    function EtiquetteCompte ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'etiquette-comptes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryByPTI': {method: 'GET',url:'/api/etiquettePTI' ,isArray: true},
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
