(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('CompteBanque', CompteBanque);

    CompteBanque.$inject = ['$resource','BASE_URL_API'];

    function CompteBanque ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'compte-banques/:id';

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
