(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LigneChargement', LigneChargement);

    LigneChargement.$inject = ['$resource'];

    function LigneChargement ($resource) {
        var resourceUrl =  'api/ligne-chargements/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'queryDetail': { method: 'GET', url: 'api/requete/:id', isArray: true },
            'findById': {method: 'GET', url:'api/ligne/:id'},
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
