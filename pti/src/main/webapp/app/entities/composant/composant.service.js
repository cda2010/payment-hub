(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Composant', Composant);

    Composant.$inject = ['$resource','BASE_URL_API'];

    function Composant ($resource,BASE_URL_API) {
        var resourceUrl =  'api/composants/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryById': { method: 'GET',url:resourceUrl, isArray: false},
            'findAllListe': {method: 'GET', url:BASE_URL_API +'composants-all',isArray: true},
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
