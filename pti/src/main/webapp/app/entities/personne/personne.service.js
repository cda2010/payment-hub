(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Personne', Personne);

    Personne.$inject = ['$resource','BASE_URL_API'];

    function Personne ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'personnes/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getInfosLien': {method: 'GET',url: BASE_URL_API+'getInfosLien' , isArray: false},
            'getDoublouByPieceOrHash': {method: 'POST',url: BASE_URL_API+'personnes/getDoublouByPieceOrHash' , isArray: true},
            'resetPassword': { method: 'GET','url':BASE_URL_API+'user-controls/reset-password', isArray: false},
            'saveCompteBanque': {method: 'POST', 'url':BASE_URL_API+'saveCompteBanque-of-client', isArray: false},
            'getAuditOfPersonne':{method: 'GET', 'url': BASE_URL_API+'entity-audit-events/entite',isArray: true},
            'getBilanOperationByPersonne':{method: 'GET', 'url': BASE_URL_API+'bilan-operation/by-personne',isArray: true},
            'getUserControle':{method: 'GET', 'url': BASE_URL_API+'user-controle/by-personne',isArray: true},
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
