(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Utilitaire', Utilitaire);

    Utilitaire.$inject = ['$resource','BASE_URL_API'];

    function Utilitaire ($resource,BASE_URL_API) {
        var resourceUrl =  'api/utilitaires/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'startOrganisationAPI':{method: 'POST' ,isArray: true,url:BASE_URL_API+'tagpay/db/populate/agent-sous-distributeur'},
            'startOrganisationCompteAPI':{method: 'POST' ,isArray: false,url:BASE_URL_API+'tagpay/db/populate/comptes/agent'},
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
