(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('Champs', Champs);

    Champs.$inject = ['$resource','BASE_URL_API'];

    function Champs ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'champs/:id';

        return $resource(resourceUrl, {}, {
            'queryByEcran': { method: 'GET',url:BASE_URL_API+'champs/of-ecran/:id', isArray: true},
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
