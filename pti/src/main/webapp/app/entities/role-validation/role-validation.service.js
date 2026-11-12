(function() {
    'use strict';
    angular
        .module('ptiApp')
        .factory('RoleValidation', RoleValidation);

    RoleValidation.$inject = ['$resource','BASE_URL_API'];

    function RoleValidation ($resource,BASE_URL_API) {
        var resourceUrl =BASE_URL_API+'role-validations/:id';

        return $resource(resourceUrl, {}, {
            'query': { method: 'GET', isArray: true},
            'findAuthorities': { method: 'GET',url:BASE_URL_API+'role-validations/authorities', isArray: true},
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
