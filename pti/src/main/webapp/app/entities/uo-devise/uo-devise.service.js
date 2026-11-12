(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('UoDevise', UoDevise);

    UoDevise.$inject = ['$resource','BASE_URL_API'];

    function UoDevise ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'uo-devises/:id';

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
