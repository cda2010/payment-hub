(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('OperationSuivis', OperationSuivis);

    OperationSuivis.$inject = ['$resource'];

    function OperationSuivis ($resource) {
        var resourceUrl =  'api/findOperaSuivisByClient';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'findByNom': {method: 'GET', url: '/api/findByNom/:nom' , isArray: true},
            'findByPrenom': {method: 'GET', url: '/api/findByPrenom/:prenom', isArray: true},
            'findByCodeAndNom': {method: 'GET' , url: '/api/findByCodeAndNom/:code/:nom', isArray: true},
            'findByTelAndNom' : {method: 'GET', url: '/api/findByTelAndNom/:tel/:nom', isArray: true},
            'findByCodeAndTelAndNom' : {method: 'GET', url: '/api/findByCodeAndTelAndNom/:code/:tel/:nom', isArray: true},
            'findOperaSuivisByClient': {method: 'GET', url: '/api/findOperaSuivisByClient', isArray: true},
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
