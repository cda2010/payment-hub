(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Canal', Canal);

    Canal.$inject = ['$resource','BASE_URL_API'];

    function Canal ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'canals/:id';

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
