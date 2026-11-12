(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Historique', Historique);

    Historique.$inject = ['$resource', 'DateUtils'];

    function Historique ($resource, DateUtils) {
        var resourceUrl =  'api/historiques/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    if (data) {
                        data = angular.fromJson(data);
                        data.dateHistorique = DateUtils.convertDateTimeFromServer(data.dateHistorique);
                    }
                    return data;
                }
            },
            'update': { method:'PUT' }
        });
    }
})();
