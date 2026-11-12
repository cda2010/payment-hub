(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('PersonneMorale', PersonneMorale);

    PersonneMorale.$inject = ['$resource','BASE_URL_API'];

    function PersonneMorale ($resource,BASE_URL_API) {
        var resourceUrl =  BASE_URL_API+'personne-morales/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: false},
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
