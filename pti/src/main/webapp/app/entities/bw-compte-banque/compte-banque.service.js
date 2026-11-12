(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('CompteBanque', CompteBanque);

    CompteBanque.$inject = ['$resource'];

    function CompteBanque ($resource) {
        var resourceUrl =  'api/v1/comptes-banques/:id';

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
