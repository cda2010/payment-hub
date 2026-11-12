(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('MotifOperation', MotifOperation);

    MotifOperation.$inject = ['$resource','BASE_URL_API'];

    function MotifOperation ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'motif-operations/:id';

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
