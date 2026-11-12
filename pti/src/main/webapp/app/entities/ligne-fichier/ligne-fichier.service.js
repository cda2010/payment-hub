(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('LigneFichier', LigneFichier);

    LigneFichier.$inject = ['$resource','BASE_URL_API'];

    function LigneFichier ($resource,BASE_URL_API) {
        var resourceUrl =  'api/ligne-fichier/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'getLigneMessage': {method: 'GET', url: BASE_URL_API+'findByChargementId' ,isArray: true},
            'deleteChargement' : {method: 'GET', url: BASE_URL_API + 'deleteChargement', isArray: false},
            'queryDetailByStatut' : {method: 'GET', url: '/api/ligne-fichier/requete', isArray: true},
            'queryDetailByOrganisation' : {method: 'GET', url: '/api/ligne-fichier/organisation', isArray: true},
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
