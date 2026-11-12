(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('ChargementMessage', ChargementMessage);

    ChargementMessage.$inject = ['$resource', 'DateUtils','BASE_URL_API'];

    function ChargementMessage ($resource, DateUtils,BASE_URL_API) {
        var resourceUrl =  'api/chargement-messages/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'valider': {method: 'GET', url: BASE_URL_API+'valider-chargement',isArray: false },
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
