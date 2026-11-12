(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ChargementFichier', ChargementFichier);

    ChargementFichier.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function ChargementFichier ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  'api/chargement-fichiers/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'valider': {method: 'GET', url: BASE_URL_API+'chargement-fichiers/valider/:id/:user/:canal',isArray: false },
            'chargementByOrganisation': {method: 'GET', url: BASE_URL_API+'chargement-fichiers/chargementByOrganisation/:id',isArray: true },
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.date_chargement = DateUtils.convertDateTimeFromServer(data.date_chargement);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
