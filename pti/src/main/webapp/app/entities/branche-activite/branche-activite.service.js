(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('BrancheActivite', BrancheActivite);

    BrancheActivite.$inject = ['$resource','BASE_URL_API'];

    function BrancheActivite ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'branche-activites/:id';

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
