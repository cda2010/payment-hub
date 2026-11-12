(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('DetailHistorique', DetailHistorique);

    DetailHistorique.$inject = ['$resource'];

    function DetailHistorique ($resource) {
        var resourceUrl =  'api/detail-historiques/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
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
