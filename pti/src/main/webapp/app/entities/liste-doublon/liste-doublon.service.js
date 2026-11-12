(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ListeDoublon', ListeDoublon);

    ListeDoublon.$inject = ['$resource','BASE_URL_API'];

    function ListeDoublon ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'liste-doublon/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET',url: BASE_URL_API+'/getDoublon', isArray: true},
            'getDoublonListe':{method: 'GET',url: BASE_URL_API+'/getDoublon' ,isArray: true},
            'enregistrer': {method: 'POST', url: BASE_URL_API+'saveDoublon', isArray: false},
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
