(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('FournisseurService', FournisseurService);

    FournisseurService.$inject = ['$resource','BASE_URL_API'];

    function FournisseurService ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'fournisseur-services/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'find': { method: 'GET',url: BASE_URL_API+'omnnicanal/fournisseur', isArray: true},
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
